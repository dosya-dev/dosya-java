package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * A presigned download link plus the file's size, name and region.
 *
 * @since 0.3.0
 */
public final class DownloadLink {

    private final String url;
    private final long size;
    private final String name;
    private final String region;
    private final long expiresAt;

    public DownloadLink(@NotNull String url, long size, @NotNull String name, @NotNull String region, long expiresAt) {
        this.url = url;
        this.size = size;
        this.name = name;
        this.region = region;
        this.expiresAt = expiresAt;
    }

    /** Presigned storage URL, a bearer credential. Fetch it without an Authorization header. */
    public @NotNull String getUrl() { return url; }
    /** Bytes of the requested version. */
    public long getSize() { return size; }
    public @NotNull String getName() { return name; }
    public @NotNull String getRegion() { return region; }
    /** Unix seconds after which the URL stops working (computed client-side from the ttl). */
    public long getExpiresAt() { return expiresAt; }
}
