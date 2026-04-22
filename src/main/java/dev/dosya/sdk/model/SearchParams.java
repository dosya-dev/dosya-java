package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for searching files, folders, shares, and file requests.
 *
 * <pre>{@code
 * SearchParams params = new SearchParams("ws_123", "report")
 *     .page(1)
 *     .perPage(10);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class SearchParams {

    private final String workspaceId;
    private final String q;
    private Integer page;
    private Integer perPage;

    public SearchParams(@NotNull String workspaceId, @NotNull String q) {
        this.workspaceId = workspaceId;
        this.q = q;
    }

    public SearchParams page(int page) { this.page = page; return this; }
    public SearchParams perPage(int perPage) { this.perPage = perPage; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getQ() { return q; }
    public @Nullable Integer getPage() { return page; }
    public @Nullable Integer getPerPage() { return perPage; }
}
