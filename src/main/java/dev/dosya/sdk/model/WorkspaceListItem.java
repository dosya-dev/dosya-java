package dev.dosya.sdk.model;

public class WorkspaceListItem {

    private String id;
    private String name;
    private String slug;
    private String iconInitials;
    private String iconColor;
    private String ownerId;
    private String role;
    private String roleId;
    private int memberCount;
    private long storageUsedBytes;
    private long createdAt;

    private WorkspaceListItem() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getIconInitials() { return iconInitials; }
    public String getIconColor() { return iconColor; }
    public String getOwnerId() { return ownerId; }
    public String getRole() { return role; }
    public String getRoleId() { return roleId; }
    public int getMemberCount() { return memberCount; }
    public long getStorageUsedBytes() { return storageUsedBytes; }
    public long getCreatedAt() { return createdAt; }
}
