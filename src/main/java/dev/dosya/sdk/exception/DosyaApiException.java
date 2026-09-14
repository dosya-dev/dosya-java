package dev.dosya.sdk.exception;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when the Dosya API answers with an error ({@code {"ok": false, "error": ...}})
 * or a non-2xx status.
 *
 * <p>Carries the HTTP status, the API's error message, a machine-readable code when
 * the API sent one (for example {@code folder_locked}, {@code version_conflict},
 * {@code quota}), any extra fields of the error body, and {@code Retry-After}.
 *
 * @since 0.1.0
 */
public final class DosyaApiException extends DosyaException {

    private final int status;
    private final String errorMessage;
    private final String raw;
    private final String requestId;
    private final String code;
    private final JsonObject details;
    private final Long retryAfterSeconds;
    private final String method;
    private final String path;

    public DosyaApiException(int status, String errorMessage) {
        this(status, errorMessage, null, null, null);
    }

    public DosyaApiException(int status, String errorMessage, @Nullable String raw) {
        this(status, errorMessage, raw, null, null);
    }

    public DosyaApiException(int status, String errorMessage, @Nullable String raw, @Nullable String requestId) {
        this(status, errorMessage, raw, requestId, null);
    }

    public DosyaApiException(int status, String errorMessage, @Nullable String raw,
                             @Nullable String requestId, @Nullable Throwable cause) {
        this(status, errorMessage, raw, requestId, cause, null, null, null, null, null);
    }

    /**
     * Full constructor used by the SDK's HTTP client.
     *
     * @since 0.3.0
     */
    public DosyaApiException(int status, String errorMessage, @Nullable String raw,
                             @Nullable String requestId, @Nullable Throwable cause,
                             @Nullable String code, @Nullable JsonObject details,
                             @Nullable Long retryAfterSeconds, @Nullable String method, @Nullable String path) {
        super("[" + status + "] " + errorMessage + (requestId != null ? " (request_id=" + requestId + ")" : ""), cause);
        this.status = status;
        this.errorMessage = errorMessage;
        this.raw = raw;
        this.requestId = requestId;
        this.code = code;
        this.details = details != null ? details : new JsonObject();
        this.retryAfterSeconds = retryAfterSeconds;
        this.method = method;
        this.path = path;
    }

    /** Returns the HTTP status code. */
    public int getStatus() {
        return status;
    }

    /** Returns the error message from the API. */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** Returns the raw response body, or null. */
    @Nullable
    public String getRaw() {
        return raw;
    }

    /** Returns the server request ID from the {@code X-Request-Id} header, or null. */
    @Nullable
    public String getRequestId() {
        return requestId;
    }

    /**
     * Returns the machine-readable error code when the API sent one, or null.
     *
     * @since 0.3.0
     */
    @Nullable
    public String getCode() {
        return code;
    }

    /**
     * Returns the extra fields of the error body (everything except {@code ok} and
     * {@code error}), for example {@code folder_id} or {@code current_version}. Keys are
     * the API's snake_case names. Never null.
     *
     * @since 0.3.0
     */
    @NotNull
    public JsonObject getDetails() {
        return details.deepCopy();
    }

    /**
     * Returns the seconds the API asked the caller to wait ({@code Retry-After}), or null.
     *
     * @since 0.3.0
     */
    @Nullable
    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /** Returns the HTTP method of the failed request, or null. @since 0.3.0 */
    @Nullable
    public String getMethod() {
        return method;
    }

    /** Returns the API path of the failed request, or null. @since 0.3.0 */
    @Nullable
    public String getPath() {
        return path;
    }
}
