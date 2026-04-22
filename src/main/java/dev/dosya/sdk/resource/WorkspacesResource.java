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

public class WorkspacesResource {

    private final DosyaHttpClient http;

    public WorkspacesResource(DosyaHttpClient http) {
        this.http = http;
    }

    public List<WorkspaceListItem> list() {
        JsonObject resp = http.request(HttpRequest.get("/api/workspaces"));
        Type listType = new TypeToken<List<WorkspaceListItem>>() {}.getType();
        return http.fromJson(resp.get("workspaces"), listType);
    }

    public WorkspaceGetResponse get(String workspaceId) {
        return http.requestAs(
                HttpRequest.get("/api/workspaces/" + encode(workspaceId)),
                WorkspaceGetResponse.class);
    }

    public WorkspaceDetail create(CreateWorkspaceParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", params.getName());
        if (params.getIconInitials() != null) body.put("icon_initials", params.getIconInitials());
        if (params.getIconColor() != null) body.put("icon_color", params.getIconColor());
        if (params.getDefaultRegion() != null) body.put("default_region", params.getDefaultRegion());
        JsonObject resp = http.request(HttpRequest.post("/api/workspaces").body(body));
        return http.fromJson(resp.get("workspace"), WorkspaceDetail.class);
    }

    public void update(String workspaceId, UpdateWorkspaceParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (params.getName() != null) body.put("name", params.getName());
        if (params.getIconInitials() != null) body.put("icon_initials", params.getIconInitials());
        if (params.getIconColor() != null) body.put("icon_color", params.getIconColor());
        if (params.getDefaultRegion() != null) body.put("default_region", params.getDefaultRegion());
        http.request(HttpRequest.put("/api/workspaces/" + encode(workspaceId)).body(body));
    }

    public void updateSettings(String workspaceId, WorkspaceSettings settings) {
        http.request(HttpRequest.put("/api/workspaces/" + encode(workspaceId) + "/settings")
                .body(settings));
    }

    public void delete(String workspaceId) {
        http.request(HttpRequest.delete("/api/workspaces/" + encode(workspaceId)));
    }
}
