package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameters for creating a new file request.
 *
 * <pre>{@code
 * CreateFileRequestParams params = new CreateFileRequestParams("ws_123")
 *     .title("Upload your documents")
 *     .message("Please submit by Friday")
 *     .expiresInDays(7)
 *     .maxFiles(5)
 *     .emails(Arrays.asList("user@example.com"));
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateFileRequestParams {

    private final String workspaceId;
    private String folderId;
    private String title;
    private String message;
    private String password;
    private Integer expiresInDays;
    private String allowedExtensions;
    private Integer maxFileSizeMb;
    private Integer maxFiles;
    private List<String> emails;

    public CreateFileRequestParams(@NotNull String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public CreateFileRequestParams folderId(String folderId) { this.folderId = folderId; return this; }
    public CreateFileRequestParams title(String title) { this.title = title; return this; }
    public CreateFileRequestParams message(String message) { this.message = message; return this; }
    public CreateFileRequestParams password(String password) { this.password = password; return this; }
    public CreateFileRequestParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    public CreateFileRequestParams allowedExtensions(String allowedExtensions) { this.allowedExtensions = allowedExtensions; return this; }
    public CreateFileRequestParams maxFileSizeMb(int maxFileSizeMb) { this.maxFileSizeMb = maxFileSizeMb; return this; }
    public CreateFileRequestParams maxFiles(int maxFiles) { this.maxFiles = maxFiles; return this; }
    public CreateFileRequestParams emails(List<String> emails) { this.emails = emails != null ? new ArrayList<String>(emails) : null; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getTitle() { return title; }
    public @Nullable String getMessage() { return message; }
    public @Nullable String getPassword() { return password; }
    public @Nullable Integer getExpiresInDays() { return expiresInDays; }
    public @Nullable String getAllowedExtensions() { return allowedExtensions; }
    public @Nullable Integer getMaxFileSizeMb() { return maxFileSizeMb; }
    public @Nullable Integer getMaxFiles() { return maxFiles; }
    public @Nullable List<String> getEmails() { return emails; }
}
