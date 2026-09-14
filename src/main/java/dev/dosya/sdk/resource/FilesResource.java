package dev.dosya.sdk.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.internal.ShareLinks;
import dev.dosya.sdk.model.BatchDeleteParams;
import dev.dosya.sdk.model.BatchDeleteResult;
import dev.dosya.sdk.model.CopiedFile;
import dev.dosya.sdk.model.CreateShareBundleParams;
import dev.dosya.sdk.model.CreateShareLinkParams;
import dev.dosya.sdk.model.CreatedShareBundle;
import dev.dosya.sdk.model.CreatedShareLink;
import dev.dosya.sdk.model.DuplicatesResponse;
import dev.dosya.sdk.model.FileDetail;
import dev.dosya.sdk.model.FileVersionsResponse;
import dev.dosya.sdk.model.HiddenMode;
import dev.dosya.sdk.model.HideInfo;
import dev.dosya.sdk.model.ItemShareLink;
import dev.dosya.sdk.model.ListFilesParams;
import dev.dosya.sdk.model.ListFilesResponse;
import dev.dosya.sdk.model.LockInfo;
import dev.dosya.sdk.model.LockMode;
import dev.dosya.sdk.model.SetHideParams;
import dev.dosya.sdk.model.ShareByEmailParams;
import dev.dosya.sdk.model.ShareByEmailResult;
import dev.dosya.sdk.model.UnlockGrant;
import dev.dosya.sdk.model.VersionRestoreResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Files in a workspace: listing, metadata, trash, rename/move/copy, locks, hiding,
 * versions, share links, batch delete and duplicate detection.
 *
 * <p>API keys: GET calls need the {@code read} scope, everything else {@code full}.
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

    private static String path(String fileId) {
        return "/api/files/" + seg(fileId);
    }

    /**
     * Lists folders and files in a workspace folder, the trash ({@code deleted}), hidden
     * items ({@code hidden}) or a folder group ({@code groupId}).
     *
     * <p>A {@code full_lock} folder answers 403 {@code folder_locked} unless a valid
     * {@link ListFilesParams#unlockToken(String) unlock token} is passed. A folder-confined
     * member cannot list the trash or a group (403).
     *
     * @param params the listing parameters
     * @return folders, files, breadcrumbs and pagination
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull ListFilesResponse list(@NotNull ListFilesParams params) {
        boolean deleted = Boolean.TRUE.equals(params.getDeleted());
        return http.requestAs(
                HttpRequest.get("/api/files")
                        .query("workspace_id", params.getWorkspaceId())
                        .query("folder_id", params.getFolderId())
                        .query("filter", params.getFilter())
                        .query("sort", params.getSort())
                        .query("dir", params.getDir())
                        .query("q", params.getQ())
                        // The handler checks `=== "1"`; "true" would silently list live items.
                        .query("deleted", deleted ? "1" : null)
                        .query("hidden", Boolean.TRUE.equals(params.getHidden()) ? "1" : null)
                        .query("ut", params.getUnlockToken())
                        .query("group_id", params.getGroupId())
                        .query("folder", deleted ? params.getFolder() : null)
                        .query("page", params.getPage())
                        .query("per_page", params.getPerPage()),
                ListFilesResponse.class);
    }

    /**
     * Gets one file's metadata. Hidden files answer 404; files inside a locked folder
     * answer 403 {@code folder_locked}.
     *
     * @param fileId the file id
     * @return the file metadata
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileDetail get(@NotNull String fileId) {
        JsonObject resp = http.request(HttpRequest.get(path(fileId)));
        return http.fromJson(resp.get("file"), FileDetail.class);
    }

    /**
     * Deletes a file in two stages: the first call moves a live file to the trash
     * ({@code false}), a call on a file already in the trash purges it and all its
     * versions for good ({@code true}). Locked files cannot be deleted (403).
     *
     * <p>Never retried automatically, so a replay cannot turn a trash into a purge.
     *
     * @param fileId the file id
     * @return {@code true} if the file was purged, {@code false} if it was moved to the trash
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public boolean delete(@NotNull String fileId) {
        JsonObject resp = http.request(HttpRequest.delete(path(fileId)).retry(HttpRequest.Retry.NEVER));
        JsonElement permanent = resp.get("permanent");
        return permanent != null && !permanent.isJsonNull() && permanent.getAsBoolean();
    }

    /**
     * Restores a file from the trash. 409 when its folder is still in the trash
     * (restore the folder first). Never retried automatically.
     *
     * @param fileId the file id
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void restore(@NotNull String fileId) {
        http.request(HttpRequest.put(path(fileId)).retry(HttpRequest.Retry.NEVER));
    }

    /**
     * Renames a file. Needs the {@code rename_files} permission; locked files cannot be renamed.
     *
     * @param fileId the file id
     * @param name   the new name
     * @return the name as stored by the server
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String rename(@NotNull String fileId, @NotNull String name) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put(path(fileId) + "/rename").body(body));
        return resp.get("name").getAsString();
    }

    /**
     * Moves a file to a folder, optionally renaming it in the same step (renaming also
     * needs {@code rename_files}).
     *
     * <p>409 when the target already has a file with that name; 400 when the file is
     * already there. Never retried automatically.
     *
     * @param fileId   the file id
     * @param folderId the target folder id, or {@code null} for the workspace root
     * @param name     the new name, or {@code null} to keep the current one
     * @return the file's name after the move
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull String move(@NotNull String fileId, @Nullable String folderId, @Nullable String name) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("folder_id", folderId);
        if (name != null) body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put(path(fileId) + "/move").body(body)
                .retry(HttpRequest.Retry.NEVER));
        return resp.get("name").getAsString();
    }

    /**
     * Moves a file to a folder, keeping its name. See {@link #move(String, String, String)}.
     *
     * @param fileId   the file id
     * @param folderId the target folder id, or {@code null} for the workspace root
     * @return the file's name
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String move(@NotNull String fileId, @Nullable String folderId) {
        return move(fileId, folderId, null);
    }

    /**
     * Copies a file into a folder. The copy is named {@code "Copy of <name>"} when it lands
     * in the source file's own folder, otherwise it keeps the name.
     *
     * <p>Needs the {@code upload_files} permission and counts against storage.
     *
     * @param fileId   the file id
     * @param folderId the target folder id, or {@code null} for the workspace ROOT (not next to the source)
     * @return the new file's id and name
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull CopiedFile copy(@NotNull String fileId, @Nullable String folderId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("folder_id", folderId);
        return http.requestAs(HttpRequest.post(path(fileId) + "/copy").body(body), CopiedFile.class);
    }

    /**
     * Copies a file to the workspace ROOT (not next to the source).
     * See {@link #copy(String, String)}.
     *
     * @param fileId the file id
     * @return the new file's id and name
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CopiedFile copy(@NotNull String fileId) {
        return copy(fileId, null);
    }

    /**
     * Reads a file's lock state.
     *
     * @param fileId the file id
     * @return the lock mode and who set it
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull LockInfo getLock(@NotNull String fileId) {
        return http.requestAs(HttpRequest.get(path(fileId) + "/lock"), LockInfo.class);
    }

    /**
     * Sets or removes a file lock. {@link LockMode#NONE} removes it; {@link LockMode#FULL_LOCK}
     * needs a password of at least 4 characters. Needs the {@code lock_files} permission.
     *
     * @param fileId   the file id
     * @param lockMode the new lock mode
     * @param password the lock password (required for {@code FULL_LOCK}), or {@code null}
     * @return the lock mode now in effect
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull LockMode lock(@NotNull String fileId, @NotNull LockMode lockMode, @Nullable String password) {
        Objects.requireNonNull(lockMode, "lockMode");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lock_mode", lockMode.value());
        if (password != null) body.put("password", password);
        return readLockMode(http.request(HttpRequest.post(path(fileId) + "/lock").body(body)), lockMode);
    }

    /**
     * Sets a lock that needs no password, or removes the lock with {@link LockMode#NONE}.
     * See {@link #lock(String, LockMode, String)}.
     *
     * @param fileId   the file id
     * @param lockMode the new lock mode
     * @return the lock mode now in effect
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull LockMode lock(@NotNull String fileId, @NotNull LockMode lockMode) {
        return lock(fileId, lockMode, null);
    }

    /**
     * Locks a file using the wire value of the lock mode.
     *
     * @param fileId   the file id
     * @param lockMode {@code "none"}, {@code "view_only"} or {@code "full_lock"}
     * @param password the lock password, or {@code null}
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @deprecated use {@link #lock(String, LockMode, String)}.
     */
    @Deprecated
    public void lock(@NotNull String fileId, @NotNull String lockMode, @Nullable String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lock_mode", lockMode);
        if (password != null) body.put("password", password);
        http.request(HttpRequest.post(path(fileId) + "/lock").body(body));
    }

    /**
     * Locks a file using the wire value of the lock mode, without a password.
     *
     * @param fileId   the file id
     * @param lockMode {@code "none"} or {@code "view_only"}
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @deprecated use {@link #lock(String, LockMode)}.
     */
    @Deprecated
    public void lock(@NotNull String fileId, @NotNull String lockMode) {
        lock(fileId, lockMode, (String) null);
    }

    /**
     * Exchanges the password of a {@code full_lock} file for a one-hour unlock token, to
     * pass to download, raw and thumbnail calls.
     *
     * <p>This does NOT remove the lock - use {@code lock(fileId, LockMode.NONE)} for that.
     * 401 on a wrong password; 400 when the file is not password-locked.
     *
     * @param fileId   the file id
     * @param password the lock password
     * @return the unlock token and its expiry
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull UnlockGrant unlock(@NotNull String fileId, @NotNull String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", password);
        return http.requestAs(HttpRequest.post(path(fileId) + "/unlock").body(body), UnlockGrant.class);
    }

    /**
     * Reads a file's hidden mode and its user/role rules.
     *
     * @param fileId the file id
     * @return the hidden state
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull HideInfo getHide(@NotNull String fileId) {
        return http.requestAs(HttpRequest.get(path(fileId) + "/hide"), HideInfo.class);
    }

    /**
     * Hides a file from everyone, from specific users or roles, or un-hides it
     * ({@link SetHideParams#none()}). Replaces any previous rules. Needs the
     * {@code hide_files} permission.
     *
     * @param fileId the file id
     * @param params who to hide the file from
     * @return the hidden mode now in effect
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull HiddenMode hide(@NotNull String fileId, @NotNull SetHideParams params) {
        JsonObject resp = http.request(HttpRequest.post(path(fileId) + "/hide").body(params.toBody()));
        JsonElement mode = resp.get("hidden_mode");
        if (mode == null || mode.isJsonNull()) return params.getMode();
        HiddenMode parsed = http.fromJson(mode, HiddenMode.class);
        return parsed != null ? parsed : params.getMode();
    }

    /**
     * Hides a file using the wire value of the hidden mode.
     *
     * <p>Before 0.3.0 this sent {@code target_ids}, which the API ignores, so hiding from
     * users or roles always failed; it now sends {@code targets}.
     *
     * @param fileId     the file id
     * @param hiddenMode {@code "none"}, {@code "everyone"}, {@code "users"} or {@code "roles"}
     * @param targetIds  user or role ids for {@code "users"} / {@code "roles"}, else {@code null}
     * @throws IllegalArgumentException if the mode is null or unknown (a missing mode un-hides)
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @deprecated use {@link #hide(String, SetHideParams)}.
     */
    @Deprecated
    public void hide(@NotNull String fileId, @Nullable String hiddenMode, @Nullable List<String> targetIds) {
        SetHideParams params;
        if ("none".equals(hiddenMode)) params = SetHideParams.none();
        else if ("everyone".equals(hiddenMode)) params = SetHideParams.everyone();
        else if ("users".equals(hiddenMode) || "roles".equals(hiddenMode)) {
            if (targetIds == null || targetIds.isEmpty()) {
                throw new IllegalArgumentException("Hidden mode '" + hiddenMode + "' needs at least one target id");
            }
            params = "users".equals(hiddenMode) ? SetHideParams.users(targetIds) : SetHideParams.roles(targetIds);
        }
        else throw new IllegalArgumentException("Unknown hidden mode: " + hiddenMode);
        hide(fileId, params);
    }

    /**
     * Lists a file's versions, newest first. A never-edited file still reports its current
     * version, with the synthetic id {@code fver_implicit_<fileId>}.
     *
     * @param fileId the file id
     * @return the current version's basics and the version history
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileVersionsResponse listVersions(@NotNull String fileId) {
        return http.requestAs(HttpRequest.get(path(fileId) + "/versions"), FileVersionsResponse.class);
    }

    /**
     * Makes an older version current again by copying it forward as a new version.
     * Refused on locked files; needs the {@code upload_files} permission.
     *
     * @param fileId        the file id
     * @param versionNumber the version to restore (1 or higher)
     * @return the new version number and the version it was copied from
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull VersionRestoreResponse restoreVersion(@NotNull String fileId, int versionNumber) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("version_number", versionNumber);
        return http.requestAs(HttpRequest.post(path(fileId) + "/versions/restore").body(body),
                VersionRestoreResponse.class);
    }

    /**
     * Lists a file's live (non-revoked) share links. Roles limited to their own shares
     * see only theirs.
     *
     * @param fileId the file id
     * @return the share links
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<ItemShareLink> getShareLinks(@NotNull String fileId) {
        return Collections.unmodifiableList(ShareLinks.list(http, ShareLinks.Kind.FILES, fileId));
    }

    /**
     * Creates a public or recipient-restricted share link for a file.
     *
     * <p>Share passwords need at least 8 characters; workspace settings may force a
     * password, cap the expiry or disable links (403).
     *
     * @param fileId the file id
     * @param params link options, or {@code null} for the defaults
     * @return the created link
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreatedShareLink createShareLink(@NotNull String fileId, @Nullable CreateShareLinkParams params) {
        return ShareLinks.create(http, ShareLinks.Kind.FILES, fileId, params);
    }

    /**
     * Creates a public share link for a file with the workspace defaults.
     *
     * @param fileId the file id
     * @return the created link
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreatedShareLink createShareLink(@NotNull String fileId) {
        return createShareLink(fileId, null);
    }

    /**
     * Creates a share link and emails it. Check {@link ShareByEmailResult#getFailed()} for
     * addresses that could not be mailed. Never retried automatically: a failure can
     * arrive after the link was created and mailed.
     *
     * @param fileId the file id
     * @param params recipients and link options
     * @return the link URL and the delivery outcome
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull ShareByEmailResult shareByEmail(@NotNull String fileId, @NotNull ShareByEmailParams params) {
        return ShareLinks.email(http, ShareLinks.Kind.FILES, fileId, params);
    }

    /**
     * Emails a share link to the recipients.
     *
     * @param fileId  the file id
     * @param emails  the recipient addresses
     * @param message an optional message, or {@code null}
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @deprecated use {@link #shareByEmail(String, ShareByEmailParams)}, which reports failed addresses.
     */
    @Deprecated
    public void shareByEmail(@NotNull String fileId, @NotNull List<String> emails, @Nullable String message) {
        ShareByEmailParams params = new ShareByEmailParams(emails);
        if (message != null) params.message(message);
        shareByEmail(fileId, params);
    }

    /**
     * Creates one share link covering 1-100 files from the same workspace. With
     * {@code notify}, the link is emailed to the recipients.
     *
     * @param params the files and link options
     * @return the link plus the email delivery outcome
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreatedShareBundle createShareBundle(@NotNull CreateShareBundleParams params) {
        return http.requestAs(HttpRequest.post("/api/files/share-bundle").body(params.toBody()),
                CreatedShareBundle.class);
    }

    /**
     * Moves up to 500 files and any number of folders to the trash in one call (never
     * purges). Items the caller may not delete, hidden items and locked folders are skipped
     * silently; a locked FILE refuses the whole batch (403).
     *
     * @param params the workspace and the ids to delete
     * @return how many files and folders were moved to the trash
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull BatchDeleteResult batchDelete(@NotNull BatchDeleteParams params) {
        return http.requestAs(HttpRequest.post("/api/files/batch-delete").body(params.toBody()),
                BatchDeleteResult.class);
    }

    /**
     * Lists groups of identical files (same content hash) in a workspace, most wasted space
     * first. Returns {@code scanEnabled == false} and no groups when the workspace has
     * duplicate scanning turned off.
     *
     * @param workspaceId the workspace id
     * @return the duplicate groups
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull DuplicatesResponse duplicates(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/duplicates").query("workspace_id", workspaceId),
                DuplicatesResponse.class);
    }

    private LockMode readLockMode(JsonObject resp, LockMode fallback) {
        JsonElement mode = resp.get("lock_mode");
        if (mode == null || mode.isJsonNull()) return fallback;
        LockMode parsed = http.fromJson(mode, LockMode.class);
        return parsed != null ? parsed : fallback;
    }
}
