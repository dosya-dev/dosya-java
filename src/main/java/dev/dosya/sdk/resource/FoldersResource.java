package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.internal.ShareLinks;
import dev.dosya.sdk.model.BatchFolderInput;
import dev.dosya.sdk.model.BatchFolderResult;
import dev.dosya.sdk.model.CreateFolderParams;
import dev.dosya.sdk.model.CreateFolderResponse;
import dev.dosya.sdk.model.CreateShareLinkParams;
import dev.dosya.sdk.model.CreatedShareLink;
import dev.dosya.sdk.model.DeleteFolderResult;
import dev.dosya.sdk.model.FolderChild;
import dev.dosya.sdk.model.FolderDetail;
import dev.dosya.sdk.model.FolderSearchResult;
import dev.dosya.sdk.model.FolderTreeItem;
import dev.dosya.sdk.model.HiddenMode;
import dev.dosya.sdk.model.HideInfo;
import dev.dosya.sdk.model.ItemShareLink;
import dev.dosya.sdk.model.LockInfo;
import dev.dosya.sdk.model.LockMode;
import dev.dosya.sdk.model.PurgeFolderOptions;
import dev.dosya.sdk.model.PurgeFolderSummary;
import dev.dosya.sdk.model.RestoreFolderResult;
import dev.dosya.sdk.model.SetHideParams;
import dev.dosya.sdk.model.ShareByEmailParams;
import dev.dosya.sdk.model.ShareByEmailResult;
import dev.dosya.sdk.model.UnlockGrant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Folder operations: create, inspect, rename, move, trash/restore/purge, lock, hide,
 * share links and folder lookups.
 *
 * @since 0.1.0
 */
public final class FoldersResource {

    private static final Type TREE_LIST = new TypeToken<List<FolderTreeItem>>() {}.getType();
    private static final Type CHILD_LIST = new TypeToken<List<FolderChild>>() {}.getType();
    private static final Type SEARCH_LIST = new TypeToken<List<FolderSearchResult>>() {}.getType();
    private static final Type BATCH_LIST = new TypeToken<List<BatchFolderResult>>() {}.getType();

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code FoldersResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public FoldersResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    private static String base(String folderId) {
        return "/api/folders/" + seg(folderId);
    }

    // ── create ──

    /**
     * Creates a folder, or a nested path like {@code "a/b/c"}, find-or-create style: existing
     * segments are reused and {@code createdCount} is 0 when nothing was new, so repeating the
     * call is safe. Works with an upload-scoped key. 403 {@code folder_locked} when the parent
     * is full-locked; 404 for a missing or hidden parent.
     *
     * @since 0.3.0
     */
    public @NotNull CreateFolderResponse create(@NotNull CreateFolderParams params) {
        return http.requestAs(HttpRequest.post("/api/folders").body(params.toBody()), CreateFolderResponse.class);
    }

    /**
     * Creates a folder (or nested path) under {@code parentId}, or at the root when {@code null}.
     * See {@link #create(CreateFolderParams)}.
     */
    public @NotNull CreateFolderResponse create(@NotNull String workspaceId, @NotNull String name, @Nullable String parentId) {
        return create(new CreateFolderParams(workspaceId, name).parentId(parentId));
    }

    /** Creates a folder (or nested path) at the workspace root. See {@link #create(CreateFolderParams)}. */
    public @NotNull CreateFolderResponse create(@NotNull String workspaceId, @NotNull String name) {
        return create(workspaceId, name, null);
    }

    /**
     * Creates up to 500 single-segment folders in one call (find-or-create). Needs a full-scope key.
     * Entries with an invalid name, or a parent that is missing, hidden or outside the caller's
     * reach, are silently left out of the result - compare sizes if that matters.
     *
     * @since 0.3.0
     */
    public @NotNull List<BatchFolderResult> createBatch(@NotNull String workspaceId, @NotNull List<BatchFolderInput> folders) {
        List<Map<String, Object>> entries = new ArrayList<>(folders.size());
        for (BatchFolderInput f : folders) entries.add(f.toBody());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        body.put("folders", entries);
        JsonObject res = http.request(HttpRequest.post("/api/folders/batch").body(body));
        return unmodifiable(http.fromJson(res.get("folders"), BATCH_LIST));
    }

    // ── read ──

    /** Folder metadata. Also answers for a folder in the trash ({@link FolderDetail#isDeleted()}). */
    public @NotNull FolderDetail get(@NotNull String folderId) {
        JsonObject res = http.request(HttpRequest.get(base(folderId)));
        return http.fromJson(res.get("folder"), FolderDetail.class);
    }

    /**
     * Every live folder in the workspace as one flat list (no pagination). For large workspaces
     * prefer {@link #children} and {@link #search}. Needs {@code access_files}.
     */
    public @NotNull List<FolderTreeItem> tree(@NotNull String workspaceId) {
        JsonObject res = http.request(HttpRequest.get("/api/folders/tree").query("workspace_id", workspaceId));
        return unmodifiable(http.fromJson(res.get("folders"), TREE_LIST));
    }

    /**
     * Direct subfolders of {@code parentId} ({@code null} for the root), each with
     * {@code hasChildren}. 404 for a hidden parent. Needs {@code access_files}.
     *
     * @since 0.3.0
     */
    public @NotNull List<FolderChild> children(@NotNull String workspaceId, @Nullable String parentId) {
        JsonObject res = http.request(HttpRequest.get("/api/folders/children")
                .query("workspace_id", workspaceId)
                .query("parent_id", parentId));
        return unmodifiable(http.fromJson(res.get("folders"), CHILD_LIST));
    }

    /** Direct subfolders of the workspace root. See {@link #children(String, String)}. @since 0.3.0 */
    public @NotNull List<FolderChild> children(@NotNull String workspaceId) {
        return children(workspaceId, null);
    }

    /**
     * Up to 50 folders whose name contains {@code q}, sorted by name, each with its breadcrumb
     * path. An empty {@code q} returns no folders. Needs {@code access_files}.
     *
     * @since 0.3.0
     */
    public @NotNull List<FolderSearchResult> search(@NotNull String workspaceId, @NotNull String q) {
        JsonObject res = http.request(HttpRequest.get("/api/folders/search")
                .query("workspace_id", workspaceId)
                .query("q", q));
        return unmodifiable(http.fromJson(res.get("folders"), SEARCH_LIST));
    }

    // ── change ──

    /**
     * Renames a live folder and returns the name the server stored. Needs {@code rename_folders}.
     *
     * <p>0.3.0 fix: 0.2.x sent this to {@code PUT /api/folders/:id}, which is the restore route.
     */
    public @NotNull String rename(@NotNull String folderId, @NotNull String name) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        JsonObject res = http.request(HttpRequest.put(base(folderId) + "/rename").body(body));
        return res.get("name").getAsString();
    }

    /**
     * Moves a folder under {@code parentId}, or to the workspace root when {@code null}. Needs
     * {@code rename_folders}. A move to the folder's current parent is a 400, not a no-op.
     * Never retried automatically.
     */
    public void move(@NotNull String folderId, @Nullable String parentId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("parent_id", parentId);
        http.request(HttpRequest.put(base(folderId) + "/move").body(body).retry(HttpRequest.Retry.NEVER));
    }

    /**
     * Restores a trashed root folder and everything deleted with it. Lands at the workspace root
     * when the original parent is gone, and is renamed on a name collision. 400 when the folder is
     * not in the trash or was deleted as part of another folder. Never retried automatically.
     *
     * @since 0.3.0
     */
    public @NotNull RestoreFolderResult restore(@NotNull String folderId) {
        return http.requestAs(HttpRequest.put(base(folderId)).retry(HttpRequest.Retry.NEVER), RestoreFolderResult.class);
    }

    /**
     * Deletes a folder. Two-stage:
     * <ul>
     *   <li>on a live folder, moves the subtree to the trash ({@code permanent} false);</li>
     *   <li>on a folder ALREADY in the trash, PERMANENTLY purges it ({@code permanent} true).
     *       A purge pass can stop early ({@code complete} false, HTTP 202); call again to finish.</li>
     * </ul>
     * A second call on the same folder therefore destroys data, so this is never retried
     * automatically. Prefer {@link #purge} to empty a trashed folder. Any lock other than
     * {@code none} blocks deletion with 403.
     *
     * @param maxR2Calls lowers the R2 call budget of a purge pass (cannot raise it); {@code null} for the default
     * @since 0.3.0
     */
    public @NotNull DeleteFolderResult delete(@NotNull String folderId, @Nullable Integer maxR2Calls) {
        return http.requestAs(HttpRequest.delete(base(folderId))
                .query("max_r2_calls", maxR2Calls)
                .retry(HttpRequest.Retry.NEVER), DeleteFolderResult.class);
    }

    /** Deletes a folder with the default purge budget. See {@link #delete(String, Integer)}. */
    public @NotNull DeleteFolderResult delete(@NotNull String folderId) {
        return delete(folderId, null);
    }

    /**
     * Permanently purges a folder that is already in the trash, calling delete until the purge
     * completes. Refuses (without deleting anything) when {@link #get} says the folder is not in
     * the trash, so it can never trash a live folder by mistake. Stops if the thread is interrupted.
     *
     * @throws DosyaException when the folder is live, a pass trashed instead of purging (someone
     *                        restored it in between), or the purge did not finish within {@code maxPasses}
     * @since 0.3.0
     */
    public @NotNull PurgeFolderSummary purge(@NotNull String folderId, @NotNull PurgeFolderOptions options) {
        Objects.requireNonNull(options, "options");
        FolderDetail folder = get(folderId);
        if (!folder.isDeleted()) {
            throw new DosyaException("Folder " + folderId + " is not in the trash; call delete() first to trash it");
        }
        int maxPasses = options.getMaxPasses();
        long filesAffected = 0;
        for (int passes = 1; passes <= maxPasses; passes++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new DosyaException("Purge of folder " + folderId + " interrupted after " + (passes - 1) + " passes");
            }
            DeleteFolderResult result = delete(folderId, options.getMaxR2Calls());
            if (!result.isPermanent()) {
                // Restored and re-trashed by someone else in between: stop, do not purge.
                throw new DosyaException("Folder " + folderId
                        + " was live again and has been moved back to the trash; purge not performed");
            }
            filesAffected += result.getFilesAffected();
            if (result.isComplete()) return new PurgeFolderSummary(filesAffected, passes);
        }
        throw new DosyaException("Purge of folder " + folderId + " did not complete within " + maxPasses + " passes");
    }

    /** Purges a trashed folder with default options. See {@link #purge(String, PurgeFolderOptions)}. @since 0.3.0 */
    public @NotNull PurgeFolderSummary purge(@NotNull String folderId) {
        return purge(folderId, new PurgeFolderOptions());
    }

    // ── lock ──

    /** Current lock state. @since 0.3.0 */
    public @NotNull LockInfo getLock(@NotNull String folderId) {
        return http.requestAs(HttpRequest.get(base(folderId) + "/lock"), LockInfo.class);
    }

    /**
     * Sets or removes a lock and returns the mode now in effect. {@link LockMode#NONE} removes it;
     * {@link LockMode#FULL_LOCK} needs a password of at least 4 characters. Needs {@code lock_files}.
     *
     * @param password the lock password, or {@code null} when not needed
     * @since 0.3.0
     */
    public @NotNull LockMode lock(@NotNull String folderId, @NotNull LockMode lockMode, @Nullable String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lock_mode", lockMode.value());
        if (password != null) body.put("password", password);
        JsonObject res = http.request(HttpRequest.post(base(folderId) + "/lock").body(body));
        LockMode mode = res.has("lock_mode") ? http.fromJson(res.get("lock_mode"), LockMode.class) : null;
        return mode != null ? mode : lockMode;
    }

    /** Sets a lock that needs no password ({@code none} or {@code view_only}). @since 0.3.0 */
    public @NotNull LockMode lock(@NotNull String folderId, @NotNull LockMode lockMode) {
        return lock(folderId, lockMode, null);
    }

    /**
     * Sets a lock from a raw wire value.
     *
     * @deprecated use {@link #lock(String, LockMode, String)}; valid values are {@code none},
     *             {@code view_only} and {@code full_lock}
     */
    @Deprecated
    public void lock(@NotNull String folderId, @NotNull String lockMode, @Nullable String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lock_mode", lockMode);
        if (password != null) body.put("password", password);
        http.request(HttpRequest.post(base(folderId) + "/lock").body(body));
    }

    /**
     * Sets a lock from a raw wire value without a password.
     *
     * @deprecated use {@link #lock(String, LockMode)}
     */
    @Deprecated
    public void lock(@NotNull String folderId, @NotNull String lockMode) {
        lock(folderId, lockMode, (String) null);
    }

    /**
     * Enters the password of a {@code full_lock} folder to get a one-hour access grant for the
     * calling user. Does not remove the lock (use {@code lock(id, LockMode.NONE)}). 400 when the
     * folder is not password-locked, 401 on a wrong password. Needs a full-scope key.
     *
     * @since 0.3.0 (replaces {@code unlock(String)}, which sent no password and could not work)
     */
    public @NotNull UnlockGrant unlock(@NotNull String folderId, @NotNull String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", password);
        return http.requestAs(HttpRequest.post(base(folderId) + "/unlock").body(body), UnlockGrant.class);
    }

    // ── hide ──

    /** Hidden state and its user/role rules. @since 0.3.0 */
    public @NotNull HideInfo getHide(@NotNull String folderId) {
        return http.requestAs(HttpRequest.get(base(folderId) + "/hide"), HideInfo.class);
    }

    /**
     * Hides a folder from everyone, from listed users or roles, or un-hides it
     * ({@link SetHideParams#none()}), replacing existing rules. Returns the mode now in effect.
     * Needs {@code hide_files}.
     *
     * @since 0.3.0
     */
    public @NotNull HiddenMode hide(@NotNull String folderId, @NotNull SetHideParams params) {
        JsonObject res = http.request(HttpRequest.post(base(folderId) + "/hide").body(params.toBody()));
        HiddenMode mode = res.has("hidden_mode") ? http.fromJson(res.get("hidden_mode"), HiddenMode.class) : null;
        return mode != null ? mode : params.getMode();
    }

    // ── share links ──

    /** Live share links on this folder the caller may see. @since 0.3.0 */
    public @NotNull List<ItemShareLink> getShareLinks(@NotNull String folderId) {
        return unmodifiable(ShareLinks.list(http, ShareLinks.Kind.FOLDERS, folderId));
    }

    /**
     * Creates a share link to this folder. Refused for locked folders and by workspace share policy.
     *
     * @param params link options, or {@code null} for defaults
     * @since 0.3.0
     */
    public @NotNull CreatedShareLink createShareLink(@NotNull String folderId, @Nullable CreateShareLinkParams params) {
        return ShareLinks.create(http, ShareLinks.Kind.FOLDERS, folderId, params);
    }

    /** Creates a share link to this folder with default options. @since 0.3.0 */
    public @NotNull CreatedShareLink createShareLink(@NotNull String folderId) {
        return createShareLink(folderId, null);
    }

    /** Emails a link to this folder to up to 50 recipients. Never retried automatically. @since 0.3.0 */
    public @NotNull ShareByEmailResult shareByEmail(@NotNull String folderId, @NotNull ShareByEmailParams params) {
        return ShareLinks.email(http, ShareLinks.Kind.FOLDERS, folderId, params);
    }

    private static <T> List<T> unmodifiable(@Nullable List<T> list) {
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }
}
