package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a single version of a file.
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

    public @NotNull String getId() { return id; }
    public int getVersionNumber() { return versionNumber; }
    public long getSizeBytes() { return sizeBytes; }
    public @NotNull String getMimeType() { return mimeType; }
    public @NotNull String getExtension() { return extension; }
    public @NotNull String getUploadedBy() { return uploadedBy; }
    public @NotNull String getUploaderName() { return uploaderName; }
    public long getCreatedAt() { return createdAt; }
}
