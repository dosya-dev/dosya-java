package dev.dosya.sdk.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.MyWorkspacePermissions;
import dev.dosya.sdk.model.UserProfile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The account that owns the API key: profile, per-workspace permissions, display name
 * and self-revocation of the key.
 *
 * <p>Listing, creating and deleting API keys by id are session-only routes (a leaked key
 * must not be able to mint or inspect its siblings), so they are not part of the SDK.
 *
 * @since 0.1.0
 */
public final class MeResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code MeResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public MeResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Gets the account that owns this API key. Needs a {@code read} or {@code full} key.
     * Note {@link UserProfile#getAvatarUrl()} is a storage key, not a URL.
     *
     * @return the user profile
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull UserProfile profile() {
        JsonObject resp = http.request(HttpRequest.get("/api/me"));
        return http.fromJson(resp.get("user"), UserProfile.class);
    }

    /**
     * The caller's role and effective permissions in one workspace. Read scope.
     * Throws 403 {@code Not a member} when the caller is not in the workspace, and 403
     * for a key pinned to a different workspace. Permission keys stay snake_case
     * (e.g. {@code upload_files}).
     *
     * @param workspaceId the workspace to inspect
     * @return the caller's role and permission map
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull MyWorkspacePermissions permissions(@NotNull String workspaceId) {
        return http.requestAs(
                HttpRequest.get("/api/me/permissions").query("workspace_id", workspaceId),
                MyWorkspacePermissions.class);
    }

    /**
     * Changes the account's display name (trimmed, 1-80 characters; 400 otherwise).
     * Requires a {@code full} scope key.
     *
     * @param name the new display name
     * @return the stored name
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public @NotNull String updateName(@NotNull String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put("/api/me/name").body(body));
        JsonElement stored = resp.get("name");
        return stored != null && stored.isJsonPrimitive() ? stored.getAsString() : name.trim();
    }

    /**
     * Permanently revokes the API key this client authenticates with. Works at any key
     * scope. The client is unusable afterwards: every later call fails with 401.
     * Not retried, because a replay after success would itself 401.
     *
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     * @since 0.3.0
     */
    public void revokeCurrentKey() {
        http.request(HttpRequest.delete("/api/me/api-keys/current").retry(HttpRequest.Retry.NEVER));
    }
}
