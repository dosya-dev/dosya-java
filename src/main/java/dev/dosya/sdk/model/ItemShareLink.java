package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A live share link on one file or folder.
 *
 * @since 0.3.0
 */
public final class ItemShareLink {

    private String id;
    private String token;
    private String url;
    private boolean isPasswordProtected;
    private Long expiresAt;
    private int viewCount;
    private int downloadCount;
    private boolean isRevoked;
    private LockMode lockMode;
    private long createdAt;

    private ItemShareLink() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    public @NotNull String getUrl() { return url; }
    public boolean isPasswordProtected() { return isPasswordProtected; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public int getViewCount() { return viewCount; }
    public int getDownloadCount() { return downloadCount; }
    public boolean isRevoked() { return isRevoked; }
    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    public long getCreatedAt() { return createdAt; }
}
