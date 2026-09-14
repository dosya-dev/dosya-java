package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * One folder or one file to star or unstar.
 *
 * <pre>{@code
 * FavouriteTarget.folder("ws_1", "fld_1");
 * FavouriteTarget.file("ws_1", "file_1");
 * }</pre>
 *
 * @since 0.3.0
 */
public final class FavouriteTarget {

    private final String workspaceId;
    private final String folderId;
    private final String fileId;

    private FavouriteTarget(String workspaceId, String folderId, String fileId) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.folderId = folderId;
        this.fileId = fileId;
    }

    public static @NotNull FavouriteTarget folder(@NotNull String workspaceId, @NotNull String folderId) {
        return new FavouriteTarget(workspaceId, Objects.requireNonNull(folderId, "folderId"), null);
    }

    public static @NotNull FavouriteTarget file(@NotNull String workspaceId, @NotNull String fileId) {
        return new FavouriteTarget(workspaceId, null, Objects.requireNonNull(fileId, "fileId"));
    }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    /** Set for a folder target, else null. */
    public @Nullable String getFolderId() { return folderId; }
    /** Set for a file target, else null. */
    public @Nullable String getFileId() { return fileId; }
}
