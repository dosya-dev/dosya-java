package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A file row in a folder, trash, hidden or group listing.
 *
 * @since 0.1.0
 */
public final class FileListItem {

    private String id;
    private String name;
    private long sizeBytes;
    private String mimeType;
    private String extension;
    private String region;
    private String uploadedBy;
    private String uploaderName;
    private long createdAt;
    private long updatedAt;
    private Long deletedAt;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private int currentVersion;
    private int isSynced;
    private String importSource;
    private String importAccountEmail;
    private String origin;
    private String contentHash;
    private String etag;
    private String capturedAt;
    private int shareCount;
    private int commentCount;

    private FileListItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public long getSizeBytes() { return sizeBytes; }
    public @NotNull String getMimeType() { return mimeType; }
    /** Lower-case extension without the dot, or null. */
    public @Nullable String getExtension() { return extension; }
    public @NotNull String getRegion() { return region; }
    public @NotNull String getUploadedBy() { return uploadedBy; }
    /** Null when the uploader's account is gone. */
    public @Nullable String getUploaderName() { return uploaderName; }
    /** Source file creation time when known, else upload time (unix seconds). */
    public long getCreatedAt() { return createdAt; }
    /** Source file modification time when known, else last change (unix seconds). */
    public long getUpdatedAt() { return updatedAt; }
    /** Unix seconds; set in the trash view only. */
    public @Nullable Long getDeletedAt() { return deletedAt; }

    /**
     * The raw lock mode wire value.
     *
     * @deprecated use {@link #getLockModeValue()}.
     */
    @Deprecated
    public @Nullable String getLockMode() { return lockMode; }

    /** The lock mode; {@link LockMode#NONE} when absent or unknown. @since 0.3.0 */
    public @NotNull LockMode getLockModeValue() { return Modes.lock(lockMode); }

    public int getIsHidden() { return isHidden; }

    /**
     * The raw hidden mode wire value.
     *
     * @deprecated use {@link #getHiddenModeValue()}.
     */
    @Deprecated
    public @Nullable String getHiddenMode() { return hiddenMode; }

    /** The hidden mode; {@link HiddenMode#NONE} when absent or unknown. @since 0.3.0 */
    public @NotNull HiddenMode getHiddenModeValue() { return Modes.hidden(hiddenMode); }

    public int getCurrentVersion() { return currentVersion; }
    public int getIsSynced() { return isSynced; }
    /** Cloud import provider the file came from, or null. @since 0.3.0 */
    public @Nullable String getImportSource() { return importSource; }
    /** Account email of the cloud import, or null. @since 0.3.0 */
    public @Nullable String getImportAccountEmail() { return importAccountEmail; }
    /** Which client created the file (for example a sync client), or null. @since 0.3.0 */
    public @Nullable String getOrigin() { return origin; }
    /** Content hash once the file has been hashed, else null. @since 0.3.0 */
    public @Nullable String getContentHash() { return contentHash; }
    /** @since 0.3.0 */
    public @Nullable String getEtag() { return etag; }
    /** EXIF capture date as a zone-less wall-clock string, or null. @since 0.3.0 */
    public @Nullable String getCapturedAt() { return capturedAt; }
    public int getShareCount() { return shareCount; }
    public int getCommentCount() { return commentCount; }
}
