package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * A folder name match, as returned by {@code folders().search()}.
 *
 * @since 0.3.0
 */
public final class FolderSearchResult {

    private String id;
    private String name;
    private int fileCount;
    private String path;

    private FolderSearchResult() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public int getFileCount() { return fileCount; }
    /** Ancestor names root-first joined with {@code " / "}; {@code ""} for a root folder. */
    public @NotNull String getPath() { return path != null ? path : ""; }
}
