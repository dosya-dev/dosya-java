package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for {@code upload().init(...)}: opening an upload session by hand.
 *
 * @since 0.3.0
 */
public final class UploadInitParams {

    private final String workspaceId;
    private final String fileName;
    private final long fileSize;
    private String mimeType;
    private String folderId;
    private String fileId;
    private Integer expectedVersion;

    /**
     * @param workspaceId destination workspace
     * @param fileName    file name (the server sanitizes it)
     * @param fileSize    exact size in bytes
     */
    public UploadInitParams(@NotNull String workspaceId, @NotNull String fileName, long fileSize) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        if (fileSize < 0) throw new IllegalArgumentException("fileSize must be non-negative, got " + fileSize);
        this.fileSize = fileSize;
    }

    /** Echoed back only; the stored type comes from the extension. */
    public UploadInitParams mimeType(@Nullable String mimeType) { this.mimeType = mimeType; return this; }

    /** Destination folder; null for the workspace root. */
    public UploadInitParams folderId(@Nullable String folderId) { this.folderId = folderId; return this; }

    /** Upload as a new version of this file. */
    public UploadInitParams fileId(@Nullable String fileId) { this.fileId = fileId; return this; }

    /** Version you last saw of the file being replaced; stale gives 409 {@code version_conflict}. */
    public UploadInitParams expectedVersion(@Nullable Integer expectedVersion) {
        if (expectedVersion != null && expectedVersion < 0) {
            throw new IllegalArgumentException("expectedVersion must be non-negative");
        }
        this.expectedVersion = expectedVersion;
        return this;
    }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }

    /** The init request body with the API's wire names; only set values are included. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        body.put("file_name", fileName);
        body.put("file_size", fileSize);
        if (mimeType != null) body.put("mime_type", mimeType);
        if (folderId != null) body.put("folder_id", folderId);
        if (fileId != null) body.put("file_id", fileId);
        if (expectedVersion != null) body.put("expected_version", expectedVersion);
        return body;
    }
}
