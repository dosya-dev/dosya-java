package dev.dosya.sdk.model;

import dev.dosya.sdk.webhook.WebhookEventType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A webhook endpoint as returned once by {@code create()}. The {@link #getSecret() secret} is
 * never shown again (use {@code rollSecret()} to replace it).
 *
 * @since 0.3.0
 */
public final class CreatedWebhookEndpoint {

    private String id;
    private String workspaceId;
    private String url;
    private List<String> events;
    private String description;
    private boolean active;
    private String secret;
    private long createdAt;

    private CreatedWebhookEndpoint() {}

    /** {@code whep_...} */
    public @NotNull String getId() { return id; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUrl() { return url; }

    /** Subscribed event types as wire values, duplicates collapsed. */
    public @NotNull List<String> getEvents() { return events == null ? Collections.emptyList() : Collections.unmodifiableList(events); }

    /** Subscribed event types this SDK version knows. */
    public @NotNull List<WebhookEventType> getEventTypes() { return WebhookEndpoint.eventTypes(events); }

    public @Nullable String getDescription() { return description; }
    public boolean isActive() { return active; }

    /** {@code whsec_...} signing secret. Store it now. */
    public @NotNull String getSecret() { return secret; }
    public long getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "CreatedWebhookEndpoint{id='" + id + "', url='" + url + "', secret=***}";
    }
}
