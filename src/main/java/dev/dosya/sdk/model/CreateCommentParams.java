package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A new comment, or a reply, on exactly one file or one folder.
 *
 * <pre>{@code
 * CreateCommentParams.onFile("ws_123", "file_456", "Looks good");
 * CreateCommentParams.onFolder("ws_123", "fld_789", "Agreed").parentId("cmt_1");
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateCommentParams {

    private final String workspaceId;
    private final String body;
    private String fileId;
    private String folderId;
    private String parentId;

    /**
     * @param body 1-5000 characters
     * @deprecated use {@link #onFile} or {@link #onFolder}, which make the required target explicit
     */
    @Deprecated
    public CreateCommentParams(@NotNull String workspaceId, @NotNull String body) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.body = Objects.requireNonNull(body, "body");
    }

    /** A comment on a file. {@code body} is 1-5000 characters. @since 0.3.0 */
    @SuppressWarnings("deprecation")
    public static @NotNull CreateCommentParams onFile(@NotNull String workspaceId, @NotNull String fileId, @NotNull String body) {
        return new CreateCommentParams(workspaceId, body).fileId(Objects.requireNonNull(fileId, "fileId"));
    }

    /** A comment on a folder. {@code body} is 1-5000 characters. @since 0.3.0 */
    @SuppressWarnings("deprecation")
    public static @NotNull CreateCommentParams onFolder(@NotNull String workspaceId, @NotNull String folderId, @NotNull String body) {
        return new CreateCommentParams(workspaceId, body).folderId(Objects.requireNonNull(folderId, "folderId"));
    }

    /** Target a file. Exactly one of file and folder must be set. */
    public @NotNull CreateCommentParams fileId(@Nullable String fileId) { this.fileId = fileId; return this; }
    /** Target a folder. Exactly one of file and folder must be set. */
    public @NotNull CreateCommentParams folderId(@Nullable String folderId) { this.folderId = folderId; return this; }
    /** Reply to this comment. */
    public @NotNull CreateCommentParams parentId(@Nullable String parentId) { this.parentId = parentId; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getBody() { return body; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getParentId() { return parentId; }

    /**
     * The request body with only the allowed keys.
     *
     * @throws IllegalStateException unless exactly one of file and folder is set
     * @since 0.3.0
     */
    public @NotNull Map<String, Object> toBody() {
        if ((fileId == null) == (folderId == null)) {
            throw new IllegalStateException("A comment needs exactly one of fileId or folderId");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("workspace_id", workspaceId);
        if (fileId != null) out.put("file_id", fileId);
        if (folderId != null) out.put("folder_id", folderId);
        if (parentId != null) out.put("parent_id", parentId);
        out.put("body", body);
        return out;
    }
}
