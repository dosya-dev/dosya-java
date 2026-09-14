package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A file's version history plus the current version's basics.
 *
 * @since 0.1.0
 */
public final class FileVersionsResponse {

    private String fileName;
    private long fileSize;
    private String fileMime;
    private long fileCreated;
    private int currentVersion;
    private List<FileVersion> versions;

    private FileVersionsResponse() {}

    public @NotNull String getFileName() { return fileName; }
    /** Size of the current version in bytes. @since 0.3.0 */
    public long getFileSize() { return fileSize; }
    /** MIME type of the current version. @since 0.3.0 */
    public @NotNull String getFileMime() { return fileMime; }
    /** Unix seconds. @since 0.3.0 */
    public long getFileCreated() { return fileCreated; }
    public int getCurrentVersion() { return currentVersion; }
    /** Newest first. Always includes the current version. */
    public @NotNull List<FileVersion> getVersions() { return versions != null ? Collections.unmodifiableList(versions) : Collections.<FileVersion>emptyList(); }
}
