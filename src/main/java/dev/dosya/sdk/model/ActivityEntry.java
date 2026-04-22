package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single activity log entry in a workspace.
 *
 * @since 0.1.0
 */
public final class ActivityEntry {

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

    public @NotNull String getId() { return id; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUserId() { return userId; }
    public @NotNull String getUserName() { return userName; }
    public @NotNull String getAction() { return action; }
    public @NotNull String getResourceType() { return resourceType; }
    public @NotNull String getResourceId() { return resourceId; }
    public @Nullable String getResourceName() { return resourceName; }
    public @Nullable String getMetadata() { return metadata; }
    public long getCreatedAt() { return createdAt; }
}
