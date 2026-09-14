package dev.dosya.sdk.webhook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A delivered webhook event body, as returned by {@link WebhookSignature#constructEvent}.
 *
 * <p>The body is kept exactly as delivered: {@link #getData()} and {@link #getJson()}
 * use the API's snake_case keys ({@code file_id}, {@code is_new_version}, ...).
 *
 * <p>Data per type:
 * <ul>
 *   <li>{@code file.uploaded}: {@code file_id, name, size, folder_id, version, is_new_version},
 *       plus {@code test: true} on the synthetic event from {@code webhooks().test()}.</li>
 *   <li>{@code file.deleted}: {@code file_id, name, permanent} ({@code false} = moved to trash).</li>
 *   <li>{@code share.accessed}: {@code share_id, token, access_type} ({@code view},
 *       {@code download} or {@code download_all}) and {@code file_id} and/or {@code folder_id}.
 *       The {@code token} grants access to the link - treat webhook bodies as secrets.</li>
 * </ul>
 *
 * @since 0.3.0
 */
public final class WebhookEvent {

    private final JsonObject json;

    WebhookEvent(@NotNull JsonObject json) {
        this.json = json;
    }

    /** {@code evt_...}; use it for idempotency (identical across retries and redeliveries). */
    public @Nullable String getId() {
        return string(json, "id");
    }

    /** The wire event type, e.g. {@code file.uploaded}. */
    public @Nullable String getType() {
        return string(json, "type");
    }

    /** The event type, or {@code null} when this SDK version does not know it. */
    public @Nullable WebhookEventType getEventType() {
        return WebhookEventType.fromValue(getType());
    }

    /** Unix seconds when the event happened (not the delivery attempt time); 0 when absent. */
    public long getCreated() {
        JsonElement e = json.get("created");
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsLong() : 0L;
    }

    /** The workspace the event belongs to ({@code workspace_id}). */
    public @Nullable String getWorkspaceId() {
        return string(json, "workspace_id");
    }

    /** The event's {@code data} object with snake_case keys; empty when absent. */
    public @NotNull JsonObject getData() {
        JsonElement e = json.get("data");
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : new JsonObject();
    }

    /** {@code data.file_id}, or {@code null} when absent (e.g. a folder share view). */
    public @Nullable String getFileId() {
        return string(getData(), "file_id");
    }

    /** {@code data.folder_id}, or {@code null} when absent. */
    public @Nullable String getFolderId() {
        return string(getData(), "folder_id");
    }

    /** True for the synthetic event sent by {@code webhooks().test()} ({@code data.test: true}). */
    public boolean isTest() {
        JsonElement e = getData().get("test");
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() && e.getAsBoolean();
    }

    /** The whole event body as parsed. */
    public @NotNull JsonObject getJson() {
        return json;
    }

    @Override
    public String toString() {
        return "WebhookEvent{id='" + getId() + "', type='" + getType() + "', workspaceId='" + getWorkspaceId() + "'}";
    }

    private static String string(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    }
}
