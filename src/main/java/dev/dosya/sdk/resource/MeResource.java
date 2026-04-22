package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ApiKeyItem;
import dev.dosya.sdk.model.CreateApiKeyParams;
import dev.dosya.sdk.model.CreatedApiKey;
import dev.dosya.sdk.model.UserProfile;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class MeResource {

    private final DosyaHttpClient http;

    public MeResource(DosyaHttpClient http) {
        this.http = http;
    }

    public UserProfile profile() {
        JsonObject resp = http.request(HttpRequest.get("/api/me"));
        return http.fromJson(resp.get("user"), UserProfile.class);
    }

    public List<ApiKeyItem> listApiKeys() {
        JsonObject resp = http.request(HttpRequest.get("/api/me/api-keys"));
        Type listType = new TypeToken<List<ApiKeyItem>>() {}.getType();
        return http.fromJson(resp.get("keys"), listType);
    }

    public CreatedApiKey createApiKey(CreateApiKeyParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", params.getName());
        if (params.getScope() != null) body.put("scope", params.getScope());
        if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
        JsonObject resp = http.request(HttpRequest.post("/api/me/api-keys").body(body));
        return http.fromJson(resp.get("key"), CreatedApiKey.class);
    }

    public void deleteApiKey(String keyId) {
        http.request(HttpRequest.delete("/api/me/api-keys/" + encode(keyId)));
    }
}
