package dev.dosya.sdk.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when an attempt exceeded its timeout and no retry was allowed or left.
 *
 * @since 0.3.0
 */
public final class DosyaTimeoutException extends DosyaNetworkException {

    private final long timeoutMs;

    public DosyaTimeoutException(@NotNull String message, long timeoutMs, @Nullable Throwable cause) {
        super(message, cause);
        this.timeoutMs = timeoutMs;
    }

    /** Returns the per-attempt timeout that was exceeded, in milliseconds. */
    public long getTimeoutMs() {
        return timeoutMs;
    }
}
