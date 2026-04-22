package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CommentDetail;
import dev.dosya.sdk.model.CreateCommentParams;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class CommentsResource {

    private final DosyaHttpClient http;

    public CommentsResource(DosyaHttpClient http) {
        this.http = http;
    }

    public List<CommentDetail> list(String workspaceId, String fileId, String folderId) {
        JsonObject resp = http.request(
                HttpRequest.get("/api/comments")
                        .query("workspace_id", workspaceId)
                        .query("file_id", fileId)
                        .query("folder_id", folderId));
        Type listType = new TypeToken<List<CommentDetail>>() {}.getType();
        return http.fromJson(resp.get("comments"), listType);
    }

    public List<CommentDetail> list(String workspaceId) {
        return list(workspaceId, null, null);
    }

    public CommentDetail create(CreateCommentParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("workspace_id", params.getWorkspaceId());
        body.put("body", params.getBody());
        if (params.getFileId() != null) body.put("file_id", params.getFileId());
        if (params.getFolderId() != null) body.put("folder_id", params.getFolderId());
        if (params.getParentId() != null) body.put("parent_id", params.getParentId());
        JsonObject resp = http.request(HttpRequest.post("/api/comments").body(body));
        return http.fromJson(resp.get("comment"), CommentDetail.class);
    }

    public CommentDetail edit(String commentId, String body) {
        Map<String, Object> reqBody = new HashMap<String, Object>();
        reqBody.put("body", body);
        JsonObject resp = http.request(
                HttpRequest.put("/api/comments/" + encode(commentId)).body(reqBody));
        return http.fromJson(resp, CommentDetail.class);
    }

    public void delete(String commentId) {
        http.request(HttpRequest.delete("/api/comments/" + encode(commentId)));
    }
}
