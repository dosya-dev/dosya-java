package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateRoleParams;
import dev.dosya.sdk.model.RolesListResponse;
import dev.dosya.sdk.model.UpdateRoleParams;
import org.jetbrains.annotations.NotNull;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Workspace roles and their permissions.
 *
 * <p>Key scope: GET needs {@code read} or {@code full}, the rest {@code full}; {@code upload}
 * keys cannot call these. Workspace-pinned keys can only call {@link #list(String)} (for their
 * own workspace).
 *
 * @since 0.3.0
 */
public final class RolesResource {

    private final DosyaHttpClient http;

    public RolesResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Built-in and custom roles with their permission maps, conditions and usage limits.
     * Any member. Use the ids with {@code team().invite()} / {@code team().updateMember()}.
     */
    public @NotNull RolesListResponse list(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/roles").query("workspace_id", workspaceId), RolesListResponse.class);
    }

    /**
     * Creates a custom role and returns its id. Needs {@code manage_roles}. 400 on invalid
     * conditions or limits.
     */
    public @NotNull String create(@NotNull CreateRoleParams params) {
        JsonObject resp = http.request(HttpRequest.post("/api/roles").body(params.toBody()));
        return resp.get("role_id").getAsString();
    }

    /**
     * Updates a custom role; only the fields set change, permissions are merged per key.
     * Needs {@code manage_roles}. Built-in roles are refused (400).
     */
    public void update(@NotNull String roleId, @NotNull UpdateRoleParams params) {
        http.request(HttpRequest.put("/api/roles/" + seg(roleId)).body(params.toBody()));
    }

    /**
     * Deletes a custom role. Needs {@code manage_roles}. 400 for built-in roles or while
     * members still hold the role. Never retried.
     */
    public void delete(@NotNull String roleId) {
        http.request(HttpRequest.delete("/api/roles/" + seg(roleId)).retry(HttpRequest.Retry.NEVER));
    }
}
