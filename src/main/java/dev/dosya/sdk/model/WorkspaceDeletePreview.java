package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * What deleting a workspace would destroy, and what would refuse it.
 *
 * @since 0.3.0
 */
public final class WorkspaceDeletePreview {

    private String workspaceId;
    private String workspaceName;
    private long fileCount;
    private long totalBytes;
    private long folderCount;
    private long memberCount;
    private List<String> blockers;

    private WorkspaceDeletePreview() {}

    public @NotNull String getWorkspaceId() { return workspaceId; }
    /** The exact name {@code delete()} must be given. */
    public @NotNull String getWorkspaceName() { return workspaceName; }
    /** Trashed items included. */
    public long getFileCount() { return fileCount; }
    public long getTotalBytes() { return totalBytes; }
    public long getFolderCount() { return folderCount; }
    /** Members other than the owner. */
    public long getMemberCount() { return memberCount; }
    /** Reasons {@code delete()} would be refused right now: {@code has_members}, {@code last_workspace}. */
    public @NotNull List<String> getBlockers() {
        return blockers != null ? Collections.unmodifiableList(blockers) : Collections.emptyList();
    }
}
