package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for listing activity entries in a workspace.
 *
 * <pre>{@code
 * ListActivityParams params = new ListActivityParams("ws_123")
 *     .category("file")
 *     .action("upload")
 *     .page(1);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class ListActivityParams {

    private final String workspaceId;
    private Integer page;
    private Integer perPage;
    private String category;
    private String action;
    private String userId;

    public ListActivityParams(@NotNull String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public ListActivityParams page(int page) { this.page = page; return this; }
    public ListActivityParams perPage(int perPage) { this.perPage = perPage; return this; }
    public ListActivityParams category(String category) { this.category = category; return this; }
    public ListActivityParams action(String action) { this.action = action; return this; }
    public ListActivityParams userId(String userId) { this.userId = userId; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable Integer getPage() { return page; }
    public @Nullable Integer getPerPage() { return perPage; }
    public @Nullable String getCategory() { return category; }
    public @Nullable String getAction() { return action; }
    public @Nullable String getUserId() { return userId; }
}
