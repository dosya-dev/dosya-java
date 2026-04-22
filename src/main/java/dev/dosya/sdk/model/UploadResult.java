package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * The result of a successful file upload, containing the uploaded file detail and session ID.
 *
 * @since 0.1.0
 */
public final class UploadResult {

    private final UploadedFile file;
    private final String sessionId;

    public UploadResult(@NotNull UploadedFile file, @NotNull String sessionId) {
        this.file = file;
        this.sessionId = sessionId;
    }

    public @NotNull UploadedFile getFile() { return file; }
    public @NotNull String getSessionId() { return sessionId; }

    /**
     * Details of the file that was uploaded.
     *
     * @since 0.1.0
     */
    public static final class UploadedFile {
        private String id;
        private String name;
        private long sizeBytes;
        private String mimeType;
        private String extension;
        private String region;
        private int version;
        private long createdAt;

        private UploadedFile() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        public long getSizeBytes() { return sizeBytes; }
        public @NotNull String getMimeType() { return mimeType; }
        public @NotNull String getExtension() { return extension; }
        public @NotNull String getRegion() { return region; }
        public int getVersion() { return version; }
        public long getCreatedAt() { return createdAt; }
    }
}
