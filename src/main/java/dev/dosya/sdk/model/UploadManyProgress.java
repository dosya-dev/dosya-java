package dev.dosya.sdk.model;

/**
 * Progress of {@code upload().many(...)}.
 *
 * @since 0.3.0
 */
public final class UploadManyProgress {

    private final int filesCompleted;
    private final int filesFailed;
    private final int totalFiles;
    private final long bytesUploaded;
    private final long totalBytes;

    public UploadManyProgress(int filesCompleted, int filesFailed, int totalFiles, long bytesUploaded, long totalBytes) {
        this.filesCompleted = filesCompleted;
        this.filesFailed = filesFailed;
        this.totalFiles = totalFiles;
        this.bytesUploaded = bytesUploaded;
        this.totalBytes = totalBytes;
    }

    public int getFilesCompleted() { return filesCompleted; }
    public int getFilesFailed() { return filesFailed; }
    public int getTotalFiles() { return totalFiles; }
    /** Bytes of finished files (large files also report within-file progress). */
    public long getBytesUploaded() { return bytesUploaded; }
    public long getTotalBytes() { return totalBytes; }
}
