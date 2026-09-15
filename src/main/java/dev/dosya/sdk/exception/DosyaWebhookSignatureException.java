package dev.dosya.sdk.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a webhook delivery fails signature verification.
 *
 * @since 0.3.0
 */
public final class DosyaWebhookSignatureException extends DosyaException {

    public DosyaWebhookSignatureException(@NotNull String message) {
        super(message);
    }
}
