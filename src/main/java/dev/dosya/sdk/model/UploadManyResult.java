package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One file's outcome from {@code upload().many(...)}.
 *
 * @since 0.3.0
 */
public final class UploadManyResult {

    private final boolean ok;
    private final String name;
    private final String fileId;
    private final Integer version;
    private final UploadResult.UploadedFile file;
    private final String error;
    private final String code;
    private final Integer currentVersion;
    private final Throwable cause;

    private UploadManyResult(boolean ok, String name, String fileId, Integer version, UploadResult.UploadedFile file,
                             String error, String code, Integer currentVersion, Throwable cause) {
        this.ok = ok;
        this.name = name;
        this.fileId = fileId;
        this.version = version;
        this.file = file;
        this.error = error;
        this.code = code;
        this.currentVersion = currentVersion;
        this.cause = cause;
    }

    /** A stored file; {@code file} is set for files that went through {@code upload().file()}. */
    public static @NotNull UploadManyResult success(@NotNull String name, @NotNull String fileId, int version,
                                                    @Nullable UploadResult.UploadedFile file) {
        return new UploadManyResult(true, name, fileId, version, file, null, null, null, null);
    }

    /** A failed file. */
    public static @NotNull UploadManyResult failure(@NotNull String name, @NotNull String error, @Nullable String code,
                                                    @Nullable Integer currentVersion, @Nullable Throwable cause) {
        return new UploadManyResult(false, name, null, null, null, error, code, currentVersion, cause);
    }

    public boolean isOk() { return ok; }
    /** The stored name when ok, the input name otherwise. */
    public @NotNull String getName() { return name; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable Integer getVersion() { return version; }
    /** The full row, for files that went through {@code upload().file()}. */
    public @Nullable UploadResult.UploadedFile getFile() { return file; }
    public @Nullable String getError() { return error; }
    /** Machine code when there is one ({@code version_conflict}, {@code concurrent_upload_limit}). */
    public @Nullable String getCode() { return code; }
    public @Nullable Integer getCurrentVersion() { return currentVersion; }
    /** The underlying exception for whole-request or transport failures. */
    public @Nullable Throwable getCause() { return cause; }
}
