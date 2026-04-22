package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for managing files in Dosya workspaces.
 *
 * <p>This resource handles listing, retrieving, deleting, restoring, renaming,
 * moving, copying, locking, hiding, versioning, and sharing files.</p>
 *
 * @since 0.1.0
 */
public final class FilesResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code FilesResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public FilesResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists files and folders in a workspace, optionally filtered by folder, query, or status.
     *
     * @param params the listing parameters including workspace ID, pagination, and filters
     * @return the response containing files, folders, breadcrumbs, and pagination info
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull ListFilesResponse list(@NotNull ListFilesParams params) {
        return http.requestAs(
                HttpRequest.get("/api/files")
                        .query("workspace_id", params.getWorkspaceId())
                        .query("folder_id", params.getFolderId())
                        .query("filter", params.getFilter())
                        .query("sort", params.getSort())
                        .query("q", params.getQ())
                        .query("deleted", params.getDeleted())
                        .query("hidden", params.getHidden())
                        .query("page", params.getPage())
                        .query("per_page", params.getPerPage()),
                ListFilesResponse.class);
    }

    /**
     * Retrieves detailed information about a single file.
     *
     * @param fileId the unique identifier of the file
     * @return the file detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileDetail get(@NotNull String fileId) {
        JsonObject resp = http.request(HttpRequest.get("/api/files/" + encode(fileId)));
        return http.fromJson(resp.get("file"), FileDetail.class);
    }

    /**
     * Deletes a file. Returns whether the deletion was permanent.
     *
     * @param fileId the unique identifier of the file to delete
     * @return {@code true} if the file was permanently deleted, {@code false} if soft-deleted
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public boolean delete(@NotNull String fileId) {
        JsonObject resp = http.request(HttpRequest.delete("/api/files/" + encode(fileId)));
        return resp.has("permanent") && resp.get("permanent").getAsBoolean();
    }

    /**
     * Restores a previously soft-deleted file.
     *
     * @param fileId the unique identifier of the file to restore
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void restore(@NotNull String fileId) {
        http.request(HttpRequest.put("/api/files/" + encode(fileId)));
    }

    /**
     * Renames a file and returns the new name.
     *
     * @param fileId the unique identifier of the file
     * @param name   the new name for the file
     * @return the updated file name as confirmed by the server
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String rename(@NotNull String fileId, @NotNull String name) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put("/api/files/" + encode(fileId) + "/rename").body(body));
        return resp.get("name").getAsString();
    }

    /**
     * Moves a file into a different folder.
     *
     * @param fileId   the unique identifier of the file to move
     * @param folderId the target folder ID
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void move(@NotNull String fileId, @NotNull String folderId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("folder_id", folderId);
        http.request(HttpRequest.put("/api/files/" + encode(fileId) + "/move").body(body));
    }

    /**
     * Copies a file, optionally with a new name and/or into a different folder.
     *
     * @param fileId   the unique identifier of the file to copy
     * @param newName  the name for the copy, or {@code null} to keep the original name
     * @param folderId the target folder ID for the copy, or {@code null} to keep the same folder
     * @return the detail of the newly created copy
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileDetail copy(@NotNull String fileId, @Nullable String newName, @Nullable String folderId) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (newName != null) body.put("new_name", newName);
        if (folderId != null) body.put("folder_id", folderId);
        JsonObject resp = http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/copy").body(body));
        return http.fromJson(resp.get("file"), FileDetail.class);
    }

    /**
     * Copies a file using the original name and folder.
     *
     * @param fileId the unique identifier of the file to copy
     * @return the detail of the newly created copy
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileDetail copy(@NotNull String fileId) {
        return copy(fileId, null, null);
    }

    /**
     * Locks a file with the specified lock mode and optional password.
     *
     * @param fileId   the unique identifier of the file to lock
     * @param lockMode the lock mode (e.g. "password", "readonly")
     * @param password the password for the lock, or {@code null} if not required
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void lock(@NotNull String fileId, @NotNull String lockMode, @Nullable String password) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("lock_mode", lockMode);
        if (password != null) body.put("password", password);
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/lock").body(body));
    }

    /**
     * Locks a file with the specified lock mode without a password.
     *
     * @param fileId   the unique identifier of the file to lock
     * @param lockMode the lock mode (e.g. "password", "readonly")
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void lock(@NotNull String fileId, @NotNull String lockMode) {
        lock(fileId, lockMode, null);
    }

    /**
     * Removes the lock from a file.
     *
     * @param fileId the unique identifier of the file to unlock
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void unlock(@NotNull String fileId) {
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/unlock"));
    }

    /**
     * Hides a file, optionally specifying a hidden mode and target user/group IDs.
     *
     * @param fileId     the unique identifier of the file to hide
     * @param hiddenMode the hidden mode, or {@code null} for the default
     * @param targetIds  the list of user or group IDs to hide the file from, or {@code null}
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void hide(@NotNull String fileId, @Nullable String hiddenMode, @Nullable List<String> targetIds) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (hiddenMode != null) body.put("hidden_mode", hiddenMode);
        if (targetIds != null) body.put("target_ids", targetIds);
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/hide").body(body));
    }

    /**
     * Hides a file using the default hidden mode.
     *
     * @param fileId the unique identifier of the file to hide
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void hide(@NotNull String fileId) {
        hide(fileId, null, null);
    }

    /**
     * Lists all versions of a file.
     *
     * @param fileId the unique identifier of the file
     * @return the response containing the file name, current version, and version history
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileVersionsResponse listVersions(@NotNull String fileId) {
        return http.requestAs(
                HttpRequest.get("/api/files/" + encode(fileId) + "/versions"),
                FileVersionsResponse.class);
    }

    /**
     * Restores a file to a previous version.
     *
     * @param fileId        the unique identifier of the file
     * @param versionNumber the version number to restore
     * @return the response containing the new version number and the version it was restored from
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull VersionRestoreResponse restoreVersion(@NotNull String fileId, int versionNumber) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("version_number", versionNumber);
        return http.requestAs(
                HttpRequest.post("/api/files/" + encode(fileId) + "/versions/restore").body(body),
                VersionRestoreResponse.class);
    }

    /**
     * Retrieves all share links associated with a file.
     *
     * @param fileId the unique identifier of the file
     * @return the list of share link details
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<ShareLinkDetail> getShareLinks(@NotNull String fileId) {
        JsonObject resp = http.request(HttpRequest.get("/api/files/" + encode(fileId) + "/share"));
        Type listType = new TypeToken<List<ShareLinkDetail>>() {}.getType();
        return http.fromJson(resp.get("links"), listType);
    }

    /**
     * Creates a new share link for a file with optional configuration.
     *
     * @param fileId the unique identifier of the file to share
     * @param params optional parameters such as expiration, password, and lock mode; may be {@code null}
     * @return the created share link detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull ShareLinkDetail createShareLink(@NotNull String fileId, @Nullable CreateShareLinkParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (params != null) {
            if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
            if (params.getPassword() != null) body.put("password", params.getPassword());
            if (params.getLockMode() != null) body.put("lock_mode", params.getLockMode());
        }
        JsonObject resp = http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/share").body(body));
        return http.fromJson(resp.get("link"), ShareLinkDetail.class);
    }

    /**
     * Creates a new share link for a file with default settings.
     *
     * @param fileId the unique identifier of the file to share
     * @return the created share link detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull ShareLinkDetail createShareLink(@NotNull String fileId) {
        return createShareLink(fileId, null);
    }

    /**
     * Shares a file by sending email invitations to the specified recipients.
     *
     * @param fileId  the unique identifier of the file to share
     * @param emails  the list of recipient email addresses
     * @param message an optional message to include in the email, or {@code null}
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void shareByEmail(@NotNull String fileId, @NotNull List<String> emails, @Nullable String message) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("emails", emails);
        if (message != null) body.put("message", message);
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/share-email").body(body));
    }

    /**
     * Creates a share bundle containing multiple files as a single shareable link.
     *
     * @param params the bundle parameters including file IDs and optional expiration/password
     * @return the created share bundle link
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull ShareBundleLink createShareBundle(@NotNull CreateShareBundleParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("file_ids", params.getFileIds());
        if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
        if (params.getPassword() != null) body.put("password", params.getPassword());
        JsonObject resp = http.request(HttpRequest.post("/api/files/share-bundle").body(body));
        return http.fromJson(resp.get("link"), ShareBundleLink.class);
    }
}
