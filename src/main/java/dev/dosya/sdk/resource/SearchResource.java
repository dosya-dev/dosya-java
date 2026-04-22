package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.SearchParams;
import dev.dosya.sdk.model.SearchResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Provides operations for searching files, folders, shares, and file requests in Dosya.
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
     * Searches for files, folders, shares, and file requests matching the given query.
     *
     * @param params the search parameters including workspace ID, query string, and pagination
     * @return the search response containing matched files, folders, shares, and file requests
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
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
}
