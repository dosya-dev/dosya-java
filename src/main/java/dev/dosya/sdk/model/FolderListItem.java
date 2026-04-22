package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Summary information about a folder as returned in listing responses.
 *
 * @since 0.1.0
 */
public final class FolderListItem {

    private String id;
    private String name;
    private long createdAt;
    private int fileCount;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private int isSynced;

    private FolderListItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public long getCreatedAt() { return createdAt; }
    public int getFileCount() { return fileCount; }
    public @Nullable String getLockMode() { return lockMode; }
    public int getIsHidden() { return isHidden; }
    public @Nullable String getHiddenMode() { return hiddenMode; }
    public int getIsSynced() { return isSynced; }
}
