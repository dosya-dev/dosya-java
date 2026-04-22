package dev.dosya.sdk.model;

public class ShareLinkDetail {

    private String id;
    private String fileId;
    private String fileName;
    private String token;
    private int isPasswordProtected;
    private Long expiresAt;
    private int downloadCount;
    private long createdAt;
    private String createdBy;
    private String status;

    private ShareLinkDetail() {}

    public String getId() { return id; }
    public String getFileId() { return fileId; }
    public String getFileName() { return fileName; }
    public String getToken() { return token; }
    public int getIsPasswordProtected() { return isPasswordProtected; }
    public Long getExpiresAt() { return expiresAt; }
    public int getDownloadCount() { return downloadCount; }
    public long getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getStatus() { return status; }
}
