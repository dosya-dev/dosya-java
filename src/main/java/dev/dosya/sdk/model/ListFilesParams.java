package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for listing files and folders in a workspace.
 *
 * <pre>{@code
 * ListFilesParams params = new ListFilesParams("ws_123")
 *     .folderId("folder_456")
 *     .sort("name")
 *     .page(1)
 *     .perPage(20);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class ListFilesParams {

    private final String workspaceId;
    private String folderId;
    private String filter;
    private String sort;
    private String q;
    private Boolean deleted;
    private Boolean hidden;
    private Integer page;
    private Integer perPage;

    public ListFilesParams(@NotNull String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public ListFilesParams folderId(String folderId) { this.folderId = folderId; return this; }
    public ListFilesParams filter(String filter) { this.filter = filter; return this; }
    public ListFilesParams sort(String sort) { this.sort = sort; return this; }
    public ListFilesParams q(String q) { this.q = q; return this; }
    public ListFilesParams deleted(boolean deleted) { this.deleted = deleted; return this; }
    public ListFilesParams hidden(boolean hidden) { this.hidden = hidden; return this; }
    public ListFilesParams page(int page) { this.page = page; return this; }
    public ListFilesParams perPage(int perPage) { this.perPage = perPage; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFilter() { return filter; }
    public @Nullable String getSort() { return sort; }
    public @Nullable String getQ() { return q; }
    public @Nullable Boolean getDeleted() { return deleted; }
    public @Nullable Boolean getHidden() { return hidden; }
    public @Nullable Integer getPage() { return page; }
    public @Nullable Integer getPerPage() { return perPage; }
}
