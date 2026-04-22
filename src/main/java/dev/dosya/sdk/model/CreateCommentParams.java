package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for creating a new comment on a file or folder.
 *
 * <pre>{@code
 * CreateCommentParams params = new CreateCommentParams("ws_123", "Great file!")
 *     .fileId("file_456");
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

    public CreateCommentParams(@NotNull String workspaceId, @NotNull String body) {
        this.workspaceId = workspaceId;
        this.body = body;
    }

    public CreateCommentParams fileId(String fileId) { this.fileId = fileId; return this; }
    public CreateCommentParams folderId(String folderId) { this.folderId = folderId; return this; }
    public CreateCommentParams parentId(String parentId) { this.parentId = parentId; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getBody() { return body; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getParentId() { return parentId; }
}
