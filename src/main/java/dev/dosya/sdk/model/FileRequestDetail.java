package dev.dosya.sdk.model;

public class FileRequestDetail {

    private String id;
    private String token;
    private String url;
    private String title;
    private String message;
    private int isPasswordProtected;
    private Long expiresAt;
    private int uploadCount;
    private long createdAt;

    private FileRequestDetail() {}

    public String getId() { return id; }
    public String getToken() { return token; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public int getIsPasswordProtected() { return isPasswordProtected; }
    public Long getExpiresAt() { return expiresAt; }
    public int getUploadCount() { return uploadCount; }
    public long getCreatedAt() { return createdAt; }
}
