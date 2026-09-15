package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One file request, as returned by {@code fileRequests().get()}.
 *
 * @since 0.1.0
 */
public final class FileRequestDetail {

    private String id;
    private String workspaceId;
    private String token;
    private String url;
    private String title;
    private String message;
    private boolean isPasswordProtected;
    private Long expiresAt;
    private String allowedExtensions;
    private Long maxFileSizeBytes;
    private Integer maxFiles;
    private int uploadCount;
    private boolean isRevoked;
    private long createdAt;
    private String folderId;
    private String folderName;
    private String createdByName;

    private FileRequestDetail() {}

    public @NotNull String getId() { return id; }
    /** @since 0.3.0 */
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getToken() { return token; }
    /** Public upload page. */
    public @NotNull String getUrl() { return url; }
    public @Nullable String getTitle() { return title; }
    public @Nullable String getMessage() { return message; }
    /** @since 0.3.0 (was {@code int getIsPasswordProtected()}) */
    public boolean isPasswordProtected() { return isPasswordProtected; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    /** @since 0.3.0 */
    public @Nullable String getAllowedExtensions() { return allowedExtensions; }
    /** @since 0.3.0 */
    public @Nullable Long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    /** @since 0.3.0 */
    public @Nullable Integer getMaxFiles() { return maxFiles; }
    public int getUploadCount() { return uploadCount; }
    /** @since 0.3.0 */
    public boolean isRevoked() { return isRevoked; }
    public long getCreatedAt() { return createdAt; }
    /** @since 0.3.0 */
    public @Nullable String getFolderId() { return folderId; }
    /** @since 0.3.0 */
    public @Nullable String getFolderName() { return folderName; }
    /** @since 0.3.0 */
    public @Nullable String getCreatedByName() { return createdByName; }
}
