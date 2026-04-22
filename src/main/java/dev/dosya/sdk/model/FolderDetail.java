package dev.dosya.sdk.model;

public class FolderDetail {

    private String id;
    private String name;
    private String parentId;
    private String workspaceId;
    private long createdAt;

    private FolderDetail() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
    public String getWorkspaceId() { return workspaceId; }
    public long getCreatedAt() { return createdAt; }
}
