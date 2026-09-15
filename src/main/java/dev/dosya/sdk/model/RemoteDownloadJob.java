package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A remote-download job: a server-side fetch of a public URL into workspace storage.
 *
 * @since 0.3.0
 */
public final class RemoteDownloadJob {

    /** Job state. */
    public enum Status {
        QUEUED, DOWNLOADING, FINALIZING, DONE, ERROR, CANCELLED,
        /** A value this SDK version does not know; see {@link #getRawStatus()}. */
        UNKNOWN
    }

    private String id;
    private String url;
    private String filename;
    private String status;
    private long bytesTotal;
    private long bytesDone;
    private String errorCode;
    private String fileId;
    private long createdAt;

    private RemoteDownloadJob() {}

    /** {@code rdl_...} */
    public @NotNull String getId() { return id; }
    public @NotNull String getUrl() { return url; }
    public @NotNull String getFilename() { return filename; }

    public @NotNull Status getStatus() {
        if (status == null) return Status.UNKNOWN;
        switch (status) {
            case "queued": return Status.QUEUED;
            case "downloading": return Status.DOWNLOADING;
            case "finalizing": return Status.FINALIZING;
            case "done": return Status.DONE;
            case "error": return Status.ERROR;
            case "cancelled": return Status.CANCELLED;
            default: return Status.UNKNOWN;
        }
    }

    /** The status as sent, e.g. {@code downloading}. */
    public @Nullable String getRawStatus() { return status; }
    public long getBytesTotal() { return bytesTotal; }
    public long getBytesDone() { return bytesDone; }

    /**
     * Why the job failed: {@code ssrf_blocked}, {@code not_a_file}, {@code unknown_size},
     * {@code too_large}, {@code quota}, {@code workspace_moving} (transient - the job waits),
     * {@code source_changed}, {@code network} or {@code http_<status>}. {@code null} otherwise.
     */
    public @Nullable String getErrorCode() { return errorCode; }

    /** The created file's id once the status is {@code done}. */
    public @Nullable String getFileId() { return fileId; }
    public long getCreatedAt() { return createdAt; }
}
