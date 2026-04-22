package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Summary information about a file as returned in listing responses.
 *
 * @since 0.1.0
 */
public final class FileListItem {

    private String id;
    private String name;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private String region;
    private String uploadedBy;
    private String uploaderName;
    private long createdAt;
    private long updatedAt;
    private Long deletedAt;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private int currentVersion;
    private int isSynced;
    private int shareCount;
    private int commentCount;

    private FileListItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public @NotNull String getMimeType() { return mimeType; }
    public @NotNull String getExtension() { return extension; }
    public @NotNull String getRegion() { return region; }
    public @NotNull String getUploadedBy() { return uploadedBy; }
    public @NotNull String getUploaderName() { return uploaderName; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public @Nullable Long getDeletedAt() { return deletedAt; }
    public @Nullable String getLockMode() { return lockMode; }
    public int getIsHidden() { return isHidden; }
    public @Nullable String getHiddenMode() { return hiddenMode; }
    public int getCurrentVersion() { return currentVersion; }
    public int getIsSynced() { return isSynced; }
    public int getShareCount() { return shareCount; }
    public int getCommentCount() { return commentCount; }
}
