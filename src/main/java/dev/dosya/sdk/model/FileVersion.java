package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One version of a file.
 *
 * @since 0.1.0
 */
public final class FileVersion {

    private String id;
    private int versionNumber;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private String uploadedBy;
    private String uploaderName;
    private long createdAt;

    private FileVersion() {}

    /**
     * The version id. A never-edited file reports its current version as
     * {@code fver_implicit_<fileId>}, which is not a real version row.
     */
    public @NotNull String getId() { return id; }
    public int getVersionNumber() { return versionNumber; }
    public long getSizeBytes() { return sizeBytes; }
    public @NotNull String getMimeType() { return mimeType; }
    public @Nullable String getExtension() { return extension; }
    public @NotNull String getUploadedBy() { return uploadedBy; }
    /** Null when the uploader's account is gone. */
    public @Nullable String getUploaderName() { return uploaderName; }
    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }
}
