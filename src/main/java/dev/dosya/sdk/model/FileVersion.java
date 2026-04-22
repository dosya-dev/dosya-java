package dev.dosya.sdk.model;

public class FileVersion {

    private String id;
    private int versionNumber;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private String uploadedBy;
    private String uploaderName;
    private long createdAt;

    private FileVersion() {}

    public String getId() { return id; }
    public int getVersionNumber() { return versionNumber; }
    public long getSizeBytes() { return sizeBytes; }
    public String getMimeType() { return mimeType; }
    public String getExtension() { return extension; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploaderName() { return uploaderName; }
    public long getCreatedAt() { return createdAt; }
}
