package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Metadata of a single file.
 *
 * @since 0.1.0
 */
public final class FileDetail {

    private String id;
    private String name;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private String region;
    private String uploadedBy;
    private String uploaderName;
    private String folderId;
    private String workspaceId;
    private int currentVersion;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private long createdAt;
    private long updatedAt;
    private Long deletedAt;
    private String contentHash;
    private String etag;
    private int shareCount;
    private int commentCount;

    private FileDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public @NotNull String getMimeType() { return mimeType; }
    public @Nullable String getExtension() { return extension; }
    public @NotNull String getRegion() { return region; }
    public @NotNull String getUploadedBy() { return uploadedBy; }
    /** Null when the uploader's account is gone. */
    public @Nullable String getUploaderName() { return uploaderName; }
    /** Null at the workspace root. */
    public @Nullable String getFolderId() { return folderId; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public int getCurrentVersion() { return currentVersion; }

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

    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }
    /** Unix seconds. */
    public long getUpdatedAt() { return updatedAt; }
    /** Unix seconds; null unless the file is in the trash. */
    public @Nullable Long getDeletedAt() { return deletedAt; }
    /** Content hash once the file has been hashed, else null. @since 0.3.0 */
    public @Nullable String getContentHash() { return contentHash; }
    /** @since 0.3.0 */
    public @Nullable String getEtag() { return etag; }
    /** @since 0.3.0 */
    public int getShareCount() { return shareCount; }
    /** @since 0.3.0 */
    public int getCommentCount() { return commentCount; }
}
