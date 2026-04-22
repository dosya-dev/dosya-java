package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed information about a file request.
 *
 * @since 0.1.0
 */
public final class FileRequestDetail {

    private String id;
    private String token;
    private String url;
    private String title;
    private String message;
    private int isPasswordProtected;
    private Long expiresAt;
    private int uploadCount;
    private long createdAt;

    private FileRequestDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    public @NotNull String getUrl() { return url; }
    public @Nullable String getTitle() { return title; }
    public @Nullable String getMessage() { return message; }
    public int getIsPasswordProtected() { return isPasswordProtected; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public int getUploadCount() { return uploadCount; }
    public long getCreatedAt() { return createdAt; }
}
