package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ActivityListResponse;
import dev.dosya.sdk.model.ListActivityParams;
import org.jetbrains.annotations.NotNull;

/**
 * Reads a workspace's activity log.
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
     * One page of the workspace activity log, newest first. Needs the {@code view_activity}
     * permission.
     */
    public @NotNull ActivityListResponse list(@NotNull ListActivityParams params) {
        return http.requestAs(params.applyTo(HttpRequest.get("/api/activity")), ActivityListResponse.class);
    }

    /** The first page with no filters. See {@link #list(ListActivityParams)}. @since 0.3.0 */
    public @NotNull ActivityListResponse list(@NotNull String workspaceId) {
        return list(new ListActivityParams(workspaceId));
    }
}
