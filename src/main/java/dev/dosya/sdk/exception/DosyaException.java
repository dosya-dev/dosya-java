package dev.dosya.sdk.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base runtime exception for all errors originating from the Dosya SDK.
 *
 * <p>This is the superclass of all Dosya-specific exceptions. Catching this type
 * will handle any error thrown by the SDK, including API errors, network errors,
 * and upload errors.</p>
 *
 * @since 0.1.0
 */
public class DosyaException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the detail message
     */
    public DosyaException(@NotNull String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause, or {@code null}
     */
    public DosyaException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
