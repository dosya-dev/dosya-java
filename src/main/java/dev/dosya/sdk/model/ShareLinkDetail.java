package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed information about a share link for a file.
 *
 * @since 0.1.0
 */
public final class ShareLinkDetail {

    private String id;
    private String fileId;
    private String fileName;
    private String token;
    private int isPasswordProtected;
    private Long expiresAt;
    private int downloadCount;
    private long createdAt;
    private String createdBy;
    private String status;

    private ShareLinkDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getFileId() { return fileId; }
    public @NotNull String getFileName() { return fileName; }
    public @NotNull String getToken() { return token; }
    public int getIsPasswordProtected() { return isPasswordProtected; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public int getDownloadCount() { return downloadCount; }
    public long getCreatedAt() { return createdAt; }
    public @NotNull String getCreatedBy() { return createdBy; }
    public @NotNull String getStatus() { return status; }
}
