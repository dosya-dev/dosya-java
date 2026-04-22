package dev.dosya.sdk.model;

public class UploadResult {

    private final UploadedFile file;
    private final String sessionId;

    public UploadResult(UploadedFile file, String sessionId) {
        this.file = file;
        this.sessionId = sessionId;
    }

    public UploadedFile getFile() { return file; }
    public String getSessionId() { return sessionId; }

    public static class UploadedFile {
        private String id;
        private String name;
        private long sizeBytes;
        private String mimeType;
        private String extension;
        private String region;
        private int version;
        private long createdAt;

        private UploadedFile() {}

        public String getId() { return id; }
        public String getName() { return name; }
        public long getSizeBytes() { return sizeBytes; }
        public String getMimeType() { return mimeType; }
        public String getExtension() { return extension; }
        public String getRegion() { return region; }
        public int getVersion() { return version; }
        public long getCreatedAt() { return createdAt; }
    }
}
