package dev.dosya.sdk.model;

public class ListActivityParams {

    private final String workspaceId;
    private Integer page;
    private Integer perPage;
    private String category;
    private String action;
    private String userId;

    public ListActivityParams(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public ListActivityParams page(int page) { this.page = page; return this; }
    public ListActivityParams perPage(int perPage) { this.perPage = perPage; return this; }
    public ListActivityParams category(String category) { this.category = category; return this; }
    public ListActivityParams action(String action) { this.action = action; return this; }
    public ListActivityParams userId(String userId) { this.userId = userId; return this; }

    public String getWorkspaceId() { return workspaceId; }
    public Integer getPage() { return page; }
    public Integer getPerPage() { return perPage; }
    public String getCategory() { return category; }
    public String getAction() { return action; }
    public String getUserId() { return userId; }
}
