package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A folder row in a folder, trash, hidden or group listing.
 *
 * @since 0.1.0
 */
public final class FolderListItem {

    private String id;
    private String name;
    private long createdAt;
    private long updatedAt;
    private Long deletedAt;
    private int fileCount;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private int isSynced;
    private String origin;
    private String uploaderName;
    private int shareCount;
    private int commentCount;
    private long totalSizeBytes;
    private Long contentUpdatedAt;
    private String region;
    private Boolean isTrashRoot;
    private Long trashedSizeBytes;

    private FolderListItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }
    /** Unix seconds. @since 0.3.0 */
    public long getUpdatedAt() { return updatedAt; }
    /** Unix seconds; trash view only. @since 0.3.0 */
    public @Nullable Long getDeletedAt() { return deletedAt; }
    /** Direct children: files plus subfolders (trash view: trashed files). */
    public int getFileCount() { return fileCount; }

    /**
     * The raw lock mode wire value.
     *
     * @deprecated use {@link #getLockModeValue()}.
     */
    @Deprecated
    public @Nullable String getLockMode() { return lockMode; }

    /** The lock mode; {@link LockMode#NONE} when absent or unknown. @since 0.3.0 */
    public @NotNull LockMode getLockModeValue() { return Modes.lock(lockMode); }

    public int getIsHidden() { return isHidden; }

    /**
     * The raw hidden mode wire value.
     *
     * @deprecated use {@link #getHiddenModeValue()}.
     */
    @Deprecated
    public @Nullable String getHiddenMode() { return hiddenMode; }

    /** The hidden mode; {@link HiddenMode#NONE} when absent or unknown. @since 0.3.0 */
    public @NotNull HiddenMode getHiddenModeValue() { return Modes.hidden(hiddenMode); }

    public int getIsSynced() { return isSynced; }
    /** @since 0.3.0 */
    public @Nullable String getOrigin() { return origin; }
    /** Display name of the folder's creator, or null. @since 0.3.0 */
    public @Nullable String getUploaderName() { return uploaderName; }
    /** @since 0.3.0 */
    public int getShareCount() { return shareCount; }
    /** @since 0.3.0 */
    public int getCommentCount() { return commentCount; }
    /** Recursive size of live contents (trash view: bytes trashed with the folder). @since 0.3.0 */
    public long getTotalSizeBytes() { return totalSizeBytes; }
    /** Latest change anywhere inside the folder (unix seconds). @since 0.3.0 */
    public @Nullable Long getContentUpdatedAt() { return contentUpdatedAt; }
    /** One region code, {@code "multi"} when mixed, null when empty or in the trash. @since 0.3.0 */
    public @Nullable String getRegion() { return region; }
    /** Trash view only: true for folders the user deleted directly, null outside the trash. @since 0.3.0 */
    public @Nullable Boolean getIsTrashRoot() { return isTrashRoot; }
    /** Top-level trash view only. @since 0.3.0 */
    public @Nullable Long getTrashedSizeBytes() { return trashedSizeBytes; }
}
