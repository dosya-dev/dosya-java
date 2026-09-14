package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * Options for {@code upload().resume(...)}.
 *
 * @since 0.3.0
 */
public final class UploadResumeOptions {

    private Integer concurrency;
    private Consumer<UploadProgress> onProgress;
    private Long sourceModifiedAt;
    private Long sourceCreatedAt;

    /** Parts in flight. Default 3. Streams always use 1. */
    public UploadResumeOptions concurrency(int concurrency) {
        if (concurrency < 1) throw new IllegalArgumentException("concurrency must be a positive integer, got " + concurrency);
        this.concurrency = concurrency;
        return this;
    }

    /** Progress callback; the first event is seeded from the parts the server already has. */
    public UploadResumeOptions onProgress(@Nullable Consumer<UploadProgress> onProgress) { this.onProgress = onProgress; return this; }

    /** Sent on complete as {@code X-Dosya-Source-Mtime}. */
    public UploadResumeOptions sourceModifiedAt(@Nullable Instant at) { this.sourceModifiedAt = at != null ? at.getEpochSecond() : null; return this; }

    /** Sent on complete as {@code X-Dosya-Source-Ctime}. */
    public UploadResumeOptions sourceCreatedAt(@Nullable Instant at) { this.sourceCreatedAt = at != null ? at.getEpochSecond() : null; return this; }

    public @Nullable Integer getConcurrency() { return concurrency; }
    public @Nullable Consumer<UploadProgress> getOnProgress() { return onProgress; }
    /** Unix seconds, or null. */
    public @Nullable Long getSourceModifiedAt() { return sourceModifiedAt; }
    /** Unix seconds, or null. */
    public @Nullable Long getSourceCreatedAt() { return sourceCreatedAt; }
}
