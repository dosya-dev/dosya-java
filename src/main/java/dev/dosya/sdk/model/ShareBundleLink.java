package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a share bundle link that groups multiple files into a single shareable URL.
 *
 * @since 0.1.0
 */
public final class ShareBundleLink {

    private String id;
    private String token;
    private String url;
    private int fileCount;
    private Long expiresAt;
    private long createdAt;

    private ShareBundleLink() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    public @NotNull String getUrl() { return url; }
    public int getFileCount() { return fileCount; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }
}
