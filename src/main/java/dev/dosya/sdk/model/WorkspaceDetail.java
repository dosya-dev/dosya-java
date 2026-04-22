package dev.dosya.sdk.model;

public class WorkspaceDetail {

    private String id;
    private String name;
    private String slug;
    private String iconInitials;
    private String iconColor;
    private String iconUrl;
    private String ownerId;
    private long storageUsedBytes;
    private long createdAt;

    private WorkspaceDetail() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getIconInitials() { return iconInitials; }
    public String getIconColor() { return iconColor; }
    public String getIconUrl() { return iconUrl; }
    public String getOwnerId() { return ownerId; }
    public long getStorageUsedBytes() { return storageUsedBytes; }
    public long getCreatedAt() { return createdAt; }
}
