package dev.dosya.sdk.model;

import java.util.List;

public class ListFilesResponse {

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

    public List<FolderListItem> getFolders() { return folders; }
    public List<FileListItem> getFiles() { return files; }
    public List<Breadcrumb> getBreadcrumbs() { return breadcrumbs; }
    public String getWorkspaceId() { return workspaceId; }
    public String getFolderId() { return folderId; }
    public boolean isCanLock() { return canLock; }
    public boolean isCanHide() { return canHide; }
    public boolean isFolderViewOnly() { return folderViewOnly; }
    public Pagination getPagination() { return pagination; }
}
