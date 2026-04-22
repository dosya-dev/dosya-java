package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ApiKeyItem;
import dev.dosya.sdk.model.CreateApiKeyParams;
import dev.dosya.sdk.model.CreatedApiKey;
import dev.dosya.sdk.model.UserProfile;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for the authenticated user's profile and API keys.
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
     * Retrieves the authenticated user's profile.
     *
     * @return the user profile
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull UserProfile profile() {
        JsonObject resp = http.request(HttpRequest.get("/api/me"));
        return http.fromJson(resp.get("user"), UserProfile.class);
    }

    /**
     * Lists all API keys belonging to the authenticated user.
     *
     * @return the list of API key items
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<ApiKeyItem> listApiKeys() {
        JsonObject resp = http.request(HttpRequest.get("/api/me/api-keys"));
        Type listType = new TypeToken<List<ApiKeyItem>>() {}.getType();
        return http.fromJson(resp.get("keys"), listType);
    }

    /**
     * Creates a new API key for the authenticated user.
     *
     * @param params the API key creation parameters including name and optional scope/expiration
     * @return the created API key, including the plain-text key (shown only once)
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreatedApiKey createApiKey(@NotNull CreateApiKeyParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", params.getName());
        if (params.getScope() != null) body.put("scope", params.getScope());
        if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
        JsonObject resp = http.request(HttpRequest.post("/api/me/api-keys").body(body));
        return http.fromJson(resp.get("key"), CreatedApiKey.class);
    }

    /**
     * Deletes an API key.
     *
     * @param keyId the unique identifier of the API key to delete
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void deleteApiKey(@NotNull String keyId) {
        http.request(HttpRequest.delete("/api/me/api-keys/" + encode(keyId)));
    }
}
