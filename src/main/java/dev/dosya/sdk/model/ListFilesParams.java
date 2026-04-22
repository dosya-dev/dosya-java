package dev.dosya.sdk.model;

public class ListFilesParams {

    private final String workspaceId;
    private String folderId;
    private String filter;
    private String sort;
    private String q;
    private Boolean deleted;
    private Boolean hidden;
    private Integer page;
    private Integer perPage;

    public ListFilesParams(String workspaceId) {
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

    public String getWorkspaceId() { return workspaceId; }
    public String getFolderId() { return folderId; }
    public String getFilter() { return filter; }
    public String getSort() { return sort; }
    public String getQ() { return q; }
    public Boolean getDeleted() { return deleted; }
    public Boolean getHidden() { return hidden; }
    public Integer getPage() { return page; }
    public Integer getPerPage() { return perPage; }
}
