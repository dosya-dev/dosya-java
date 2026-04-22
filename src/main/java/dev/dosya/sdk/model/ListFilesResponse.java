package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response returned when listing files and folders in a workspace.
 *
 * <p>Contains the folder listing, file listing, breadcrumbs for navigation,
 * and pagination metadata.</p>
 *
 * @since 0.1.0
 */
public final class ListFilesResponse {

    private List<FolderListItem> folders;
    private List<FileListItem> files;
    private List<Breadcrumb> breadcrumbs;
    private String workspaceId;
    private String folderId;
    private boolean canLock;
    private boolean canHide;
    private boolean folderViewOnly;
    private Pagination pagination;

    private ListFilesResponse() {}

    public @NotNull List<FolderListItem> getFolders() { return folders != null ? Collections.unmodifiableList(folders) : Collections.<FolderListItem>emptyList(); }
    public @NotNull List<FileListItem> getFiles() { return files != null ? Collections.unmodifiableList(files) : Collections.<FileListItem>emptyList(); }
    public @NotNull List<Breadcrumb> getBreadcrumbs() { return breadcrumbs != null ? Collections.unmodifiableList(breadcrumbs) : Collections.<Breadcrumb>emptyList(); }
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable String getFolderId() { return folderId; }
    public boolean isCanLock() { return canLock; }
    public boolean isCanHide() { return canHide; }
    public boolean isFolderViewOnly() { return folderViewOnly; }
    public @Nullable Pagination getPagination() { return pagination; }
}
