package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Response returned when retrieving a single workspace, including its settings and role info.
 *
 * @since 0.1.0
 */
public final class WorkspaceGetResponse {

    private WorkspaceDetail workspace;
    private WorkspaceSettings settings;
    private String roleId;
    private boolean isOwner;

    private WorkspaceGetResponse() {}

    public @NotNull WorkspaceDetail getWorkspace() { return workspace; }
    public @NotNull WorkspaceSettings getSettings() { return settings; }
    public @NotNull String getRoleId() { return roleId; }
    public boolean isOwner() { return isOwner; }
}
