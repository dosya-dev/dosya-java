package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single folder's metadata. Also returned for a folder sitting in the trash.
 *
 * @since 0.1.0
 */
public final class FolderDetail {

    private String id;
    private String name;
    private String parentId;
    private String workspaceId;
    private boolean isSynced;
    private boolean isDeleted;
    private String trashRootId;
    private long createdAt;
    private long updatedAt;

    private FolderDetail() {}

    public @NotNull String getId() { return id; }
    /** The real name, also for a folder in the trash. */
    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    /** Desktop sync flag. @since 0.3.0 */
    public boolean isSynced() { return isSynced; }
    /** True when the folder is in the trash. @since 0.3.0 */
    public boolean isDeleted() { return isDeleted; }
    /** Id of the folder whose deletion swept this one into the trash (itself for a trash root). @since 0.3.0 */
    public @Nullable String getTrashRootId() { return trashRootId; }
    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }
    /** Unix seconds. @since 0.3.0 */
    public long getUpdatedAt() { return updatedAt; }
}
