package dev.dosya.sdk.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a file upload operation fails.
 *
 * <p>Contains the upload session ID and, for multipart uploads, the part number
 * that failed. This information can be used to resume or diagnose the upload.</p>
 *
 * @since 0.1.0
 */
public final class DosyaUploadException extends DosyaException {

    private final String sessionId;
    private final Integer partNumber;

    /**
     * Creates a new upload exception with a session ID.
     *
     * @param message   the detail message
     * @param sessionId the upload session ID
     */
    public DosyaUploadException(@NotNull String message, @NotNull String sessionId) {
        this(message, sessionId, null, null);
    }

    /**
     * Creates a new upload exception with a session ID and part number.
     *
     * @param message    the detail message
     * @param sessionId  the upload session ID
     * @param partNumber the part number that failed, or {@code null}
     */
    public DosyaUploadException(@NotNull String message, @NotNull String sessionId, @Nullable Integer partNumber) {
        this(message, sessionId, partNumber, null);
    }

    /**
     * Creates a new upload exception with a session ID, part number, and cause.
     *
     * @param message    the detail message
     * @param sessionId  the upload session ID
     * @param partNumber the part number that failed, or {@code null}
     * @param cause      the underlying cause, or {@code null}
     */
    public DosyaUploadException(@NotNull String message, @NotNull String sessionId,
                                @Nullable Integer partNumber, @Nullable Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.partNumber = partNumber;
    }

    /**
     * Returns the upload session ID associated with this error.
     *
     * @return the session ID
     */
    public @NotNull String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the part number that failed, or {@code null} if not applicable.
     *
     * @return the failed part number, or {@code null}
     */
    public @Nullable Integer getPartNumber() {
        return partNumber;
    }
}
