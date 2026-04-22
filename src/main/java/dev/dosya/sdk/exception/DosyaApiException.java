package dev.dosya.sdk.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Thrown when the Dosya API returns an error response.
 *
 * <p>Carries structured context: HTTP status code, error message from the API,
 * raw response body, and server request ID (if available).
 *
 * @since 0.1.0
 */
public final class DosyaApiException extends DosyaException {

    private final int status;
    private final String errorMessage;
    private final String raw;
    private final String requestId;

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
        super("[" + status + "] " + errorMessage + (requestId != null ? " (request_id=" + requestId + ")" : ""), cause);
        this.status = status;
        this.errorMessage = errorMessage;
        this.raw = raw;
        this.requestId = requestId;
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
}
