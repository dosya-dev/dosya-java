package dev.dosya.sdk.internal;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dosya.sdk.DosyaClientOptions;
import dev.dosya.sdk.DosyaInterceptor;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaNetworkException;
import dev.dosya.sdk.exception.DosyaTimeoutException;
import dev.dosya.sdk.model.RateLimitInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Internal HTTP client: request execution, retries, timeouts and JSON handling.
 *
 * <p>Internal to the SDK; not part of the public API.
 *
 * @since 0.1.0
 */
public final class DosyaHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DosyaHttpClient.class);

    private static final String BOOKMARK_HEADER = "X-D1-Bookmark";
    private static final Set<String> IDEMPOTENT = new HashSet<>(Arrays.asList("GET", "HEAD", "PUT", "DELETE"));
    private static final Pattern MACHINE_CODE = Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final int MAX_ERROR_BODY = 64 * 1024;

    private final String baseUrl;
    private final String apiKey;
    private final int maxRetries;
    private final long baseDelay;
    private final long maxDelay;
    private final long readTimeout;
    private final long uploadTimeout;
    private final boolean readYourWrites;
    private final Consumer<RateLimitInfo> onRateLimit;
    private final Consumer<String> debugFn;
    private final DosyaInterceptor interceptor;
    private final Gson gson;
    private final Gson bodyGson;
    private final HttpClient following;
    private final HttpClient manual;
    private final AtomicReference<String> bookmark = new AtomicReference<>();

    public DosyaHttpClient(DosyaClientOptions options) {
        this.baseUrl = options.getBaseUrl().replaceAll("/+$", "");
        this.apiKey = options.getApiKey();
        this.maxRetries = options.getMaxRetries();
        this.baseDelay = options.getBaseDelay();
        this.maxDelay = options.getMaxDelay();
        this.readTimeout = options.getReadTimeout();
        this.uploadTimeout = options.getUploadTimeout();
        this.readYourWrites = options.isReadYourWrites();
        this.onRateLimit = options.getOnRateLimit();
        this.debugFn = options.getDebug();
        this.interceptor = options.getInterceptor();
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                // D1 answers flags as 0/1 on some routes and true/false on others.
                .registerTypeAdapter(boolean.class, LenientBoolean.INSTANCE)
                .registerTypeAdapter(Boolean.class, LenientBoolean.INSTANCE)
                // Integer columns can hold a stored decimal (7.5); one odd row must not
                // make a whole response unreadable. Decimals are truncated.
                .registerTypeAdapter(int.class, LenientNumber.INTEGER)
                .registerTypeAdapter(Integer.class, LenientNumber.INTEGER)
                .registerTypeAdapter(long.class, LenientNumber.LONG)
                .registerTypeAdapter(Long.class, LenientNumber.LONG)
                .create();
        // Request bodies keep explicit nulls: `parent_id: null` means "the root",
        // and a missing key would mean something else to several handlers.
        this.bodyGson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .serializeNulls()
                .create();

        HttpClient supplied = options.getHttpClient();
        if (supplied != null) {
            this.following = supplied;
            this.manual = supplied;
        } else {
            Duration connect = Duration.ofMillis(options.getConnectTimeout());
            this.following = HttpClient.newBuilder()
                    .connectTimeout(connect)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            this.manual = HttpClient.newBuilder()
                    .connectTimeout(connect)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
        }
    }

    // ── JSON ──

    /**
     * Sends a JSON request and returns the payload with {@code ok} removed.
     * Error statuses and {@code ok: false} throw {@link DosyaApiException}.
     */
    public JsonObject request(HttpRequest req) {
        Outcome<byte[]> out = send(req, HttpResponse.BodyHandlers.ofByteArray());
        String body = new String(out.response.body(), StandardCharsets.UTF_8);
        JsonElement parsed;
        try {
            parsed = body.isEmpty() ? new JsonObject() : JsonParser.parseString(body);
        } catch (Exception e) {
            throw apiError(out.response.statusCode(), out.response.headers(), req,
                    "Invalid JSON response: " + body.substring(0, Math.min(body.length(), 200)), body, e);
        }
        if (!parsed.isJsonObject()) {
            if (req.isEnvelope()) {
                throw apiError(out.response.statusCode(), out.response.headers(), req, "Unexpected response shape", body, null);
            }
            JsonObject wrapper = new JsonObject();
            wrapper.add("value", parsed);
            return wrapper;
        }
        JsonObject json = parsed.getAsJsonObject();
        if (req.isEnvelope()) {
            JsonElement ok = json.get("ok");
            if (ok == null || !ok.isJsonPrimitive() || !ok.getAsJsonPrimitive().isBoolean() || !ok.getAsBoolean()) {
                throw apiError(out.response.statusCode(), out.response.headers(), req, errorString(json), body, null);
            }
        }
        json.remove("ok");
        return json;
    }

    public <T> T requestAs(HttpRequest req, Class<T> type) {
        return gson.fromJson(request(req), type);
    }

    public <T> T requestAs(HttpRequest req, Type type) {
        return gson.fromJson(request(req), type);
    }

    public <T> T fromJson(JsonElement element, Class<T> type) {
        return gson.fromJson(element, type);
    }

    public <T> T fromJson(JsonElement element, Type type) {
        return gson.fromJson(element, type);
    }

    public JsonElement toJsonTree(Object src) {
        return gson.toJsonTree(src);
    }

    public Gson getGson() {
        return gson;
    }

    // ── Raw ──

    /**
     * Sends a request whose body the caller reads (binary, streams, redirects). The
     * attempt timeout covers waiting for the response headers only, so a long
     * download is not cut off. Error statuses still throw {@link DosyaApiException}.
     * The caller must close the returned stream.
     */
    public HttpResponse<InputStream> requestRaw(HttpRequest req) {
        return send(req, HttpResponse.BodyHandlers.ofInputStream()).response;
    }

    /**
     * GETs an absolute URL outside the API (a presigned storage URL) without the
     * Authorization header, bookmark or retries. Status is not checked.
     */
    public HttpResponse<InputStream> fetchExternal(URI uri, @Nullable Map<String, String> headers) {
        java.net.http.HttpRequest.Builder b = java.net.http.HttpRequest.newBuilder(uri).GET();
        if (headers != null) headers.forEach(b::header);
        try {
            return await(following.sendAsync(b.build(), HttpResponse.BodyHandlers.ofInputStream()), readTimeout);
        } catch (TimeoutException e) {
            throw new DosyaTimeoutException("GET " + uri.getHost() + " timed out after " + readTimeout + "ms", readTimeout, e);
        } catch (IOException e) {
            throw new DosyaNetworkException("GET " + uri.getHost() + " failed: " + e.getMessage(), e);
        }
    }

    /** Reads an error body from a raw response into an exception for that status. */
    public DosyaApiException errorFrom(HttpResponse<InputStream> response, String method, String path) {
        String text = readLimited(response.body());
        return apiErrorFromText(response.statusCode(), response.headers(), method, path, text);
    }

    // ── Settings for resources that run their own retry loops ──

    public int getMaxRetries() { return maxRetries; }
    public long getBaseDelay() { return baseDelay; }
    public long getMaxDelay() { return maxDelay; }
    public long getReadTimeout() { return readTimeout; }
    public long getUploadTimeout() { return uploadTimeout; }
    public String getBaseUrl() { return baseUrl; }

    /** Exponential backoff with 20% jitter, capped at maxDelay. */
    public long backoff(int attempt) {
        long delay = baseDelay * (1L << Math.min(attempt, 30));
        long jitter = (long) (delay * 0.2 * ThreadLocalRandom.current().nextDouble());
        return Math.min(delay + jitter, maxDelay);
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DosyaNetworkException("Request interrupted", e);
        }
    }

    /**
     * Form-encodes a query value.
     *
     * @deprecated for path segments use {@link PathSegments#seg(String)}.
     */
    @Deprecated
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ── Private ──

    private static final class Outcome<T> {
        final HttpResponse<T> response;

        Outcome(HttpResponse<T> response) {
            this.response = response;
        }
    }

    private <T> Outcome<T> send(HttpRequest req, HttpResponse.BodyHandler<T> handler) {
        String url = buildUrl(req.getPath(), req.getQuery());
        URI uri = URI.create(url);
        String method = req.getMethod();
        HttpRequest.Retry policy = req.getRetry();
        int attempts = policy == HttpRequest.Retry.NEVER ? 0 : maxRetries;
        boolean idempotent = policy == HttpRequest.Retry.ALWAYS || IDEMPOTENT.contains(method);
        long timeoutMs = req.getTimeoutMs() != null ? req.getTimeoutMs() : readTimeout;
        HttpClient client = req.isManualRedirect() ? manual : following;

        for (int attempt = 0; ; attempt++) {
            boolean canRetry = attempt < attempts;
            java.net.http.HttpRequest built = build(req, uri, timeoutMs);
            debug(method + " " + url + " (attempt " + (attempt + 1) + "/" + (attempts + 1) + ")");
            if (interceptor != null) interceptor.beforeRequest(method, url);
            long start = System.currentTimeMillis();

            HttpResponse<T> response;
            try {
                response = await(client.sendAsync(built, handler), timeoutMs);
            } catch (TimeoutException e) {
                if (canRetry && idempotent) {
                    debug("Timed out after " + timeoutMs + "ms, retrying");
                    sleep(backoff(attempt));
                    continue;
                }
                throw new DosyaTimeoutException(method + " " + req.getPath() + " timed out after " + timeoutMs + "ms", timeoutMs, e);
            } catch (IOException e) {
                if (canRetry && idempotent) {
                    long delay = backoff(attempt);
                    logger.warn("Request error on {} {}: {}, retrying in {}ms", method, url, e.getMessage(), delay);
                    debug("Request error: " + e.getMessage() + ", retrying in " + delay + "ms");
                    sleep(delay);
                    continue;
                }
                throw new DosyaNetworkException(method + " " + req.getPath() + " failed after " + (attempt + 1)
                        + (attempt == 0 ? " attempt" : " attempts") + ": " + e.getMessage(), e);
            }

            int status = response.statusCode();
            String requestId = response.headers().firstValue("X-Request-Id").orElse(null);
            long durationMs = System.currentTimeMillis() - start;
            logger.debug("{} {} -> {} ({}ms)", method, url, status, durationMs);
            debug(method + " " + url + " -> " + status);
            if (interceptor != null) interceptor.afterResponse(method, url, status, requestId, durationMs);
            trackBookmark(response.headers());
            extractRateLimit(response.headers());

            if (status == 429 || status >= 500) {
                Long retryAfter = parseRetryAfter(response.headers().firstValue("Retry-After").orElse(null));
                Long delay = retryDelay(status, retryAfter, idempotent, attempt);
                if (canRetry && delay != null) {
                    logger.warn("HTTP {} on {} {}, retrying in {}ms", status, method, url, delay);
                    debug("HTTP " + status + ", retrying in " + delay + "ms");
                    discard(response.body());
                    sleep(delay);
                    continue;
                }
            }

            if (status >= 400) {
                throw errorFromBody(response, method, req.getPath());
            }
            return new Outcome<>(response);
        }
    }

    /** Milliseconds before the next attempt, or null when this response must not be retried. */
    private Long retryDelay(int status, Long retryAfterSeconds, boolean idempotent, int attempt) {
        if (retryAfterSeconds != null) {
            long ms = retryAfterSeconds * 1000;
            if (ms > maxDelay) return null;
            // Rate-limit 429s are issued before any handler runs, so every method may
            // repeat. A 503 can come from inside a handler (a workspace moving
            // mid-request), so only idempotent methods repeat on it.
            if (status == 429 || idempotent) return ms;
            return null;
        }
        // A 429 without Retry-After is a business limit that waiting will not clear.
        if (status == 429) return null;
        return idempotent ? backoff(attempt) : null;
    }

    private java.net.http.HttpRequest build(HttpRequest req, URI uri, long timeoutMs) {
        java.net.http.HttpRequest.Builder b = java.net.http.HttpRequest.newBuilder(uri)
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", "dosya-java/" + SdkVersion.VERSION);
        String mark = bookmark.get();
        if (readYourWrites && mark != null) b.header(BOOKMARK_HEADER, mark);

        java.net.http.HttpRequest.BodyPublisher publisher;
        if (req.getRawBody() != null) {
            publisher = req.getRawBody();
        } else if (req.getBody() != null) {
            b.header("Content-Type", "application/json");
            publisher = java.net.http.HttpRequest.BodyPublishers.ofString(bodyGson.toJson(req.getBody()), StandardCharsets.UTF_8);
        } else if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod()) || "PATCH".equals(req.getMethod())) {
            publisher = java.net.http.HttpRequest.BodyPublishers.noBody();
        } else {
            publisher = null;
        }
        if (req.getHeaders() != null) req.getHeaders().forEach(b::header);
        // The JDK's own response timeout as well as the future's: on JDK 11-15 cancelling the
        // future does not abort the exchange, but this timeout does.
        if (timeoutMs > 0) b.timeout(Duration.ofMillis(timeoutMs));

        if (publisher != null) b.method(req.getMethod(), publisher);
        else if ("GET".equals(req.getMethod())) b.GET();
        else if ("DELETE".equals(req.getMethod()) && req.getBody() == null) b.DELETE();
        else b.method(req.getMethod(), java.net.http.HttpRequest.BodyPublishers.noBody());
        return b.build();
    }

    private String buildUrl(String path, Map<String, String> query) {
        StringBuilder sb = new StringBuilder(baseUrl).append(path);
        if (query != null && !query.isEmpty()) {
            char sep = '?';
            for (Map.Entry<String, String> e : query.entrySet()) {
                sb.append(sep)
                        .append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
                sep = '&';
            }
        }
        return sb.toString();
    }

    private static <T> HttpResponse<T> await(CompletableFuture<HttpResponse<T>> future, long timeoutMs)
            throws TimeoutException, IOException {
        try {
            return timeoutMs > 0 ? future.get(timeoutMs, TimeUnit.MILLISECONDS) : future.get();
        } catch (TimeoutException e) {
            future.cancel(true);
            // A response that still arrives must not hold its connection open.
            future.thenAccept(r -> discard(r.body()));
            throw e;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new DosyaNetworkException("Request interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.http.HttpTimeoutException) {
                TimeoutException te = new TimeoutException(cause.getMessage());
                te.initCause(cause);
                throw te;
            }
            if (cause instanceof IOException) throw (IOException) cause;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IOException(cause);
        }
    }

    private DosyaApiException errorFromBody(HttpResponse<?> response, String method, String path) {
        Object body = response.body();
        String text;
        if (body instanceof byte[]) {
            byte[] bytes = (byte[]) body;
            text = new String(bytes, 0, Math.min(bytes.length, MAX_ERROR_BODY), StandardCharsets.UTF_8);
        } else if (body instanceof InputStream) {
            text = readLimited((InputStream) body);
        } else {
            text = "";
        }
        return apiErrorFromText(response.statusCode(), response.headers(), method, path, text);
    }

    private DosyaApiException apiErrorFromText(int status, HttpHeaders headers, String method, String path, String text) {
        JsonObject json = null;
        try {
            JsonElement parsed = text.isEmpty() ? null : JsonParser.parseString(text);
            if (parsed != null && parsed.isJsonObject()) json = parsed.getAsJsonObject();
        } catch (Exception ignored) {
            // Not JSON (an HTML error page from a proxy).
        }
        String message = json != null && json.has("error") && json.get("error").isJsonPrimitive()
                ? json.get("error").getAsString()
                : "HTTP " + status + (text.isEmpty() ? "" : ": " + text.substring(0, Math.min(text.length(), 200)));
        return new DosyaApiException(status, message, text, headers.firstValue("X-Request-Id").orElse(null), null,
                json != null ? machineCode(json) : null, details(json),
                parseRetryAfter(headers.firstValue("Retry-After").orElse(null)), method, path);
    }

    private DosyaApiException apiError(int status, HttpHeaders headers, HttpRequest req, String message,
                                       String raw, @Nullable Throwable cause) {
        JsonObject json = null;
        try {
            JsonElement parsed = JsonParser.parseString(raw);
            if (parsed.isJsonObject()) json = parsed.getAsJsonObject();
        } catch (Exception ignored) {
            // Leave json null.
        }
        return new DosyaApiException(status, message, raw, headers.firstValue("X-Request-Id").orElse(null), cause,
                json != null ? machineCode(json) : null, details(json),
                parseRetryAfter(headers.firstValue("Retry-After").orElse(null)), req.getMethod(), req.getPath());
    }

    private static String errorString(JsonObject json) {
        JsonElement e = json.get("error");
        return e != null && e.isJsonPrimitive() ? e.getAsString() : "Unknown error";
    }

    private static @Nullable String machineCode(JsonObject json) {
        for (String key : new String[] {"code", "error_code"}) {
            JsonElement e = json.get(key);
            if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) return e.getAsString();
        }
        JsonElement error = json.get("error");
        // Several routes put the machine code in `error` itself (folder_locked, version_conflict).
        if (error != null && error.isJsonPrimitive() && MACHINE_CODE.matcher(error.getAsString()).matches()) {
            return error.getAsString();
        }
        return null;
    }

    private static JsonObject details(@Nullable JsonObject json) {
        JsonObject out = new JsonObject();
        if (json == null) return out;
        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            if (!"ok".equals(e.getKey()) && !"error".equals(e.getKey())) out.add(e.getKey(), e.getValue());
        }
        return out;
    }

    /** Parses {@code Retry-After} (delta-seconds or HTTP-date) into seconds. */
    static @Nullable Long parseRetryAfter(@Nullable String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.matches("\\d+")) {
            // Cap absurd values so millisecond arithmetic cannot overflow.
            return trimmed.length() > 9 ? 999_999_999L : Long.parseLong(trimmed);
        }
        try {
            ZonedDateTime date = ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME);
            long seconds = (date.toInstant().toEpochMilli() - System.currentTimeMillis() + 999) / 1000;
            return Math.max(0, seconds);
        } catch (Exception e) {
            return null;
        }
    }

    private void trackBookmark(HttpHeaders headers) {
        headers.firstValue(BOOKMARK_HEADER).ifPresent(value ->
                // Bookmarks sort lexically by commit position; never move backwards.
                bookmark.accumulateAndGet(value, (current, next) ->
                        current == null || next.compareTo(current) > 0 ? next : current));
    }

    private void extractRateLimit(HttpHeaders headers) {
        if (onRateLimit == null) return;
        String limit = headers.firstValue("X-RateLimit-Limit").orElse(null);
        String remaining = headers.firstValue("X-RateLimit-Remaining").orElse(null);
        String reset = headers.firstValue("X-RateLimit-Reset").orElse(null);
        if (limit != null && remaining != null && reset != null) {
            try {
                onRateLimit.accept(new RateLimitInfo(Integer.parseInt(limit), Integer.parseInt(remaining), Long.parseLong(reset)));
            } catch (NumberFormatException ignored) {
                // Malformed headers are not worth failing a request over.
            }
        }
    }

    private void debug(String message) {
        if (debugFn != null) debugFn.accept(message);
    }

    private static void discard(Object body) {
        if (body instanceof InputStream) {
            try {
                ((InputStream) body).close();
            } catch (IOException ignored) {
                // Nothing to recover.
            }
        }
    }

    private static String readLimited(InputStream in) {
        if (in == null) return "";
        try (InputStream is = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while (out.size() < MAX_ERROR_BODY && (n = is.read(buf)) != -1) out.write(buf, 0, n);
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
