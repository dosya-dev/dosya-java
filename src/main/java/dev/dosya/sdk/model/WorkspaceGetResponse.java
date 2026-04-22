package dev.dosya.sdk.model;

public class WorkspaceGetResponse {

    private WorkspaceDetail workspace;
    private WorkspaceSettings settings;
    private String roleId;
    private boolean isOwner;

    private WorkspaceGetResponse() {}

    public WorkspaceDetail getWorkspace() { return workspace; }
    public WorkspaceSettings getSettings() { return settings; }
    public String getRoleId() { return roleId; }
    public boolean isOwner() { return isOwner; }
}
