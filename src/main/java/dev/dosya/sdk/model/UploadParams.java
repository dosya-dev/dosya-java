package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Parameters for uploading one file to a Dosya workspace.
 *
 * <p>Use the static factories to upload from a byte array, a file or a stream.
 * Files are read part by part during the upload, never loaded into memory whole.
 *
 * <pre>{@code
 * UploadParams params = UploadParams.fromPath("ws_123", Paths.get("report.pdf"))
 *     .folderId("folder_456")
 *     .computeSha256(true)
 *     .onProgress(p -> System.out.println(p.getPercent() + "%"));
 * }</pre>
 *
 * @since 0.1.0
 */
public final class UploadParams {

    static final Pattern SHA256 = Pattern.compile("^[0-9a-fA-F]{64}$");

    private final String workspaceId;
    private final String fileName;
    private final UploadSource source;
    private String mimeType;
    private String region;
    private String folderId;
    private String fileId;
    private Integer expectedVersion;
    private String sha256;
    private boolean computeSha256;
    private Long sourceModifiedAt;
    private Long sourceCreatedAt;
    private Integer concurrency;
    private Consumer<UploadProgress> onProgress;

    private UploadParams(String workspaceId, String fileName, UploadSource source) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.source = Objects.requireNonNull(source, "source");
    }

    /**
     * Uploads from any {@link UploadSource}.
     *
     * @since 0.3.0
     */
    public static @NotNull UploadParams of(@NotNull String workspaceId, @NotNull String fileName, @NotNull UploadSource source) {
        return new UploadParams(workspaceId, fileName, source);
    }

    /** Uploads bytes held in memory. The array is not copied. */
    public static @NotNull UploadParams fromBytes(@NotNull String workspaceId, @NotNull String fileName, byte @NotNull [] data) {
        return new UploadParams(workspaceId, fileName, UploadSource.ofBytes(data));
    }

    /**
     * Uploads a file under its own name. Only the size is read here.
     *
     * @throws IOException if the file's size cannot be read
     */
    public static @NotNull UploadParams fromFile(@NotNull String workspaceId, @NotNull File file) throws IOException {
        return new UploadParams(workspaceId, file.getName(), UploadSource.ofFile(file));
    }

    /**
     * Uploads a file under its own name. Only the size is read here.
     *
     * @throws IOException if the file's size cannot be read
     * @since 0.3.0
     */
    public static @NotNull UploadParams fromPath(@NotNull String workspaceId, @NotNull Path path) throws IOException {
        Path name = path.getFileName();
        return new UploadParams(workspaceId, name != null ? name.toString() : path.toString(), UploadSource.ofPath(path));
    }

    /**
     * Uploads a stream of {@code fileSize} bytes, read part by part (buffered only for
     * files up to 50 MiB, which go in one request). A {@code fileSize} of 0 or less
     * keeps the 0.2 behaviour: the stream is read into memory to learn its size.
     *
     * @throws IOException if the size is unknown and reading the stream fails
     */
    public static @NotNull UploadParams fromStream(@NotNull String workspaceId, @NotNull String fileName,
                                                   @NotNull InputStream stream, long fileSize) throws IOException {
        if (fileSize > 0) return new UploadParams(workspaceId, fileName, UploadSource.ofStream(stream, fileSize));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        stream.transferTo(out);
        return new UploadParams(workspaceId, fileName, UploadSource.ofBytes(out.toByteArray()));
    }

    /**
     * Echoed back by init only. The server derives the stored MIME type from the file
     * name's extension, so this does not change how the file is served.
     */
    public UploadParams mimeType(@Nullable String mimeType) { this.mimeType = mimeType; return this; }

    /**
     * @deprecated ignored by the server since 2026-09-07 and no longer sent: files land in
     * the workspace's own region.
     */
    @Deprecated
    public UploadParams region(@Nullable String region) { this.region = region; return this; }

    /** Destination folder; null for the workspace root (or your confined folder). */
    public UploadParams folderId(@Nullable String folderId) { this.folderId = folderId; return this; }

    /** Uploads as a new version of this file. */
    public UploadParams fileId(@Nullable String fileId) { this.fileId = fileId; return this; }

    /**
     * Optimistic concurrency: the version you last saw of the file being replaced (the
     * explicit {@code fileId}, or the same-name file that would be adopted). A stale value
     * fails with 409 code {@code version_conflict} and {@code details.current_version}.
     *
     * @since 0.3.0
     */
    public UploadParams expectedVersion(@Nullable Integer expectedVersion) {
        if (expectedVersion != null && expectedVersion < 0) {
            throw new IllegalArgumentException("expectedVersion must be non-negative");
        }
        this.expectedVersion = expectedVersion;
        return this;
    }

    /**
     * Hex SHA-256 of the bytes, sent as {@code X-Dosya-Sha256} on a single-request upload
     * (files up to 50 MiB) and verified server-side (400 {@code hash_mismatch}). Multipart
     * uploads cannot be verified by the server, so it is not sent there.
     *
     * @since 0.3.0
     */
    public UploadParams sha256(@Nullable String sha256) {
        if (sha256 != null && !SHA256.matcher(sha256).matches()) {
            throw new IllegalArgumentException("sha256 must be 64 hex characters");
        }
        this.sha256 = sha256;
        return this;
    }

    /**
     * Computes the SHA-256 and sends it, on the single-request path only (files up to 50 MiB).
     *
     * @since 0.3.0
     */
    public UploadParams computeSha256(boolean computeSha256) { this.computeSha256 = computeSha256; return this; }

    /**
     * Original modification time, stored as the file's source date.
     *
     * @since 0.3.0
     */
    public UploadParams sourceModifiedAt(@Nullable Instant at) { this.sourceModifiedAt = at != null ? at.getEpochSecond() : null; return this; }

    /**
     * Original modification time in unix seconds.
     *
     * @since 0.3.0
     */
    public UploadParams sourceModifiedAt(long unixSeconds) { this.sourceModifiedAt = unixSeconds; return this; }

    /**
     * Original creation time.
     *
     * @since 0.3.0
     */
    public UploadParams sourceCreatedAt(@Nullable Instant at) { this.sourceCreatedAt = at != null ? at.getEpochSecond() : null; return this; }

    /**
     * Original creation time in unix seconds.
     *
     * @since 0.3.0
     */
    public UploadParams sourceCreatedAt(long unixSeconds) { this.sourceCreatedAt = unixSeconds; return this; }

    /**
     * Parts in flight for a multipart upload. Default 3. Streams always use 1.
     *
     * @since 0.3.0
     */
    public UploadParams concurrency(int concurrency) {
        if (concurrency < 1) throw new IllegalArgumentException("concurrency must be a positive integer, got " + concurrency);
        this.concurrency = concurrency;
        return this;
    }

    /** Progress callback. Called from upload worker threads for multipart uploads. */
    public UploadParams onProgress(@Nullable Consumer<UploadProgress> onProgress) { this.onProgress = onProgress; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getFileName() { return fileName; }
    public long getFileSize() { return source.getSize(); }
    /**
     * The upload's bytes.
     *
     * @since 0.3.0
     */
    public @NotNull UploadSource getSource() { return source; }
    public @Nullable String getMimeType() { return mimeType; }
    /** @deprecated ignored by the server. */
    @Deprecated
    public @Nullable String getRegion() { return region; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable Integer getExpectedVersion() { return expectedVersion; }
    public @Nullable String getSha256() { return sha256; }
    public boolean isComputeSha256() { return computeSha256; }
    /** Unix seconds, or null. */
    public @Nullable Long getSourceModifiedAt() { return sourceModifiedAt; }
    /** Unix seconds, or null. */
    public @Nullable Long getSourceCreatedAt() { return sourceCreatedAt; }
    public @Nullable Integer getConcurrency() { return concurrency; }
    public @Nullable Consumer<UploadProgress> getOnProgress() { return onProgress; }

    /**
     * Returns a copy of the whole content.
     *
     * @deprecated reads a file or stream source into memory; use {@link #getSource()}.
     */
    @Deprecated
    public byte @NotNull [] getBody() {
        if (source.getSize() > Integer.MAX_VALUE - 8) throw new IllegalStateException("Source is too large for a byte array");
        try {
            byte[] all = source.read(0, (int) source.getSize());
            return Arrays.copyOf(all, all.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
