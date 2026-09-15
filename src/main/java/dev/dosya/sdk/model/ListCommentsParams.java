package dev.dosya.sdk.model;

import dev.dosya.sdk.internal.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Which item's comments to list: exactly one file or one folder.
 *
 * <pre>{@code
 * ListCommentsParams.file("ws_123", "file_456");
 * ListCommentsParams.folder("ws_123", "fld_789");
 * }</pre>
 *
 * @since 0.3.0
 */
public final class ListCommentsParams {

    private final String workspaceId;
    private final String fileId;
    private final String folderId;

    private ListCommentsParams(String workspaceId, String fileId, String folderId) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.fileId = fileId;
        this.folderId = folderId;
    }

    /** Comments on a file. */
    public static @NotNull ListCommentsParams file(@NotNull String workspaceId, @NotNull String fileId) {
        return new ListCommentsParams(workspaceId, Objects.requireNonNull(fileId, "fileId"), null);
    }

    /** Comments on a folder. */
    public static @NotNull ListCommentsParams folder(@NotNull String workspaceId, @NotNull String folderId) {
        return new ListCommentsParams(workspaceId, null, Objects.requireNonNull(folderId, "folderId"));
    }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable String getFolderId() { return folderId; }

    /** Adds {@code workspace_id} and {@code file_id} or {@code folder_id}. */
    public @NotNull HttpRequest applyTo(@NotNull HttpRequest request) {
        return request
                .query("workspace_id", workspaceId)
                .query("file_id", fileId)
                .query("folder_id", folderId);
    }
}
