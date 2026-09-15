package dev.dosya.sdk.model;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One recent workspace event on the team page.
 *
 * @since 0.3.0
 */
public final class TeamActivityItem {

    private String id;
    private String action;
    private String metadata;
    private JsonElement meta;
    private long createdAt;
    private String userName;
    private String userId;
    private String avatarUrl;

    private TeamActivityItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getAction() { return action; }
    /** Raw JSON text of the event metadata, or null. */
    public @Nullable String getMetadata() { return metadata; }
    /** Parsed metadata with keys as the API sent them, or null. */
    public @Nullable JsonElement getMeta() { return meta == null || meta.isJsonNull() ? null : meta; }
    public long getCreatedAt() { return createdAt; }
    public @Nullable String getUserName() { return userName; }
    public @Nullable String getUserId() { return userId; }
    public @Nullable String getAvatarUrl() { return avatarUrl; }
}
