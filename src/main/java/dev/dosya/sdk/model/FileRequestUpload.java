package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A file uploaded through a file request.
 *
 * @since 0.3.0
 */
public final class FileRequestUpload {

    private String id;
    private String fileId;
    private String uploaderEmail;
    private String uploaderName;
    private long createdAt;
    private String fileName;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private long updatedAt;
    private int currentVersion;
    private LockMode lockMode;
    private Boolean isHidden;
    private String uploadedBy;
    private String region;
    private String origin;

    private FileRequestUpload() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getFileId() { return fileId; }
    public @Nullable String getUploaderEmail() { return uploaderEmail; }
    public @Nullable String getUploaderName() { return uploaderName; }
    /** When the upload arrived, unix seconds. */
    public long getCreatedAt() { return createdAt; }
    public @NotNull String getFileName() { return fileName; }
    public long getSizeBytes() { return sizeBytes; }
    public @Nullable String getMimeType() { return mimeType; }
    public @Nullable String getExtension() { return extension; }
    public long getUpdatedAt() { return updatedAt; }
    public int getCurrentVersion() { return currentVersion; }
    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    /** Null when the API did not say. */
    public @Nullable Boolean getIsHidden() { return isHidden; }
    public @Nullable String getUploadedBy() { return uploadedBy; }
    public @Nullable String getRegion() { return region; }
    public @Nullable String getOrigin() { return origin; }
}
