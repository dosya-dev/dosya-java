package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Files and folders to move to the trash in one call. At least one id is required.
 *
 * @since 0.3.0
 */
public final class BatchDeleteParams {

    private final String workspaceId;
    private List<String> fileIds;
    private List<String> folderIds;

    public BatchDeleteParams(@NotNull String workspaceId) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
    }

    /** Up to 500 file ids. */
    public BatchDeleteParams fileIds(@NotNull List<String> fileIds) { this.fileIds = new ArrayList<>(fileIds); return this; }
    public BatchDeleteParams folderIds(@NotNull List<String> folderIds) { this.folderIds = new ArrayList<>(folderIds); return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable List<String> getFileIds() { return fileIds; }
    public @Nullable List<String> getFolderIds() { return folderIds; }

    /** The request body, with only the id lists that were set. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        if (fileIds != null) body.put("file_ids", fileIds);
        if (folderIds != null) body.put("folder_ids", folderIds);
        return body;
    }
}
