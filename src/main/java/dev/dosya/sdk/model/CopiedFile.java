package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * The file created by copying a file.
 *
 * @since 0.3.0
 */
public final class CopiedFile {

    private String fileId;
    private String name;

    private CopiedFile() {}

    /** Id of the new copy. */
    public @NotNull String getFileId() { return fileId; }
    /** Name of the copy: {@code "Copy of <name>"} in the source's own folder, else the original name. */
    public @NotNull String getName() { return name; }
}
