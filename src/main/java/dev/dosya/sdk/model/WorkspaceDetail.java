package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A workspace row, as returned by {@code workspaces().get()}.
 *
 * @since 0.1.0
 */
public final class WorkspaceDetail {

    private String id;
    private String name;
    private String slug;
    private String iconInitials;
    private String iconColor;
    private String iconImageUrl;
    private String ownerId;
    private String defaultRegion;
    private Long storageUsedBytes;
    private long createdAt;

    private WorkspaceDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @NotNull String getSlug() { return slug; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
    /** Storage key of an uploaded icon, or null. @since 0.3.0 */
    public @Nullable String getIconImageUrl() { return iconImageUrl; }
    public @NotNull String getOwnerId() { return ownerId; }
    /** The storage location, fixed at creation. @since 0.3.0 */
    public @Nullable String getDefaultRegion() { return defaultRegion; }
    public long getStorageUsedBytes() { return storageUsedBytes != null ? storageUsedBytes : 0L; }
    public long getCreatedAt() { return createdAt; }

    /**
     * The uploaded icon's storage key.
     *
     * @deprecated the API field is {@code icon_image_url}; use {@link #getIconImageUrl()}.
     */
    @Deprecated
    public @Nullable String getIconUrl() { return iconImageUrl; }
}
