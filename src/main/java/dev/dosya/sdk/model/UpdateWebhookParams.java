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
 * Changes to a webhook endpoint. Only the fields you set are sent and updated.
 *
 * @since 0.3.0
 */
public final class UpdateWebhookParams {

    private String url;
    private List<WebhookEventType> events;
    private Boolean active;
    private String description;
    private boolean descriptionSet;

    /** New public {@code https://} URL. */
    public @NotNull UpdateWebhookParams url(@NotNull String url) {
        this.url = url;
        return this;
    }

    /** New non-empty event subscription list. */
    public @NotNull UpdateWebhookParams events(@NotNull Collection<WebhookEventType> events) {
        this.events = new ArrayList<>(events);
        return this;
    }

    /** Varargs form of {@link #events(Collection)}. */
    public @NotNull UpdateWebhookParams events(@NotNull WebhookEventType... events) {
        return events(Arrays.asList(events));
    }

    /** {@code true} re-enables an auto-disabled endpoint and resets its failure counter. */
    public @NotNull UpdateWebhookParams active(boolean active) {
        this.active = active;
        return this;
    }

    /** New description; {@code null} clears it. */
    public @NotNull UpdateWebhookParams description(@Nullable String description) {
        this.description = description;
        this.descriptionSet = true;
        return this;
    }

    /** The request body with only the fields that were set (an explicit null description is sent). */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (url != null) body.put("url", url);
        if (events != null) body.put("events", CreateWebhookParams.wireEvents(events));
        if (active != null) body.put("active", active);
        if (descriptionSet) body.put("description", description);
        return body;
    }
}
