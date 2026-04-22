package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed information about a single file in a Dosya workspace.
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

    private FileDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public @NotNull String getMimeType() { return mimeType; }
    public @NotNull String getExtension() { return extension; }
    public @NotNull String getRegion() { return region; }
    public @NotNull String getUploadedBy() { return uploadedBy; }
    public @NotNull String getUploaderName() { return uploaderName; }
    public @Nullable String getFolderId() { return folderId; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public int getCurrentVersion() { return currentVersion; }
    public @Nullable String getLockMode() { return lockMode; }
    public int getIsHidden() { return isHidden; }
    public @Nullable String getHiddenMode() { return hiddenMode; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public @Nullable Long getDeletedAt() { return deletedAt; }
}
