package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A row of {@code shares().list()}: one link in a workspace.
 *
 * @since 0.3.0
 */
public final class WorkspaceShareLink {

    private String linkId;
    private String token;
    private String url;
    private ShareStatus status;
    private boolean isBundle;
    private boolean isFolder;
    private boolean isMine;
    private String displayName;
    private String fileId;
    private String fileName;
    private String folderId;
    private String folderName;
    private Long sizeBytes;
    private String extension;
    private String region;
    private LockMode lockMode;
    private ShareAccessMode accessMode;
    private boolean isPasswordProtected;
    private Integer maxDownloads;
    private int recipientCount;
    private int viewCount;
    private int downloadCount;
    private boolean isRevoked;
    private Long revokedAt;
    private Long expiresAt;
    private long sharedAt;
    private String createdBy;
    private String sharerName;

    private WorkspaceShareLink() {}

    /** Use with {@code shares().update()}, {@code analytics()} and {@code revoke()}. */
    public @NotNull String getLinkId() { return linkId; }
    public @NotNull String getToken() { return token; }
    /** Public URL. */
    public @NotNull String getUrl() { return url; }
    /** Null only when the API sends a status this SDK does not know. */
    public @Nullable ShareStatus getStatus() { return status; }
    public boolean isBundle() { return isBundle; }
    public boolean isFolder() { return isFolder; }
    /** Created by the caller. */
    public boolean isMine() { return isMine; }
    /** The folder or file name, or {@code "(deleted)"}. */
    public @NotNull String getDisplayName() { return displayName; }
    /** Null for folder links. A bundle carries its first file. */
    public @Nullable String getFileId() { return fileId; }
    public @Nullable String getFileName() { return fileName; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFolderName() { return folderName; }
    public @Nullable Long getSizeBytes() { return sizeBytes; }
    public @Nullable String getExtension() { return extension; }
    public @Nullable String getRegion() { return region; }
    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    public @NotNull ShareAccessMode getAccessMode() { return accessMode != null ? accessMode : ShareAccessMode.PUBLIC; }
    public boolean isPasswordProtected() { return isPasswordProtected; }
    /** Null = unlimited. */
    public @Nullable Integer getMaxDownloads() { return maxDownloads; }
    public int getRecipientCount() { return recipientCount; }
    public int getViewCount() { return viewCount; }
    public int getDownloadCount() { return downloadCount; }
    public boolean isRevoked() { return isRevoked; }
    public @Nullable Long getRevokedAt() { return revokedAt; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    /** When the link was created, unix seconds. */
    public long getSharedAt() { return sharedAt; }
    public @NotNull String getCreatedBy() { return createdBy; }
    public @Nullable String getSharerName() { return sharerName; }
}
