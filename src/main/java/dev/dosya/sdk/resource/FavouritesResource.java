package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.FavouriteTarget;
import dev.dosya.sdk.model.FavouritesList;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The caller's own starred folders and files, per workspace.
 *
 * <p>API keys: {@link #list(String)} needs the {@code read} scope, {@link #add} and
 * {@link #remove} need {@code full}. A workspace-pinned key can only reach its workspace.
 *
 * @since 0.3.0
 */
public final class FavouritesResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code FavouritesResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public FavouritesResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists the caller's favourite folders and files in a workspace. Folder-confined
     * members are refused (403).
     *
     * @param workspaceId the workspace id
     * @return the favourite folders and files, each sorted by name
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FavouritesList list(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/favourites").query("workspace_id", workspaceId),
                FavouritesList.class);
    }

    /**
     * Stars a folder or a file. 409 when it is already a favourite; 404 when it is
     * missing or in the trash.
     *
     * @param target the folder or file to star
     * @return the new favourite's id ({@code fav_...})
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String add(@NotNull FavouriteTarget target) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", target.getWorkspaceId());
        if (target.getFolderId() != null) body.put("folder_id", target.getFolderId());
        if (target.getFileId() != null) body.put("file_id", target.getFileId());
        JsonObject resp = http.request(HttpRequest.post("/api/favourites").body(body));
        return resp.get("id").getAsString();
    }

    /**
     * Unstars a folder or a file. Succeeds even when it was not a favourite.
     *
     * @param target the folder or file to unstar
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void remove(@NotNull FavouriteTarget target) {
        http.request(HttpRequest.delete("/api/favourites")
                .query("workspace_id", target.getWorkspaceId())
                .query("folder_id", target.getFolderId())
                .query("file_id", target.getFileId()));
    }
}
