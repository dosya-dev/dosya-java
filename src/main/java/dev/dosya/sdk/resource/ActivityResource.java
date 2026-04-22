package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ActivityListResponse;
import dev.dosya.sdk.model.ListActivityParams;

public class ActivityResource {

    private final DosyaHttpClient http;

    public ActivityResource(DosyaHttpClient http) {
        this.http = http;
    }

    public ActivityListResponse list(ListActivityParams params) {
        return http.requestAs(
                HttpRequest.get("/api/activity")
                        .query("workspace_id", params.getWorkspaceId())
                        .query("page", params.getPage())
                        .query("per_page", params.getPerPage())
                        .query("category", params.getCategory())
                        .query("action", params.getAction())
                        .query("user_id", params.getUserId()),
                ActivityListResponse.class);
    }
}
