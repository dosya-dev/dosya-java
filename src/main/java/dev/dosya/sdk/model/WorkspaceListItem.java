package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A workspace the caller belongs to, as returned by {@code workspaces().list()}.
 *
 * @since 0.1.0
 */
public final class WorkspaceListItem {

    private String id;
    private String name;
    private String slug;
    private String iconInitials;
    private String iconColor;
    private String iconImageUrl;
    private String ownerId;
    private String defaultRegion;
    private String plan;
    private long createdAt;
    private Long storageUsedBytes;
    private String roleId;
    private long joinedAt;
    @SerializedName("require_2fa")
    private boolean require2fa;
    private boolean disablePasswordLogin;
    private Double maxTotalStorageGb;
    private WorkspaceStorage storage;

    private WorkspaceListItem() {}

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
    /** @since 0.3.0 */
    public @Nullable String getPlan() { return plan; }
    public long getCreatedAt() { return createdAt; }
    /** Bytes stored; 0 when the API sent null. */
    public long getStorageUsedBytes() { return storageUsedBytes != null ? storageUsedBytes : 0L; }
    /** The caller's role in this workspace. */
    public @NotNull String getRoleId() { return roleId; }
    /** @since 0.3.0 */
    public long getJoinedAt() { return joinedAt; }
    /** Whether members must use two-factor authentication. @since 0.3.0 */
    public boolean isRequire2fa() { return require2fa; }
    /** @since 0.3.0 */
    public boolean isDisablePasswordLogin() { return disablePasswordLogin; }
    /** Whole-GB workspace cap, or null when uncapped. @since 0.3.0 */
    public @Nullable Double getMaxTotalStorageGb() { return maxTotalStorageGb; }
    /** Storage figures, or null if the API did not send them. @since 0.3.0 */
    public @Nullable WorkspaceStorage getStorage() { return storage; }

    /**
     * The caller's role id.
     *
     * @deprecated the API never sent {@code role}; use {@link #getRoleId()}.
     */
    @Deprecated
    public @NotNull String getRole() { return roleId; }

    /**
     * Always 0.
     *
     * @deprecated the list endpoint does not return a member count; use {@code team().list()}.
     */
    @Deprecated
    public int getMemberCount() { return 0; }
}
