package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A row of {@code fileRequests().list()}.
 *
 * @since 0.3.0
 */
public final class FileRequestListItem {

    private String id;
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

    private FileRequestListItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    /** Public upload page. */
    public @NotNull String getUrl() { return url; }
    public @Nullable String getTitle() { return title; }
    public @Nullable String getMessage() { return message; }
    public boolean isPasswordProtected() { return isPasswordProtected; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    /** Comma-separated, e.g. {@code ".pdf,.docx"}; null = any. */
    public @Nullable String getAllowedExtensions() { return allowedExtensions; }
    public @Nullable Long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public @Nullable Integer getMaxFiles() { return maxFiles; }
    public int getUploadCount() { return uploadCount; }
    public boolean isRevoked() { return isRevoked; }
    public long getCreatedAt() { return createdAt; }
    /** Destination folder, or null for the workspace root. */
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFolderName() { return folderName; }
    public @Nullable String getCreatedByName() { return createdByName; }
}
