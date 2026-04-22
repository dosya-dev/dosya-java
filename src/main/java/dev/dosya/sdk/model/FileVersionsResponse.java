package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Response returned when listing file versions.
 *
 * <p>Contains the file name, current version number, and a list of all versions.</p>
 *
 * @since 0.1.0
 */
public final class FileVersionsResponse {

    private String fileName;
    private int currentVersion;
    private List<FileVersion> versions;

    private FileVersionsResponse() {}

    public @NotNull String getFileName() { return fileName; }
    public int getCurrentVersion() { return currentVersion; }
    public @NotNull List<FileVersion> getVersions() { return versions != null ? Collections.unmodifiableList(versions) : Collections.<FileVersion>emptyList(); }
}
