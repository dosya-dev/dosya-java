package dev.dosya.sdk;

/**
 * Interceptor for observing HTTP requests and responses made by the SDK.
 *
 * <p>Implement this interface to add custom telemetry, logging, or metrics collection.
 * Register it via {@link DosyaClientOptions#interceptor(DosyaInterceptor)}.
 *
 * <p>Implementations must be thread-safe. Methods are called from the thread
 * performing the HTTP request, which may be a thread-pool thread during multipart uploads.
 *
 * @since 0.1.0
 */
public interface DosyaInterceptor {

    /**
     * Called before each HTTP request is sent.
     *
     * @param method the HTTP method (GET, POST, PUT, DELETE)
     * @param url    the full request URL including query parameters
     */
    void beforeRequest(String method, String url);

    /**
     * Called after each HTTP response is received.
     *
     * @param method     the HTTP method (GET, POST, PUT, DELETE)
     * @param url        the full request URL including query parameters
     * @param statusCode the HTTP response status code
     * @param requestId  the server-returned request ID from the {@code X-Request-Id} header, or null
     * @param durationMs the request duration in milliseconds
     */
    void afterResponse(String method, String url, int statusCode,
                       String requestId, long durationMs);
}
