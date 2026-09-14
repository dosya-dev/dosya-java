package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * One file for {@code upload().batch(...)} or {@code upload().many(...)}.
 *
 * @since 0.3.0
 */
public final class UploadBatchFile {

    private final String name;
    private final UploadSource source;
    private String folderId;
    private String fileId;
    private String sha256;
    private Integer expectedVersion;
    private Long sourceModifiedAt;
    private Long sourceCreatedAt;

    /**
     * @param name   file name in the destination
     * @param source the bytes (at most 5 MiB for {@code batch()})
     */
    public UploadBatchFile(@NotNull String name, @NotNull UploadSource source) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name is required");
        this.name = name;
        this.source = Objects.requireNonNull(source, "source");
    }

    /** A file from bytes in memory. */
    public static @NotNull UploadBatchFile of(@NotNull String name, byte @NotNull [] bytes) {
        return new UploadBatchFile(name, UploadSource.ofBytes(bytes));
    }

    /**
     * A file from disk, under its own name.
     *
     * @throws IOException if the file's size cannot be read
     */
    public static @NotNull UploadBatchFile of(@NotNull Path path) throws IOException {
        Path fileName = path.getFileName();
        return new UploadBatchFile(fileName != null ? fileName.toString() : path.toString(), UploadSource.ofPath(path));
    }

    /** Destination folder; null for the workspace root. */
    public UploadBatchFile folderId(@Nullable String folderId) { this.folderId = folderId; return this; }

    /** Upload as a new version of this file. */
    public UploadBatchFile fileId(@Nullable String fileId) { this.fileId = fileId; return this; }

    /** Hex SHA-256, verified per entry (a mismatch fails that entry with {@code hash_mismatch}). */
    public UploadBatchFile sha256(@Nullable String sha256) {
        if (sha256 != null && !UploadParams.SHA256.matcher(sha256).matches()) {
            throw new IllegalArgumentException("sha256 must be 64 hex characters");
        }
        this.sha256 = sha256;
        return this;
    }

    /** Version you last saw of the file being replaced; stale fails the entry with {@code version_conflict}. */
    public UploadBatchFile expectedVersion(@Nullable Integer expectedVersion) {
        if (expectedVersion != null && expectedVersion < 0) {
            throw new IllegalArgumentException("expectedVersion must be non-negative");
        }
        this.expectedVersion = expectedVersion;
        return this;
    }

    public UploadBatchFile sourceModifiedAt(@Nullable Instant at) { this.sourceModifiedAt = at != null ? at.getEpochSecond() : null; return this; }
    public UploadBatchFile sourceModifiedAt(long unixSeconds) { this.sourceModifiedAt = unixSeconds; return this; }
    public UploadBatchFile sourceCreatedAt(@Nullable Instant at) { this.sourceCreatedAt = at != null ? at.getEpochSecond() : null; return this; }
    public UploadBatchFile sourceCreatedAt(long unixSeconds) { this.sourceCreatedAt = unixSeconds; return this; }

    public @NotNull String getName() { return name; }
    public @NotNull UploadSource getSource() { return source; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable String getSha256() { return sha256; }
    public @Nullable Integer getExpectedVersion() { return expectedVersion; }
    /** Unix seconds, or null. */
    public @Nullable Long getSourceModifiedAt() { return sourceModifiedAt; }
    /** Unix seconds, or null. */
    public @Nullable Long getSourceCreatedAt() { return sourceCreatedAt; }
}
