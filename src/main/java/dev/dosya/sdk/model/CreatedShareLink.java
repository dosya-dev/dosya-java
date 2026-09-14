package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A share link as returned right after it is created.
 *
 * @since 0.3.0
 */
public final class CreatedShareLink {

    private String id;
    private String token;
    private String url;
    private LockMode lockMode;
    private ShareAccessMode accessMode;
    private Long expiresAt;
    private long createdAt;
    private Integer fileCount;

    private CreatedShareLink() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    /** Public URL to hand to recipients. */
    public @NotNull String getUrl() { return url; }
    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    public @NotNull ShareAccessMode getAccessMode() { return accessMode != null ? accessMode : ShareAccessMode.PUBLIC; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }
    /** Files in the link, for bundles; null otherwise. */
    public @Nullable Integer getFileCount() { return fileCount; }
}
