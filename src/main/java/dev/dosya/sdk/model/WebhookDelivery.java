package dev.dosya.sdk.model;

import dev.dosya.sdk.webhook.WebhookEventType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One webhook delivery attempt row. The payload body is not returned.
 *
 * @since 0.3.0
 */
public final class WebhookDelivery {

    /** Delivery state. */
    public enum Status {
        PENDING, SUCCESS, FAILED,
        /** A value this SDK version does not know; see {@link #getRawStatus()}. */
        UNKNOWN
    }

    private String id;
    private String eventId;
    private String eventType;
    private String status;
    private int attempts;
    private Long nextAttemptAt;
    private Long lastAttemptAt;
    private Integer responseStatus;
    private String responseSnippet;
    private String error;
    private long createdAt;

    private WebhookDelivery() {}

    /** {@code whd_...} */
    public @NotNull String getId() { return id; }

    /** {@code evt_...}, stable across retries and redeliveries. */
    public @NotNull String getEventId() { return eventId; }

    /** The wire event type, e.g. {@code file.uploaded}. */
    public @NotNull String getEventType() { return eventType; }

    /** The event type, or {@code null} when this SDK version does not know it. */
    public @Nullable WebhookEventType getEventTypeValue() { return WebhookEventType.fromValue(eventType); }

    public @NotNull Status getStatus() {
        if ("pending".equals(status)) return Status.PENDING;
        if ("success".equals(status)) return Status.SUCCESS;
        if ("failed".equals(status)) return Status.FAILED;
        return Status.UNKNOWN;
    }

    /** The status as sent ({@code pending}, {@code success} or {@code failed}). */
    public @Nullable String getRawStatus() { return status; }
    public int getAttempts() { return attempts; }
    public @Nullable Long getNextAttemptAt() { return nextAttemptAt; }
    public @Nullable Long getLastAttemptAt() { return lastAttemptAt; }
    public @Nullable Integer getResponseStatus() { return responseStatus; }

    /** First 500 characters of the receiver's response body. */
    public @Nullable String getResponseSnippet() { return responseSnippet; }
    public @Nullable String getError() { return error; }
    public long getCreatedAt() { return createdAt; }
}
