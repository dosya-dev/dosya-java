package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Result of restoring a trashed folder.
 *
 * @since 0.3.0
 */
public final class RestoreFolderResult {

    private String folderId;
    private String name;
    private boolean restoredToRoot;
    private int filesRestored;
    private int foldersRestored;

    private RestoreFolderResult() {}

    public @NotNull String getFolderId() { return folderId; }
    /** The name it came back under (renamed on a collision). */
    public @NotNull String getName() { return name; }
    /** True when the original parent is gone and the folder landed at the workspace root. */
    public boolean isRestoredToRoot() { return restoredToRoot; }
    public int getFilesRestored() { return filesRestored; }
    public int getFoldersRestored() { return foldersRestored; }
}
