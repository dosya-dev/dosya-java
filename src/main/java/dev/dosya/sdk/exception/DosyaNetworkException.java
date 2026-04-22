package dev.dosya.sdk.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a network-level error occurs while communicating with the Dosya API.
 *
 * <p>This includes connection timeouts, DNS resolution failures, and other I/O errors
 * that prevent the HTTP request from completing.</p>
 *
 * @since 0.1.0
 */
public final class DosyaNetworkException extends DosyaException {

    /**
     * Creates a new network exception with the given message.
     *
     * @param message the detail message
     */
    public DosyaNetworkException(@NotNull String message) {
        super(message);
    }

    /**
     * Creates a new network exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause, or {@code null}
     */
    public DosyaNetworkException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
