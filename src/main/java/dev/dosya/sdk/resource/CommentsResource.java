package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CommentDetail;
import dev.dosya.sdk.model.CreateCommentParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for managing comments on files and folders in Dosya.
 *
 * <p>This resource handles listing, creating, editing, and deleting comments.</p>
 *
 * @since 0.1.0
 */
public final class CommentsResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code CommentsResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public CommentsResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists comments in a workspace, optionally filtered by file or folder.
     *
     * @param workspaceId the workspace whose comments to list
     * @param fileId      filter by file ID, or {@code null} to include all
     * @param folderId    filter by folder ID, or {@code null} to include all
     * @return the list of comments
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<CommentDetail> list(@NotNull String workspaceId, @Nullable String fileId, @Nullable String folderId) {
        JsonObject resp = http.request(
                HttpRequest.get("/api/comments")
                        .query("workspace_id", workspaceId)
                        .query("file_id", fileId)
                        .query("folder_id", folderId));
        Type listType = new TypeToken<List<CommentDetail>>() {}.getType();
        return http.fromJson(resp.get("comments"), listType);
    }

    /**
     * Lists all comments in a workspace.
     *
     * @param workspaceId the workspace whose comments to list
     * @return the list of comments
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<CommentDetail> list(@NotNull String workspaceId) {
        return list(workspaceId, null, null);
    }

    /**
     * Creates a new comment on a file or folder.
     *
     * @param params the comment creation parameters including workspace ID, body, and optional targets
     * @return the created comment detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CommentDetail create(@NotNull CreateCommentParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("workspace_id", params.getWorkspaceId());
        body.put("body", params.getBody());
        if (params.getFileId() != null) body.put("file_id", params.getFileId());
        if (params.getFolderId() != null) body.put("folder_id", params.getFolderId());
        if (params.getParentId() != null) body.put("parent_id", params.getParentId());
        JsonObject resp = http.request(HttpRequest.post("/api/comments").body(body));
        return http.fromJson(resp.get("comment"), CommentDetail.class);
    }

    /**
     * Edits the body of an existing comment.
     *
     * @param commentId the unique identifier of the comment to edit
     * @param body      the new comment body text
     * @return the updated comment detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CommentDetail edit(@NotNull String commentId, @NotNull String body) {
        Map<String, Object> reqBody = new HashMap<String, Object>();
        reqBody.put("body", body);
        JsonObject resp = http.request(
                HttpRequest.put("/api/comments/" + encode(commentId)).body(reqBody));
        return http.fromJson(resp, CommentDetail.class);
    }

    /**
     * Deletes a comment.
     *
     * @param commentId the unique identifier of the comment to delete
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void delete(@NotNull String commentId) {
        http.request(HttpRequest.delete("/api/comments/" + encode(commentId)));
    }
}
