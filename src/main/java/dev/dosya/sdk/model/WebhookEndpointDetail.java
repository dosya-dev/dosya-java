package dev.dosya.sdk.model;

import dev.dosya.sdk.webhook.WebhookEventType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A single webhook endpoint from {@code GET /api/webhooks/:id}, with failure-health bookkeeping.
 * The signing secret is never included.
 *
 * @since 0.3.0
 */
public final class WebhookEndpointDetail {

    private String id;
    private String workspaceId;
    private String url;
    private List<String> events;
    private String description;
    private boolean active;
    private int consecutiveFailures;
    private Long disabledAt;
    private Long firstFailureAt;
    private int healthNotified;
    private String createdBy;
    private long createdAt;
    private long updatedAt;

    private WebhookEndpointDetail() {}

    /** {@code whep_...} */
    public @NotNull String getId() { return id; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUrl() { return url; }

    /** Subscribed event types as wire values (e.g. {@code file.uploaded}). */
    public @NotNull List<String> getEvents() { return events == null ? Collections.emptyList() : Collections.unmodifiableList(events); }

    /** Subscribed event types this SDK version knows; unknown values are skipped. */
    public @NotNull List<WebhookEventType> getEventTypes() { return WebhookEndpoint.eventTypes(events); }

    public @Nullable String getDescription() { return description; }

    /** False when disabled by {@code update(active=false)} or auto-disabled after 15 consecutive failures. */
    public boolean isActive() { return active; }
    public int getConsecutiveFailures() { return consecutiveFailures; }

    /** Unix seconds when the endpoint was auto-disabled; cleared when re-activated. */
    public @Nullable Long getDisabledAt() { return disabledAt; }

    /** Unix seconds of the first failure in the current run of failures. */
    public @Nullable Long getFirstFailureAt() { return firstFailureAt; }

    /** {@code 0} healthy, {@code 1} failure warning sent, {@code 2} auto-disabled notice sent. */
    public int getHealthNotified() { return healthNotified; }
    public @NotNull String getCreatedBy() { return createdBy; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
}
