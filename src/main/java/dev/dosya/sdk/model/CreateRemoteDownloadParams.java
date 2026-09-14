package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for starting a remote download.
 *
 * @since 0.3.0
 */
public final class CreateRemoteDownloadParams {

    private final String workspaceId;
    private final String url;
    private String folderId;

    /**
     * @param workspaceId the destination workspace
     * @param url         a direct link to the file
     */
    public CreateRemoteDownloadParams(@NotNull String workspaceId, @NotNull String url) {
        this.workspaceId = workspaceId;
        this.url = url;
    }

    /**
     * Destination folder; the workspace root when omitted (or the member's anchor
     * folder when folder-confined).
     */
    public @NotNull CreateRemoteDownloadParams folderId(@Nullable String folderId) {
        this.folderId = folderId;
        return this;
    }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUrl() { return url; }
    public @Nullable String getFolderId() { return folderId; }

    /** The request body with the API's wire names. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("url", url);
        body.put("workspace_id", workspaceId);
        if (folderId != null) body.put("folder_id", folderId);
        return body;
    }
}
