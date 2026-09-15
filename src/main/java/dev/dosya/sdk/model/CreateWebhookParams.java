package dev.dosya.sdk.model;

import dev.dosya.sdk.webhook.WebhookEventType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameters for registering a webhook endpoint.
 *
 * <pre>{@code
 * new CreateWebhookParams("ws_1", "https://hooks.example.com/dosya",
 *         WebhookEventType.FILE_UPLOADED, WebhookEventType.FILE_DELETED)
 *     .description("CI trigger");
 * }</pre>
 *
 * @since 0.3.0
 */
public final class CreateWebhookParams {

    private final String workspaceId;
    private final String url;
    private final List<WebhookEventType> events;
    private String description;

    /**
     * @param workspaceId the workspace to register the endpoint in
     * @param url         a public {@code https://} URL; private, loopback and non-https targets are refused (400)
     * @param events      non-empty; duplicates are collapsed by the API
     */
    public CreateWebhookParams(@NotNull String workspaceId, @NotNull String url,
                               @NotNull Collection<WebhookEventType> events) {
        this.workspaceId = workspaceId;
        this.url = url;
        this.events = new ArrayList<>(events);
    }

    /** Varargs form of {@link #CreateWebhookParams(String, String, Collection)}. */
    public CreateWebhookParams(@NotNull String workspaceId, @NotNull String url,
                               @NotNull WebhookEventType... events) {
        this(workspaceId, url, Arrays.asList(events));
    }

    /** Optional free-text description. */
    public @NotNull CreateWebhookParams description(@Nullable String description) {
        this.description = description;
        return this;
    }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUrl() { return url; }
    public @NotNull List<WebhookEventType> getEvents() { return java.util.Collections.unmodifiableList(events); }
    public @Nullable String getDescription() { return description; }

    /** The request body with the API's wire names. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        body.put("url", url);
        body.put("events", wireEvents(events));
        if (description != null) body.put("description", description);
        return body;
    }

    static List<String> wireEvents(Collection<WebhookEventType> events) {
        List<String> out = new ArrayList<>();
        for (WebhookEventType e : events) out.add(e.value());
        return out;
    }
}
