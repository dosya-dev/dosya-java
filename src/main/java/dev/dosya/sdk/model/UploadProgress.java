package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the progress of an ongoing file upload.
 *
 * <p>For multipart uploads, includes the number of parts completed and total parts.
 * For single-part uploads, these fields will be {@code null}.</p>
 *
 * @since 0.1.0
 */
public final class UploadProgress {

    private final long bytesUploaded;
    private final long totalBytes;
    private final int percent;
    private final Integer partsCompleted;
    private final Integer totalParts;
    private final String status;

    public UploadProgress(long bytesUploaded, long totalBytes, int percent, @NotNull String status) {
        this(bytesUploaded, totalBytes, percent, null, null, status);
    }

    public UploadProgress(long bytesUploaded, long totalBytes, int percent,
                          @Nullable Integer partsCompleted, @Nullable Integer totalParts, @NotNull String status) {
        this.bytesUploaded = bytesUploaded;
        this.totalBytes = totalBytes;
        this.percent = percent;
        this.partsCompleted = partsCompleted;
        this.totalParts = totalParts;
        this.status = status;
    }

    public long getBytesUploaded() { return bytesUploaded; }
    public long getTotalBytes() { return totalBytes; }
    public int getPercent() { return percent; }
    public @Nullable Integer getPartsCompleted() { return partsCompleted; }
    public @Nullable Integer getTotalParts() { return totalParts; }
    public @NotNull String getStatus() { return status; }
}
