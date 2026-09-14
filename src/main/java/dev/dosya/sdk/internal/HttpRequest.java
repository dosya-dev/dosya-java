package dev.dosya.sdk.internal;

import java.net.http.HttpRequest.BodyPublisher;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An API request as the resources describe it. Internal to the SDK.
 */
public final class HttpRequest {

    /** When a failed attempt may be repeated. */
    public enum Retry {
        /**
         * Default. Any method repeats on a 429 carrying {@code Retry-After}. Other
         * 5xx, timeouts and network errors repeat only for GET, HEAD, PUT and DELETE.
         */
        AUTO,
        /** A single attempt. */
        NEVER,
        /** Every retryable failure, for every method. */
        ALWAYS
    }

    private final String method;
    private final String path;
    private Map<String, String> query;
    private Object body;
    private BodyPublisher rawBody;
    private Map<String, String> headers;
    private boolean manualRedirect;
    private boolean envelope = true;
    private Retry retry = Retry.AUTO;
    private Long timeoutMs;

    public HttpRequest(String method, String path) {
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with \"/\": " + path);
        }
        this.method = method;
        this.path = path;
    }

    public static HttpRequest get(String path) { return new HttpRequest("GET", path); }
    public static HttpRequest post(String path) { return new HttpRequest("POST", path); }
    public static HttpRequest put(String path) { return new HttpRequest("PUT", path); }
    public static HttpRequest patch(String path) { return new HttpRequest("PATCH", path); }
    public static HttpRequest delete(String path) { return new HttpRequest("DELETE", path); }

    /** Adds a query parameter. Null values are skipped; the key is sent as given. */
    public HttpRequest query(String key, Object value) {
        if (value != null && !"".equals(value)) {
            if (query == null) query = new LinkedHashMap<>();
            query.put(key, String.valueOf(value));
        }
        return this;
    }

    /**
     * JSON body. A {@code Map} is sent with its keys as given (use the API's snake_case
     * names); any other object is serialised with lower_case_with_underscores field naming.
     */
    public HttpRequest body(Object body) {
        this.body = body;
        return this;
    }

    /** Body sent verbatim. */
    public HttpRequest rawBody(byte[] bytes) {
        this.rawBody = java.net.http.HttpRequest.BodyPublishers.ofByteArray(bytes);
        return this;
    }

    /** Body sent verbatim from a publisher (files, streams, multipart). */
    public HttpRequest rawBody(BodyPublisher publisher) {
        this.rawBody = publisher;
        return this;
    }

    public HttpRequest header(String key, String value) {
        if (value == null) return this;
        if (headers == null) headers = new LinkedHashMap<>();
        headers.put(key, value);
        return this;
    }

    /** Do not follow redirects; the response is returned as-is. */
    public HttpRequest manualRedirect(boolean manualRedirect) {
        this.manualRedirect = manualRedirect;
        return this;
    }

    /** Whether a success carries the {@code {"ok": true}} envelope. Default true. */
    public HttpRequest envelope(boolean envelope) {
        this.envelope = envelope;
        return this;
    }

    public HttpRequest retry(Retry retry) {
        this.retry = retry;
        return this;
    }

    /** Per-attempt timeout override in milliseconds. */
    public HttpRequest timeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getQuery() { return query; }
    public Object getBody() { return body; }
    public BodyPublisher getRawBody() { return rawBody; }
    public Map<String, String> getHeaders() { return headers; }
    public boolean isManualRedirect() { return manualRedirect; }
    public boolean isEnvelope() { return envelope; }
    public Retry getRetry() { return retry; }
    public Long getTimeoutMs() { return timeoutMs; }
}
