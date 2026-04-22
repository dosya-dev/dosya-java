package dev.dosya.sdk.model;

public class ActivityEntry {

    private String id;
    private String workspaceId;
    private String userId;
    private String userName;
    private String action;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String metadata;
    private long createdAt;

    private ActivityEntry() {}

    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getMetadata() { return metadata; }
    public long getCreatedAt() { return createdAt; }
}
