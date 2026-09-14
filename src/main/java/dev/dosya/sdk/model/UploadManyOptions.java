package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Options for {@code upload().many(...)}.
 *
 * @since 0.3.0
 */
public final class UploadManyOptions {

    private Integer concurrency;
    private Consumer<UploadManyProgress> onProgress;

    /** Requests in flight (batch requests and single-file uploads). Default 3. */
    public UploadManyOptions concurrency(int concurrency) {
        if (concurrency < 1) throw new IllegalArgumentException("concurrency must be a positive integer, got " + concurrency);
        this.concurrency = concurrency;
        return this;
    }

    /** Progress callback, called from worker threads. */
    public UploadManyOptions onProgress(@Nullable Consumer<UploadManyProgress> onProgress) { this.onProgress = onProgress; return this; }

    public @Nullable Integer getConcurrency() { return concurrency; }
    public @Nullable Consumer<UploadManyProgress> getOnProgress() { return onProgress; }
}
