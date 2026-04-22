package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for managing workspaces in Dosya.
 *
 * <p>This resource handles listing, creating, retrieving, updating,
 * and deleting workspaces, as well as updating workspace settings.</p>
 *
 * @since 0.1.0
 */
public final class WorkspacesResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code WorkspacesResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public WorkspacesResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists all workspaces accessible by the authenticated user.
     *
     * @return the list of workspace items
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<WorkspaceListItem> list() {
        JsonObject resp = http.request(HttpRequest.get("/api/workspaces"));
        Type listType = new TypeToken<List<WorkspaceListItem>>() {}.getType();
        return http.fromJson(resp.get("workspaces"), listType);
    }

    /**
     * Retrieves detailed information about a workspace, including its settings.
     *
     * @param workspaceId the unique identifier of the workspace
     * @return the response containing workspace detail, settings, and role information
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull WorkspaceGetResponse get(@NotNull String workspaceId) {
        return http.requestAs(
                HttpRequest.get("/api/workspaces/" + encode(workspaceId)),
                WorkspaceGetResponse.class);
    }

    /**
     * Creates a new workspace.
     *
     * @param params the creation parameters including name and optional icon/region settings
     * @return the created workspace detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull WorkspaceDetail create(@NotNull CreateWorkspaceParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", params.getName());
        if (params.getIconInitials() != null) body.put("icon_initials", params.getIconInitials());
        if (params.getIconColor() != null) body.put("icon_color", params.getIconColor());
        if (params.getDefaultRegion() != null) body.put("default_region", params.getDefaultRegion());
        JsonObject resp = http.request(HttpRequest.post("/api/workspaces").body(body));
        return http.fromJson(resp.get("workspace"), WorkspaceDetail.class);
    }

    /**
     * Updates an existing workspace's name, icon, or default region.
     *
     * @param workspaceId the unique identifier of the workspace to update
     * @param params      the update parameters (only non-null fields are applied)
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void update(@NotNull String workspaceId, @NotNull UpdateWorkspaceParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (params.getName() != null) body.put("name", params.getName());
        if (params.getIconInitials() != null) body.put("icon_initials", params.getIconInitials());
        if (params.getIconColor() != null) body.put("icon_color", params.getIconColor());
        if (params.getDefaultRegion() != null) body.put("default_region", params.getDefaultRegion());
        http.request(HttpRequest.put("/api/workspaces/" + encode(workspaceId)).body(body));
    }

    /**
     * Replaces the settings for a workspace.
     *
     * @param workspaceId the unique identifier of the workspace
     * @param settings    the new workspace settings
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void updateSettings(@NotNull String workspaceId, @NotNull WorkspaceSettings settings) {
        http.request(HttpRequest.put("/api/workspaces/" + encode(workspaceId) + "/settings")
                .body(settings));
    }

    /**
     * Permanently deletes a workspace and all its contents.
     *
     * @param workspaceId the unique identifier of the workspace to delete
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void delete(@NotNull String workspaceId) {
        http.request(HttpRequest.delete("/api/workspaces/" + encode(workspaceId)));
    }
}
