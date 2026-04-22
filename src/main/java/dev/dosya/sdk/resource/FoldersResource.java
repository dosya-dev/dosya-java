package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateFolderResponse;
import dev.dosya.sdk.model.FolderDetail;
import dev.dosya.sdk.model.FolderTreeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for managing folders in Dosya workspaces.
 *
 * <p>This resource handles creating, retrieving, renaming, deleting, moving,
 * locking, unlocking, and listing folder trees.</p>
 *
 * @since 0.1.0
 */
public final class FoldersResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code FoldersResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public FoldersResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Creates a new folder in a workspace, optionally nested under a parent folder.
     *
     * @param workspaceId the workspace in which to create the folder
     * @param name        the name of the new folder
     * @param parentId    the parent folder ID, or {@code null} to create at the workspace root
     * @return the response containing the created folder detail and count
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreateFolderResponse create(@NotNull String workspaceId, @NotNull String name, @Nullable String parentId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("workspace_id", workspaceId);
        body.put("name", name);
        if (parentId != null) body.put("parent_id", parentId);
        return http.requestAs(HttpRequest.post("/api/folders").body(body), CreateFolderResponse.class);
    }

    /**
     * Creates a new folder at the root of a workspace.
     *
     * @param workspaceId the workspace in which to create the folder
     * @param name        the name of the new folder
     * @return the response containing the created folder detail and count
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreateFolderResponse create(@NotNull String workspaceId, @NotNull String name) {
        return create(workspaceId, name, null);
    }

    /**
     * Retrieves detailed information about a single folder.
     *
     * @param folderId the unique identifier of the folder
     * @return the folder detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FolderDetail get(@NotNull String folderId) {
        JsonObject resp = http.request(HttpRequest.get("/api/folders/" + encode(folderId)));
        return http.fromJson(resp.get("folder"), FolderDetail.class);
    }

    /**
     * Renames a folder and returns the new name.
     *
     * @param folderId the unique identifier of the folder
     * @param name     the new name for the folder
     * @return the updated folder name as confirmed by the server
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String rename(@NotNull String folderId, @NotNull String name) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put("/api/folders/" + encode(folderId)).body(body));
        return resp.get("name").getAsString();
    }

    /**
     * Deletes a folder.
     *
     * @param folderId the unique identifier of the folder to delete
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void delete(@NotNull String folderId) {
        http.request(HttpRequest.delete("/api/folders/" + encode(folderId)));
    }

    /**
     * Moves a folder to a new parent folder.
     *
     * @param folderId the unique identifier of the folder to move
     * @param parentId the target parent folder ID
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void move(@NotNull String folderId, @NotNull String parentId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("parent_id", parentId);
        http.request(HttpRequest.put("/api/folders/" + encode(folderId) + "/move").body(body));
    }

    /**
     * Retrieves the folder tree for a workspace.
     *
     * @param workspaceId the workspace whose folder tree to retrieve
     * @return the list of folder tree items
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<FolderTreeItem> tree(@NotNull String workspaceId) {
        JsonObject resp = http.request(
                HttpRequest.get("/api/folders/tree").query("workspace_id", workspaceId));
        Type listType = new TypeToken<List<FolderTreeItem>>() {}.getType();
        return http.fromJson(resp.get("folders"), listType);
    }

    /**
     * Locks a folder with the specified lock mode and optional password.
     *
     * @param folderId the unique identifier of the folder to lock
     * @param lockMode the lock mode (e.g. "password", "readonly")
     * @param password the password for the lock, or {@code null} if not required
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void lock(@NotNull String folderId, @NotNull String lockMode, @Nullable String password) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("lock_mode", lockMode);
        if (password != null) body.put("password", password);
        http.request(HttpRequest.post("/api/folders/" + encode(folderId) + "/lock").body(body));
    }

    /**
     * Locks a folder with the specified lock mode without a password.
     *
     * @param folderId the unique identifier of the folder to lock
     * @param lockMode the lock mode (e.g. "password", "readonly")
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void lock(@NotNull String folderId, @NotNull String lockMode) {
        lock(folderId, lockMode, null);
    }

    /**
     * Removes the lock from a folder.
     *
     * @param folderId the unique identifier of the folder to unlock
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void unlock(@NotNull String folderId) {
        http.request(HttpRequest.post("/api/folders/" + encode(folderId) + "/unlock"));
    }
}
