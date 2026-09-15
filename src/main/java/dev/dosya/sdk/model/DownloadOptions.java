package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Options for the download methods. Each method reads only the options it documents;
 * the rest are ignored.
 *
 * @since 0.3.0
 */
public final class DownloadOptions {

    private Integer version;
    private String unlockToken;
    private Integer ttl;
    private Long rangeStart;
    private Long rangeEnd;
    private String ifNoneMatch;
    private boolean attachment;

    /** A specific version (1-based). Omit for the current one. */
    public DownloadOptions version(@Nullable Integer version) {
        if (version != null && version < 1) throw new IllegalArgumentException("version must be 1 or greater, got " + version);
        this.version = version;
        return this;
    }

    /** Unlock token from {@code files().unlock()}, required for {@code full_lock} files. */
    public DownloadOptions unlockToken(@Nullable String unlockToken) { this.unlockToken = unlockToken; return this; }

    /** Presigned link lifetime in seconds. Default 300, max 3600 (the server clamps). */
    public DownloadOptions ttl(@Nullable Integer ttl) { this.ttl = ttl; return this; }

    /** Fetch from {@code start} to the end of the file (sent as a {@code Range} header). */
    public DownloadOptions range(long start) {
        if (start < 0) throw new IllegalArgumentException("range start must be non-negative, got " + start);
        this.rangeStart = start;
        this.rangeEnd = null;
        return this;
    }

    /** Fetch the inclusive byte range {@code start..end} (sent as a {@code Range} header). */
    public DownloadOptions range(long start, long end) {
        if (start < 0) throw new IllegalArgumentException("range start must be non-negative, got " + start);
        if (end < start) throw new IllegalArgumentException("range end must be >= start, got " + end);
        this.rangeStart = start;
        this.rangeEnd = end;
        return this;
    }

    /** Sent as {@code If-None-Match} by {@code raw()}; a match answers 304. */
    public DownloadOptions ifNoneMatch(@Nullable String etag) { this.ifNoneMatch = etag; return this; }

    /** {@code archiveEntry()} only: serve as an attachment (applies the download gates). */
    public DownloadOptions attachment(boolean attachment) { this.attachment = attachment; return this; }

    public @Nullable Integer getVersion() { return version; }
    public @Nullable String getUnlockToken() { return unlockToken; }
    public @Nullable Integer getTtl() { return ttl; }
    public @Nullable Long getRangeStart() { return rangeStart; }
    public @Nullable Long getRangeEnd() { return rangeEnd; }
    public @Nullable String getIfNoneMatch() { return ifNoneMatch; }
    public boolean isAttachment() { return attachment; }

    /** The {@code Range} header value, or null when no range is set. */
    public @Nullable String rangeHeader() {
        if (rangeStart == null) return null;
        return "bytes=" + rangeStart + "-" + (rangeEnd != null ? rangeEnd : "");
    }
}
