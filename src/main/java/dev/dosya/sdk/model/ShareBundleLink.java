package dev.dosya.sdk.model;

public class ShareBundleLink {

    private String id;
    private String token;
    private String url;
    private int fileCount;
    private Long expiresAt;
    private long createdAt;

    private ShareBundleLink() {}

    public String getId() { return id; }
    public String getToken() { return token; }
    public String getUrl() { return url; }
    public int getFileCount() { return fileCount; }
    public Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }
}
