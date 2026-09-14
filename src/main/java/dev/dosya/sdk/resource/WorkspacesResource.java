package dev.dosya.sdk.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateWorkspaceParams;
import dev.dosya.sdk.model.CreatedWorkspace;
import dev.dosya.sdk.model.DeleteWorkspaceResult;
import dev.dosya.sdk.model.UpdateWorkspaceParams;
import dev.dosya.sdk.model.WorkspaceDeletePreview;
import dev.dosya.sdk.model.WorkspaceDeletionRequest;
import dev.dosya.sdk.model.WorkspaceGetResponse;
import dev.dosya.sdk.model.WorkspaceListResponse;
import dev.dosya.sdk.model.WorkspaceSettingsUpdate;
import dev.dosya.sdk.model.WorkspaceShareSettings;
import dev.dosya.sdk.model.WorkspaceUploadLimits;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Workspaces: listing, creation, settings, deletion, ownership transfer.
 *
 * <p>Key scope: GET needs a {@code read} or {@code full} key, everything else {@code full};
 * {@code upload} keys cannot call any of these. A workspace-pinned key only reaches its own
 * workspace and cannot create new ones.
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
     * Every workspace the caller belongs to, oldest first, with storage figures and the
     * caller's cap allocation. A pinned key sees one row.
     *
     * @since 0.3.0 (returned {@code List<WorkspaceListItem>} before)
     */
    public @NotNull WorkspaceListResponse list() {
        return http.requestAs(HttpRequest.get("/api/workspaces"), WorkspaceListResponse.class);
    }

    /**
     * One workspace, its full settings row, the caller's role and storage.
     * 403 when the caller is not a member.
     */
    public @NotNull WorkspaceGetResponse get(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/workspaces/" + seg(workspaceId)), WorkspaceGetResponse.class);
    }

    /**
     * Creates a workspace owned by the caller.
     *
     * <p>{@code defaultRegion} is fixed for the life of the workspace; list valid codes with
     * {@code regions().list()} (400 "Unknown location" otherwise). The free plan allows 3
     * owned workspaces (403). {@code maxTotalStorageGb} must fit the owner's unallocated plan
     * storage (400). Refused for workspace-pinned keys (403).
     *
     * @since 0.3.0 (returned {@code WorkspaceDetail} before)
     */
    public @NotNull CreatedWorkspace create(@NotNull CreateWorkspaceParams params) {
        JsonObject resp = http.request(HttpRequest.post("/api/workspaces").body(params.toBody()));
        return http.fromJson(resp.get("workspace"), CreatedWorkspace.class);
    }

    /**
     * Renames or re-icons a workspace. Needs {@code access_settings} plus
     * {@code change_workspace_name} / {@code change_workspace_icon}. 400 when nothing is
     * given. The location cannot be changed.
     */
    public void update(@NotNull String workspaceId, @NotNull UpdateWorkspaceParams params) {
        http.request(HttpRequest.put("/api/workspaces/" + seg(workspaceId)).body(params.toBody()));
    }

    /**
     * The share defaults any member may read (default and maximum link expiry, whether
     * links are disabled or need a password). Null when the workspace has no settings row.
     *
     * @since 0.3.0
     */
    public @Nullable WorkspaceShareSettings getSettings(@NotNull String workspaceId) {
        JsonObject resp = http.request(HttpRequest.get("/api/workspaces/" + seg(workspaceId) + "/settings"));
        JsonElement settings = resp.get("settings");
        if (settings == null || settings.isJsonNull()) return null;
        return http.fromJson(settings, WorkspaceShareSettings.class);
    }

    /**
     * Changes workspace limits and policies. Only the fields set on {@code settings} are
     * written.
     *
     * <p>Needs {@code access_settings} plus the per-field permission
     * ({@code change_max_file_size}, {@code change_total_storage_cap}, ...,
     * {@code change_duplicate_scan}, or {@code manage_settings} for the rest).
     * Out-of-range or non-numeric values are a 400 whose error message names the problem;
     * 400 "Nothing to update" for an empty patch.
     *
     * @since 0.3.0 (took a {@code WorkspaceSettings} before)
     */
    public void updateSettings(@NotNull String workspaceId, @NotNull WorkspaceSettingsUpdate settings) {
        http.request(HttpRequest.put("/api/workspaces/" + seg(workspaceId) + "/settings").body(settings.toBody()));
    }

    /**
     * What an uploader is judged against: extension rules, file size cap, a storage
     * remaining hint and the concurrent upload limit. Any member.
     *
     * @since 0.3.0
     */
    public @NotNull WorkspaceUploadLimits uploadLimits(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/workspaces/" + seg(workspaceId) + "/upload-limits"),
                WorkspaceUploadLimits.class);
    }

    /**
     * What deleting the workspace would destroy, plus the blockers that would refuse it
     * ({@code has_members}, {@code last_workspace}). Owner only (403).
     *
     * @since 0.3.0
     */
    public @NotNull WorkspaceDeletePreview deletePreview(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/workspaces/" + seg(workspaceId) + "/delete-preview"),
                WorkspaceDeletePreview.class);
    }

    /**
     * Step one of deletion: emails the owner a 6-digit code, valid 15 minutes.
     * Owner only. 400 when other members remain or it is the owner's last workspace;
     * 429 within 60 s of the previous code or past 5 per hour.
     *
     * @since 0.3.0
     */
    public @NotNull WorkspaceDeletionRequest requestDeletion(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.post("/api/workspaces/" + seg(workspaceId) + "/delete-request"),
                WorkspaceDeletionRequest.class);
    }

    /**
     * Step two: deletes the workspace with the emailed code and its exact name.
     *
     * <p>By design an API key cannot delete a workspace unattended: someone has to read the
     * code from the owner's inbox. Errors: 400 name mismatch / members remain / last
     * workspace / missing code, 401 wrong or expired code (the 5th wrong attempt burns the
     * code, and every later call answers 401 "expired" - call {@link #requestDeletion(String)}
     * again), 403 not the owner. The result is pending (HTTP 202) when the deletion
     * continues in the background. Never retried: a replay would spend another attempt.
     *
     * @param code        the 6-digit code emailed by {@link #requestDeletion(String)}
     * @param confirmName must equal the workspace's name
     * @since 0.3.0 (replaces {@code delete(workspaceId)}, which the API refuses)
     */
    public @NotNull DeleteWorkspaceResult delete(@NotNull String workspaceId, @NotNull String code,
                                                 @NotNull String confirmName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", Objects.requireNonNull(code, "code"));
        body.put("confirm_name", Objects.requireNonNull(confirmName, "confirmName"));
        return http.requestAs(HttpRequest.delete("/api/workspaces/" + seg(workspaceId))
                .body(body)
                .retry(HttpRequest.Retry.NEVER), DeleteWorkspaceResult.class);
    }

    /**
     * Makes another member the owner. The caller becomes an admin. Owner only.
     *
     * @param userId the new owner's user id (not their membership id)
     * @since 0.3.0
     */
    public void transfer(@NotNull String workspaceId, @NotNull String userId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user_id", Objects.requireNonNull(userId, "userId"));
        http.request(HttpRequest.post("/api/workspaces/" + seg(workspaceId) + "/transfer").body(body));
    }

    /**
     * Leaves a workspace. The owner cannot leave (400; transfer ownership first).
     *
     * @since 0.3.0
     */
    public void leave(@NotNull String workspaceId) {
        http.request(HttpRequest.post("/api/workspaces/" + seg(workspaceId) + "/leave"));
    }
}
