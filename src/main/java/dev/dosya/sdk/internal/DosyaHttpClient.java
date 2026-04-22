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
import dev.dosya.sdk.model.RateLimitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Internal HTTP client that handles request execution, retries, and serialization.
 *
 * <p>This class is internal to the SDK and should not be used directly by consumers.
 *
 * @since 0.1.0
 */
public final class DosyaHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DosyaHttpClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final int maxRetries;
    private final long baseDelay;
    private final long maxDelay;
    private final long connectTimeout;
    private final long readTimeout;
    private final Consumer<RateLimitInfo> onRateLimit;
    private final Consumer<String> debugFn;
    private final DosyaInterceptor interceptor;
    private final Gson gson;

    public DosyaHttpClient(DosyaClientOptions options) {
        this.baseUrl = options.getBaseUrl().replaceAll("/+$", "");
        this.apiKey = options.getApiKey();
        this.maxRetries = options.getMaxRetries();
        this.baseDelay = options.getBaseDelay();
        this.maxDelay = options.getMaxDelay();
        this.connectTimeout = options.getConnectTimeout();
        this.readTimeout = options.getReadTimeout();
        this.onRateLimit = options.getOnRateLimit();
        this.debugFn = options.getDebug();
        this.interceptor = options.getInterceptor();
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public JsonObject request(HttpRequest req) {
        HttpResult result = doFetch(req);

        String body = result.body;
        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            throw new DosyaApiException(result.statusCode,
                    "Invalid JSON response: " + body.substring(0, Math.min(body.length(), 200)),
                    null, result.requestId, e);
        }

        if (!json.has("ok") || !json.get("ok").getAsBoolean()) {
            String error = json.has("error") ? json.get("error").getAsString() : "Unknown error";
            throw new DosyaApiException(result.statusCode, error, body, result.requestId);
        }

        json.remove("ok");
        return json;
    }

    public <T> T requestAs(HttpRequest req, Class<T> type) {
        JsonObject response = request(req);
        return gson.fromJson(response, type);
    }

    public <T> T requestAs(HttpRequest req, Type type) {
        JsonObject response = request(req);
        return gson.fromJson(response, type);
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

    public HttpResult doFetch(HttpRequest req) {
        String url = buildUrl(req.getPath(), req.getQuery());
        Exception lastError = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            long startTime = System.currentTimeMillis();
            try {
                logger.debug("{} {} (attempt {}/{})", req.getMethod(), url, attempt + 1, maxRetries + 1);
                if (debugFn != null) {
                    debugFn.accept(req.getMethod() + " " + url +
                            " (attempt " + (attempt + 1) + "/" + (maxRetries + 1) + ")");
                }
                if (interceptor != null) {
                    interceptor.beforeRequest(req.getMethod(), url);
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod(req.getMethod());
                conn.setConnectTimeout((int) connectTimeout);
                conn.setReadTimeout((int) readTimeout);
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("User-Agent", "dosya-java/0.2.0");

                if (req.isRawResponse()) {
                    conn.setInstanceFollowRedirects(false);
                }

                if (req.getHeaders() != null) {
                    for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }

                if (req.getRawBody() != null) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(req.getRawBody());
                        os.flush();
                    }
                } else if (req.getBody() != null) {
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    String jsonBody = gson.toJson(req.getBody());
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.getBytes("UTF-8"));
                        os.flush();
                    }
                }

                int statusCode = conn.getResponseCode();
                String requestId = conn.getHeaderField("X-Request-Id");
                long durationMs = System.currentTimeMillis() - startTime;

                logger.debug("{} {} -> {} ({}ms)", req.getMethod(), url, statusCode, durationMs);
                if (debugFn != null) {
                    debugFn.accept(req.getMethod() + " " + url + " -> " + statusCode);
                }
                if (interceptor != null) {
                    interceptor.afterResponse(req.getMethod(), url, statusCode, requestId, durationMs);
                }

                extractRateLimit(conn);

                if (statusCode == 429) {
                    long delay = parseRetryAfter(conn.getHeaderField("Retry-After"), attempt);
                    logger.warn("Rate limited on {} {}, retrying in {}ms", req.getMethod(), url, delay);
                    if (debugFn != null) {
                        debugFn.accept("Rate limited, retrying in " + delay + "ms");
                    }
                    if (attempt < maxRetries) {
                        sleep(delay);
                        continue;
                    }
                }

                if (statusCode >= 500 && attempt < maxRetries) {
                    long delay = getDelay(attempt);
                    logger.warn("Server error {} on {} {}, retrying in {}ms", statusCode, req.getMethod(), url, delay);
                    if (debugFn != null) {
                        debugFn.accept("Server error " + statusCode + ", retrying in " + delay + "ms");
                    }
                    sleep(delay);
                    continue;
                }

                if (req.isRawResponse()) {
                    String location = conn.getHeaderField("Location");
                    return new HttpResult(statusCode, location != null ? location : "", conn, requestId);
                }

                String responseBody = readStream(
                        statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream());
                return new HttpResult(statusCode, responseBody, null, requestId);

            } catch (IOException e) {
                lastError = e;
                if (attempt < maxRetries) {
                    long delay = getDelay(attempt);
                    logger.warn("Request error on {} {}: {}, retrying in {}ms", req.getMethod(), url, e.getMessage(), delay);
                    if (debugFn != null) {
                        debugFn.accept("Request error: " + e.getMessage() + ", retrying in " + delay + "ms");
                    }
                    sleep(delay);
                    continue;
                }
            }
        }

        throw new DosyaNetworkException(
                "Request to " + req.getMethod() + " " + req.getPath() +
                        " failed after " + (maxRetries + 1) + " attempts",
                lastError);
    }

    public Gson getGson() {
        return gson;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getBaseDelay() {
        return baseDelay;
    }

    public long getMaxDelay() {
        return maxDelay;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private String buildUrl(String path, Map<String, String> query) {
        StringBuilder sb = new StringBuilder(baseUrl);
        if (!path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(path);

        if (query != null && !query.isEmpty()) {
            sb.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(encode(entry.getKey()));
                sb.append("=");
                sb.append(encode(entry.getValue()));
                first = false;
            }
        }
        return sb.toString();
    }

    private void extractRateLimit(HttpURLConnection conn) {
        if (onRateLimit == null) return;
        String limit = conn.getHeaderField("X-RateLimit-Limit");
        String remaining = conn.getHeaderField("X-RateLimit-Remaining");
        String reset = conn.getHeaderField("X-RateLimit-Reset");
        if (limit != null && remaining != null && reset != null) {
            try {
                onRateLimit.accept(new RateLimitInfo(
                        Integer.parseInt(limit),
                        Integer.parseInt(remaining),
                        Long.parseLong(reset)
                ));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private long parseRetryAfter(String retryAfter, int attempt) {
        if (retryAfter == null) {
            return getDelay(attempt);
        }
        try {
            return Long.parseLong(retryAfter) * 1000;
        } catch (NumberFormatException e) {
            // RFC 7231: Retry-After can also be an HTTP-date
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = fmt.parse(retryAfter);
                long delayMs = date.getTime() - System.currentTimeMillis();
                return Math.max(delayMs, 0);
            } catch (ParseException pe) {
                return getDelay(attempt);
            }
        }
    }

    private long getDelay(int attempt) {
        long delay = baseDelay * (1L << attempt);
        long jitter = (long) (delay * 0.2 * ThreadLocalRandom.current().nextDouble());
        return Math.min(delay + jitter, maxDelay);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DosyaNetworkException("Request interrupted", e);
        }
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "{}";
        try (InputStream in = is) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return baos.toString("UTF-8");
        }
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is always supported", e);
        }
    }

    public static class HttpResult {
        public final int statusCode;
        public final String body;
        public final HttpURLConnection connection;
        public final String requestId;

        public HttpResult(int statusCode, String body, HttpURLConnection connection, String requestId) {
            this.statusCode = statusCode;
            this.body = body;
            this.connection = connection;
            this.requestId = requestId;
        }
    }
}
