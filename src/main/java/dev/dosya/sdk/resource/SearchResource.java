package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.SearchParams;
import dev.dosya.sdk.model.SearchResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Searches files, folders, share links and file requests by name.
 *
 * @since 0.1.0
 */
public final class SearchResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code SearchResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public SearchResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Searches files, folders, share links and file requests by name. Add {@code ext:pdf} at the
     * start or end of the query to filter files by extension. Result sets the key's member cannot
     * see (no {@code access_files}, no share permission) come back empty rather than 403.
     */
    public @NotNull SearchResponse query(@NotNull SearchParams params) {
        return http.requestAs(
                HttpRequest.get("/api/search")
                        .query("workspace_id", params.getWorkspaceId())
                        .query("q", params.getQ())
                        .query("page", params.getPage())
                        .query("per_page", params.getPerPage()),
                SearchResponse.class);
    }

    /** Searches with default paging. See {@link #query(SearchParams)}. @since 0.3.0 */
    public @NotNull SearchResponse query(@NotNull String workspaceId, @NotNull String q) {
        return query(new SearchParams(workspaceId, q));
    }
}
