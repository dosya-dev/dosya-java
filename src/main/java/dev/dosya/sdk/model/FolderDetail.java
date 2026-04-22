package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed information about a single folder in a Dosya workspace.
 *
 * @since 0.1.0
 */
public final class FolderDetail {

    private String id;
    private String name;
    private String parentId;
    private String workspaceId;
    private long createdAt;

    private FolderDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public long getCreatedAt() { return createdAt; }
}
