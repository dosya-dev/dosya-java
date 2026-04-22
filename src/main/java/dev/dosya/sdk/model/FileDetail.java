package dev.dosya.sdk.model;

public class FileDetail {

    private String id;
    private String name;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private String region;
    private String uploadedBy;
    private String uploaderName;
    private String folderId;
    private String workspaceId;
    private int currentVersion;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private long createdAt;
    private long updatedAt;
    private Long deletedAt;

    private FileDetail() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public String getMimeType() { return mimeType; }
    public String getExtension() { return extension; }
    public String getRegion() { return region; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploaderName() { return uploaderName; }
    public String getFolderId() { return folderId; }
    public String getWorkspaceId() { return workspaceId; }
    public int getCurrentVersion() { return currentVersion; }
    public String getLockMode() { return lockMode; }
    public int getIsHidden() { return isHidden; }
    public String getHiddenMode() { return hiddenMode; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public Long getDeletedAt() { return deletedAt; }
}
