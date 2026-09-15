package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * The file row an upload produced.
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
        private String contentHash;
        private Boolean hashVerified;
        private String etag;

        private UploadedFile() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        /** Bytes the server measured, not the declared size. */
        public long getSizeBytes() { return sizeBytes; }
        public @NotNull String getMimeType() { return mimeType; }
        /** Lowercase with the dot (".txt"), or null when the name has none. */
        public @Nullable String getExtension() { return extension; }
        public @NotNull String getRegion() { return region; }
        /** Greater than 1 when a same-name file was adopted as a new version. */
        public int getVersion() { return version; }
        public long getCreatedAt() { return createdAt; }
        /**
         * SHA-256 hex of the bytes (single-request uploads); null for multipart.
         *
         * @since 0.3.0
         */
        public @Nullable String getContentHash() { return contentHash; }
        /**
         * True only when {@code sha256} was sent and the bytes matched it.
         *
         * @since 0.3.0
         */
        public boolean isHashVerified() { return hashVerified != null && hashVerified; }
        /**
         * Storage ETag, or null.
         *
         * @since 0.3.0
         */
        public @Nullable String getEtag() { return etag; }
    }
}
