package dev.dosya.sdk.model;

public class UploadInitResponse {

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

    public String getSessionId() { return sessionId; }
    public String getUploadUrl() { return uploadUrl; }
    public String getWorkspaceId() { return workspaceId; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType; }
    public String getExtension() { return extension; }
    public String getRegion() { return region; }
    public Resumable getResumable() { return resumable; }

    public static class Resumable {
        private int partSize;
        private int totalParts;
        private String partUploadUrl;
        private String completeUrl;
        private String statusUrl;

        private Resumable() {}

        public Resumable(int partSize, int totalParts, String partUploadUrl,
                         String completeUrl, String statusUrl) {
            this.partSize = partSize;
            this.totalParts = totalParts;
            this.partUploadUrl = partUploadUrl;
            this.completeUrl = completeUrl;
            this.statusUrl = statusUrl;
        }

        public int getPartSize() { return partSize; }
        public int getTotalParts() { return totalParts; }
        public String getPartUploadUrl() { return partUploadUrl; }
        public String getCompleteUrl() { return completeUrl; }
        public String getStatusUrl() { return statusUrl; }
    }
}
