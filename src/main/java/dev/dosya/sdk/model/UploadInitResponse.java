package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Response returned when initializing an upload session.
 *
 * <p>Contains the session ID, upload URL for single-part uploads, and resumable
 * configuration for multipart uploads.</p>
 *
 * @since 0.1.0
 */
public final class UploadInitResponse {

    private String sessionId;
    private String uploadUrl;
    private String workspaceId;
    private String fileName;
    private long fileSize;
    private String mimeType;
    private String extension;
    private String region;
    private Resumable resumable;

    private UploadInitResponse() {}

    public @NotNull String getSessionId() { return sessionId; }
    public @Nullable String getUploadUrl() { return uploadUrl; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public @NotNull String getMimeType() { return mimeType; }
    public @NotNull String getExtension() { return extension; }
    public @NotNull String getRegion() { return region; }
    public @Nullable Resumable getResumable() { return resumable; }

    /**
     * Multipart upload configuration returned when the file qualifies for resumable upload.
     *
     * @since 0.1.0
     */
    public static final class Resumable {
        private int partSize;
        private int totalParts;
        private String partUploadUrl;
        private String completeUrl;
        private String statusUrl;

        private Resumable() {}

        public Resumable(int partSize, int totalParts, @NotNull String partUploadUrl,
                         @NotNull String completeUrl, @NotNull String statusUrl) {
            this.partSize = partSize;
            this.totalParts = totalParts;
            this.partUploadUrl = partUploadUrl;
            this.completeUrl = completeUrl;
            this.statusUrl = statusUrl;
        }

        public int getPartSize() { return partSize; }
        public int getTotalParts() { return totalParts; }
        public @NotNull String getPartUploadUrl() { return partUploadUrl; }
        public @NotNull String getCompleteUrl() { return completeUrl; }
        public @NotNull String getStatusUrl() { return statusUrl; }
    }
}
