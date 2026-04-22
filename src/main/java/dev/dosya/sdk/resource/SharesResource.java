package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.SharesListResponse;
import org.jetbrains.annotations.NotNull;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for managing share links in Dosya workspaces.
 *
 * <p>This resource handles listing active share links and revoking them.</p>
 *
 * @since 0.1.0
 */
public final class SharesResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code SharesResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public SharesResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists all share links in a workspace along with summary statistics.
     *
     * @param workspaceId the workspace whose share links to list
     * @return the response containing share links and stats
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull SharesListResponse list(@NotNull String workspaceId) {
        return http.requestAs(
                HttpRequest.get("/api/shares").query("workspace_id", workspaceId),
                SharesListResponse.class);
    }

    /**
     * Revokes an active share link, making it inaccessible.
     *
     * @param linkId the unique identifier of the share link to revoke
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void revoke(@NotNull String linkId) {
        http.request(HttpRequest.post("/api/shares/" + encode(linkId) + "/revoke"));
    }
}
