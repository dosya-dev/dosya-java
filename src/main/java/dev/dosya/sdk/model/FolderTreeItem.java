package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a folder in a workspace folder tree hierarchy.
 *
 * @since 0.1.0
 */
public final class FolderTreeItem {

    private String id;
    private String name;
    private String parentId;
    private int fileCount;

    private FolderTreeItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }
    public int getFileCount() { return fileCount; }
}
