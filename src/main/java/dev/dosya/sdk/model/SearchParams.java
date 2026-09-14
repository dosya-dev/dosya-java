package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Parameters for searching files, folders, share links and file requests.
 *
 * <pre>{@code
 * new SearchParams("ws_123", "ext:pdf report").page(1).perPage(10);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class SearchParams {

    private final String workspaceId;
    private final String q;
    private Integer page;
    private Integer perPage;

    /**
     * @param workspaceId the workspace to search
     * @param q           search text (case-insensitive substring), must not be empty. A leading or
     *                    trailing {@code ext:pdf} token restricts files to that extension
     *                    ({@code ext:pdf} alone lists every PDF); folders and file requests then
     *                    come back empty.
     */
    public SearchParams(@NotNull String workspaceId, @NotNull String q) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.q = Objects.requireNonNull(q, "q");
    }

    /** 1-based page. */
    public @NotNull SearchParams page(int page) { this.page = page; return this; }
    /** 1-100, default 50. Applied to each result set separately. */
    public @NotNull SearchParams perPage(int perPage) { this.perPage = perPage; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getQ() { return q; }
    public @Nullable Integer getPage() { return page; }
    public @Nullable Integer getPerPage() { return perPage; }
}
