package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A direct subfolder, as returned by {@code folders().children()}.
 *
 * @since 0.3.0
 */
public final class FolderChild {

    private String id;
    private String name;
    private String parentId;
    private int fileCount;
    private boolean hasChildren;

    private FolderChild() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }
    public int getFileCount() { return fileCount; }
    /** Whether the folder has live subfolders. */
    public boolean hasChildren() { return hasChildren; }
}
