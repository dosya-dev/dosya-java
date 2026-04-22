package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateFolderResponse;
import dev.dosya.sdk.model.FolderDetail;
import dev.dosya.sdk.model.FolderTreeItem;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class FoldersResource {

    private final DosyaHttpClient http;

    public FoldersResource(DosyaHttpClient http) {
        this.http = http;
    }

    public CreateFolderResponse create(String workspaceId, String name, String parentId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("workspace_id", workspaceId);
        body.put("name", name);
        if (parentId != null) body.put("parent_id", parentId);
        return http.requestAs(HttpRequest.post("/api/folders").body(body), CreateFolderResponse.class);
    }

    public CreateFolderResponse create(String workspaceId, String name) {
        return create(workspaceId, name, null);
    }

    public FolderDetail get(String folderId) {
        JsonObject resp = http.request(HttpRequest.get("/api/folders/" + encode(folderId)));
        return http.fromJson(resp.get("folder"), FolderDetail.class);
    }

    public String rename(String folderId, String name) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", name);
        JsonObject resp = http.request(HttpRequest.put("/api/folders/" + encode(folderId)).body(body));
        return resp.get("name").getAsString();
    }

    public void delete(String folderId) {
        http.request(HttpRequest.delete("/api/folders/" + encode(folderId)));
    }

    public void move(String folderId, String parentId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("parent_id", parentId);
        http.request(HttpRequest.put("/api/folders/" + encode(folderId) + "/move").body(body));
    }

    public List<FolderTreeItem> tree(String workspaceId) {
        JsonObject resp = http.request(
                HttpRequest.get("/api/folders/tree").query("workspace_id", workspaceId));
        Type listType = new TypeToken<List<FolderTreeItem>>() {}.getType();
        return http.fromJson(resp.get("folders"), listType);
    }

    public void lock(String folderId, String lockMode, String password) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("lock_mode", lockMode);
        if (password != null) body.put("password", password);
        http.request(HttpRequest.post("/api/folders/" + encode(folderId) + "/lock").body(body));
    }

    public void lock(String folderId, String lockMode) {
        lock(folderId, lockMode, null);
    }

    public void unlock(String folderId) {
        http.request(HttpRequest.post("/api/folders/" + encode(folderId) + "/unlock"));
    }
}
