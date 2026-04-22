package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ActivityListResponse;
import dev.dosya.sdk.model.ListActivityParams;
import org.jetbrains.annotations.NotNull;

/**
 * Provides operations for retrieving activity logs from Dosya workspaces.
 *
 * @since 0.1.0
 */
public final class ActivityResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code ActivityResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public ActivityResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists activity entries for a workspace, with optional filtering by category, action, or user.
     *
     * @param params the activity listing parameters including workspace ID, pagination, and filters
     * @return the response containing activity entries, members, and pagination info
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull ActivityListResponse list(@NotNull ListActivityParams params) {
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
