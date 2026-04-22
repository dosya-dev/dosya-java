package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class FilesResource {

    private final DosyaHttpClient http;

    public FilesResource(DosyaHttpClient http) {
        this.http = http;
    }

    public ListFilesResponse list(ListFilesParams params) {
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

    public FileDetail get(String fileId) {
        JsonObject resp = http.request(HttpRequest.get("/api/files/" + encode(fileId)));
        return http.fromJson(resp.get("file"), FileDetail.class);
    }

    public boolean delete(String fileId) {
        JsonObject resp = http.request(HttpRequest.delete("/api/files/" + encode(fileId)));
        return resp.has("permanent") && resp.get("permanent").getAsBoolean();
    }

    public void restore(String fileId) {
        http.request(HttpRequest.put("/api/files/" + encode(fileId)));
    }

    public String rename(String fileId, String name) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put("/api/files/" + encode(fileId) + "/rename").body(body));
        return resp.get("name").getAsString();
    }

    public void move(String fileId, String folderId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("folder_id", folderId);
        http.request(HttpRequest.put("/api/files/" + encode(fileId) + "/move").body(body));
    }

    public FileDetail copy(String fileId, String newName, String folderId) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (newName != null) body.put("new_name", newName);
        if (folderId != null) body.put("folder_id", folderId);
        JsonObject resp = http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/copy").body(body));
        return http.fromJson(resp.get("file"), FileDetail.class);
    }

    public FileDetail copy(String fileId) {
        return copy(fileId, null, null);
    }

    public void lock(String fileId, String lockMode, String password) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("lock_mode", lockMode);
        if (password != null) body.put("password", password);
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/lock").body(body));
    }

    public void lock(String fileId, String lockMode) {
        lock(fileId, lockMode, null);
    }

    public void unlock(String fileId) {
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/unlock"));
    }

    public void hide(String fileId, String hiddenMode, List<String> targetIds) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (hiddenMode != null) body.put("hidden_mode", hiddenMode);
        if (targetIds != null) body.put("target_ids", targetIds);
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/hide").body(body));
    }

    public void hide(String fileId) {
        hide(fileId, null, null);
    }

    public FileVersionsResponse listVersions(String fileId) {
        return http.requestAs(
                HttpRequest.get("/api/files/" + encode(fileId) + "/versions"),
                FileVersionsResponse.class);
    }

    public VersionRestoreResponse restoreVersion(String fileId, int versionNumber) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("version_number", versionNumber);
        return http.requestAs(
                HttpRequest.post("/api/files/" + encode(fileId) + "/versions/restore").body(body),
                VersionRestoreResponse.class);
    }

    public List<ShareLinkDetail> getShareLinks(String fileId) {
        JsonObject resp = http.request(HttpRequest.get("/api/files/" + encode(fileId) + "/share"));
        Type listType = new TypeToken<List<ShareLinkDetail>>() {}.getType();
        return http.fromJson(resp.get("links"), listType);
    }

    public ShareLinkDetail createShareLink(String fileId, CreateShareLinkParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (params != null) {
            if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
            if (params.getPassword() != null) body.put("password", params.getPassword());
            if (params.getLockMode() != null) body.put("lock_mode", params.getLockMode());
        }
        JsonObject resp = http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/share").body(body));
        return http.fromJson(resp.get("link"), ShareLinkDetail.class);
    }

    public ShareLinkDetail createShareLink(String fileId) {
        return createShareLink(fileId, null);
    }

    public void shareByEmail(String fileId, List<String> emails, String message) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("emails", emails);
        if (message != null) body.put("message", message);
        http.request(HttpRequest.post("/api/files/" + encode(fileId) + "/share-email").body(body));
    }

    public ShareBundleLink createShareBundle(CreateShareBundleParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("file_ids", params.getFileIds());
        if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
        if (params.getPassword() != null) body.put("password", params.getPassword());
        JsonObject resp = http.request(HttpRequest.post("/api/files/share-bundle").body(body));
        return http.fromJson(resp.get("link"), ShareBundleLink.class);
    }
}
