package dev.dosya.sdk.model;

import java.util.List;

public class CreateFileRequestParams {

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

    public CreateFileRequestParams(String workspaceId) {
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
    public CreateFileRequestParams emails(List<String> emails) { this.emails = emails; return this; }

    public String getWorkspaceId() { return workspaceId; }
    public String getFolderId() { return folderId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getPassword() { return password; }
    public Integer getExpiresInDays() { return expiresInDays; }
    public String getAllowedExtensions() { return allowedExtensions; }
    public Integer getMaxFileSizeMb() { return maxFileSizeMb; }
    public Integer getMaxFiles() { return maxFiles; }
    public List<String> getEmails() { return emails; }
}
