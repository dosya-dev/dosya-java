package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.SearchParams;
import dev.dosya.sdk.model.SearchResponse;

public class SearchResource {

    private final DosyaHttpClient http;

    public SearchResource(DosyaHttpClient http) {
        this.http = http;
    }

    public SearchResponse query(SearchParams params) {
        return http.requestAs(
                HttpRequest.get("/api/search")
                        .query("workspace_id", params.getWorkspaceId())
                        .query("q", params.getQ())
                        .query("page", params.getPage())
                        .query("per_page", params.getPerPage()),
                SearchResponse.class);
    }
}
