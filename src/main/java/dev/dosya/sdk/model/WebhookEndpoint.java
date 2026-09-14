package dev.dosya.sdk.model;

import dev.dosya.sdk.webhook.WebhookEventType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A webhook endpoint as listed by {@code GET /api/webhooks}. The signing secret is never included.
 *
 * @since 0.3.0
 */
public final class WebhookEndpoint {

    private String id;
    private String workspaceId;
    private String url;
    private List<String> events;
    private String description;
    private boolean active;
    private int consecutiveFailures;
    private Long disabledAt;
    private String createdBy;
    private long createdAt;
    private long updatedAt;

    private WebhookEndpoint() {}

    /** {@code whep_...} */
    public @NotNull String getId() { return id; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUrl() { return url; }

    /** Subscribed event types as wire values (e.g. {@code file.uploaded}). */
    public @NotNull List<String> getEvents() { return events == null ? Collections.emptyList() : Collections.unmodifiableList(events); }

    /** Subscribed event types this SDK version knows; unknown values are skipped. */
    public @NotNull List<WebhookEventType> getEventTypes() { return eventTypes(events); }

    public @Nullable String getDescription() { return description; }

    /** False when disabled by {@code update(active=false)} or auto-disabled after 15 consecutive failures. */
    public boolean isActive() { return active; }
    public int getConsecutiveFailures() { return consecutiveFailures; }

    /** Unix seconds when the endpoint was auto-disabled; cleared when re-activated. */
    public @Nullable Long getDisabledAt() { return disabledAt; }
    public @NotNull String getCreatedBy() { return createdBy; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    static List<WebhookEventType> eventTypes(List<String> events) {
        if (events == null) return Collections.emptyList();
        List<WebhookEventType> out = new ArrayList<>();
        for (String e : events) {
            WebhookEventType t = WebhookEventType.fromValue(e);
            if (t != null) out.add(t);
        }
        return Collections.unmodifiableList(out);
    }
}
