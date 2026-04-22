package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.SharesListResponse;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class SharesResource {

    private final DosyaHttpClient http;

    public SharesResource(DosyaHttpClient http) {
        this.http = http;
    }

    public SharesListResponse list(String workspaceId) {
        return http.requestAs(
                HttpRequest.get("/api/shares").query("workspace_id", workspaceId),
                SharesListResponse.class);
    }

    public void revoke(String linkId) {
        http.request(HttpRequest.post("/api/shares/" + encode(linkId) + "/revoke"));
    }
}
