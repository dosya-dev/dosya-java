package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Summary information about a workspace as returned in listing responses.
 *
 * @since 0.1.0
 */
public final class WorkspaceListItem {

    private String id;
    private String name;
    private String slug;
    private String iconInitials;
    private String iconColor;
    private String ownerId;
    private String role;
    private String roleId;
    private int memberCount;
    private long storageUsedBytes;
    private long createdAt;

    private WorkspaceListItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @NotNull String getSlug() { return slug; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
    public @NotNull String getOwnerId() { return ownerId; }
    public @NotNull String getRole() { return role; }
    public @NotNull String getRoleId() { return roleId; }
    public int getMemberCount() { return memberCount; }
    public long getStorageUsedBytes() { return storageUsedBytes; }
    public long getCreatedAt() { return createdAt; }
}
