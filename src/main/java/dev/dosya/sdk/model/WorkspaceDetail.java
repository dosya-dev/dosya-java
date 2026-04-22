package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed information about a Dosya workspace.
 *
 * @since 0.1.0
 */
public final class WorkspaceDetail {

    private String id;
    private String name;
    private String slug;
    private String iconInitials;
    private String iconColor;
    private String iconUrl;
    private String ownerId;
    private long storageUsedBytes;
    private long createdAt;

    private WorkspaceDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @NotNull String getSlug() { return slug; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
    public @Nullable String getIconUrl() { return iconUrl; }
    public @NotNull String getOwnerId() { return ownerId; }
    public long getStorageUsedBytes() { return storageUsedBytes; }
    public long getCreatedAt() { return createdAt; }
}
