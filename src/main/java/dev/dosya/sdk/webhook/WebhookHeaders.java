package dev.dosya.sdk.webhook;

/**
 * Header names dosya sets on every webhook delivery.
 *
 * @since 0.3.0
 */
public final class WebhookHeaders {

    private WebhookHeaders() {}

    /** {@code t=<unix seconds>,v1=<hex HMAC-SHA256>}; verify it with {@link WebhookSignature}. */
    public static final String SIGNATURE = "X-Dosya-Signature";

    /** {@code evt_...} - identical across retries and redeliveries; dedupe on this. */
    public static final String EVENT_ID = "X-Dosya-Event-Id";

    /** {@code file.uploaded}, {@code file.deleted} or {@code share.accessed}. */
    public static final String EVENT_TYPE = "X-Dosya-Event-Type";

    /** {@code whd_...} - unique per delivery attempt row. */
    public static final String DELIVERY_ID = "X-Dosya-Delivery-Id";
}
