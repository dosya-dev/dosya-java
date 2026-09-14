package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CommentDetail;
import dev.dosya.sdk.model.CommentEditResult;
import dev.dosya.sdk.model.CreateCommentParams;
import dev.dosya.sdk.model.ListCommentsParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Comments on files and folders.
 *
 * @since 0.1.0
 */
public final class CommentsResource {

    private static final Type COMMENT_LIST = new TypeToken<List<CommentDetail>>() {}.getType();

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
     * Every comment on a file or a folder, flat and oldest first (rebuild threads from
     * {@code parentId}). No pagination.
     *
     * @since 0.3.0
     */
    public @NotNull List<CommentDetail> list(@NotNull ListCommentsParams params) {
        JsonObject res = http.request(params.applyTo(HttpRequest.get("/api/comments")));
        List<CommentDetail> list = http.fromJson(res.get("comments"), COMMENT_LIST);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    /** Every comment on a file. See {@link #list(ListCommentsParams)}. @since 0.3.0 */
    public @NotNull List<CommentDetail> listForFile(@NotNull String workspaceId, @NotNull String fileId) {
        return list(ListCommentsParams.file(workspaceId, fileId));
    }

    /** Every comment on a folder. See {@link #list(ListCommentsParams)}. @since 0.3.0 */
    public @NotNull List<CommentDetail> listForFolder(@NotNull String workspaceId, @NotNull String folderId) {
        return list(ListCommentsParams.folder(workspaceId, folderId));
    }

    /**
     * Lists comments on a file or a folder.
     *
     * @throws IllegalArgumentException unless exactly one of {@code fileId} and {@code folderId} is set
     *                                  (the API answers 400 without one)
     * @deprecated use {@link #listForFile} or {@link #listForFolder}
     */
    @Deprecated
    public @NotNull List<CommentDetail> list(@NotNull String workspaceId, @Nullable String fileId, @Nullable String folderId) {
        if ((fileId == null) == (folderId == null)) {
            throw new IllegalArgumentException("Exactly one of fileId or folderId is required");
        }
        return list(fileId != null
                ? ListCommentsParams.file(workspaceId, fileId)
                : ListCommentsParams.folder(workspaceId, folderId));
    }

    /**
     * Comments on a file or folder, or replies with {@code parentId}. Body 1-5000 characters.
     * Needs a full-scope key; keys pinned to a workspace are refused (403).
     *
     * @throws IllegalStateException unless exactly one of file and folder is set
     */
    public @NotNull CommentDetail create(@NotNull CreateCommentParams params) {
        JsonObject res = http.request(HttpRequest.post("/api/comments").body(params.toBody()));
        return http.fromJson(res.get("comment"), CommentDetail.class);
    }

    /**
     * Edits your own comment. Needs a full-scope key; workspace-pinned keys are refused.
     *
     * @since 0.3.0 (returned a mostly empty {@code CommentDetail} before)
     */
    public @NotNull CommentEditResult edit(@NotNull String commentId, @NotNull String body) {
        Map<String, Object> reqBody = new LinkedHashMap<>();
        reqBody.put("body", body);
        return http.requestAs(HttpRequest.put("/api/comments/" + seg(commentId)).body(reqBody), CommentEditResult.class);
    }

    /**
     * Deletes a comment. Allowed for its author and for workspace owners/admins. Needs a
     * full-scope key; workspace-pinned keys are refused. Never retried automatically.
     */
    public void delete(@NotNull String commentId) {
        http.request(HttpRequest.delete("/api/comments/" + seg(commentId)).retry(HttpRequest.Retry.NEVER));
    }
}
