package dev.dosya.sdk.model;

public class SearchParams {

    private final String workspaceId;
    private final String q;
    private Integer page;
    private Integer perPage;

    public SearchParams(String workspaceId, String q) {
        this.workspaceId = workspaceId;
        this.q = q;
    }

    public SearchParams page(int page) { this.page = page; return this; }
    public SearchParams perPage(int perPage) { this.perPage = perPage; return this; }

    public String getWorkspaceId() { return workspaceId; }
    public String getQ() { return q; }
    public Integer getPage() { return page; }
    public Integer getPerPage() { return perPage; }
}
