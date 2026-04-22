package dev.dosya.sdk.internal;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.DosyaClientOptions;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaNetworkException;
import dev.dosya.sdk.model.RateLimitInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class DosyaHttpClient {

    private final String baseUrl;
    private final String apiKey;
    private final int maxRetries;
    private final long baseDelay;
    private final long maxDelay;
    private final long timeout;
    private final Consumer<RateLimitInfo> onRateLimit;
    private final Consumer<String> debugFn;
    private final Gson gson;
    private final Random random = new Random();

    public DosyaHttpClient(DosyaClientOptions options) {
        this.baseUrl = options.getBaseUrl().replaceAll("/+$", "");
        this.apiKey = options.getApiKey();
        this.maxRetries = options.getMaxRetries();
        this.baseDelay = options.getBaseDelay();
        this.maxDelay = options.getMaxDelay();
        this.timeout = options.getTimeout();
        this.onRateLimit = options.getOnRateLimit();
        this.debugFn = options.getDebug();
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
                    "Invalid JSON response: " + body.substring(0, Math.min(body.length(), 200)));
        }

        if (!json.has("ok") || !json.get("ok").getAsBoolean()) {
            String error = json.has("error") ? json.get("error").getAsString() : "Unknown error";
            throw new DosyaApiException(result.statusCode, error, body);
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
            try {
                if (debugFn != null) {
                    debugFn.accept(req.getMethod() + " " + url +
                            " (attempt " + (attempt + 1) + "/" + (maxRetries + 1) + ")");
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod(req.getMethod());
                conn.setConnectTimeout((int) timeout);
                conn.setReadTimeout((int) timeout);
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("User-Agent", "dosya-java/0.1.0");

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
                    OutputStream os = conn.getOutputStream();
                    os.write(req.getRawBody());
                    os.flush();
                    os.close();
                } else if (req.getBody() != null) {
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    String jsonBody = gson.toJson(req.getBody());
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBody.getBytes("UTF-8"));
                    os.flush();
                    os.close();
                }

                int statusCode = conn.getResponseCode();
                if (debugFn != null) {
                    debugFn.accept(req.getMethod() + " " + url + " -> " + statusCode);
                }

                extractRateLimit(conn);

                if (statusCode == 429) {
                    String retryAfter = conn.getHeaderField("Retry-After");
                    long delay = retryAfter != null
                            ? Long.parseLong(retryAfter) * 1000
                            : getDelay(attempt);
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
                    if (debugFn != null) {
                        debugFn.accept("Server error " + statusCode + ", retrying in " + delay + "ms");
                    }
                    sleep(delay);
                    continue;
                }

                if (req.isRawResponse()) {
                    String location = conn.getHeaderField("Location");
                    return new HttpResult(statusCode, location != null ? location : "", conn);
                }

                String responseBody = readStream(
                        statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream());
                return new HttpResult(statusCode, responseBody, null);

            } catch (IOException e) {
                lastError = e;
                if (attempt < maxRetries) {
                    long delay = getDelay(attempt);
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
                try {
                    sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (Exception e) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                }
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
            onRateLimit.accept(new RateLimitInfo(
                    Integer.parseInt(limit),
                    Integer.parseInt(remaining),
                    Long.parseLong(reset)
            ));
        }
    }

    private long getDelay(int attempt) {
        long delay = baseDelay * (1L << attempt);
        long jitter = (long) (delay * 0.2 * random.nextDouble());
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        is.close();
        return baos.toString("UTF-8");
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static class HttpResult {
        public final int statusCode;
        public final String body;
        public final HttpURLConnection connection;

        public HttpResult(int statusCode, String body, HttpURLConnection connection) {
            this.statusCode = statusCode;
            this.body = body;
            this.connection = connection;
        }
    }
}
