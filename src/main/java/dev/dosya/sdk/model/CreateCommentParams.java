package dev.dosya.sdk.model;

public class CreateCommentParams {

    private final String workspaceId;
    private final String body;
    private String fileId;
    private String folderId;
    private String parentId;

    public CreateCommentParams(String workspaceId, String body) {
        this.workspaceId = workspaceId;
        this.body = body;
    }

    public CreateCommentParams fileId(String fileId) { this.fileId = fileId; return this; }
    public CreateCommentParams folderId(String folderId) { this.folderId = folderId; return this; }
    public CreateCommentParams parentId(String parentId) { this.parentId = parentId; return this; }

    public String getWorkspaceId() { return workspaceId; }
    public String getBody() { return body; }
    public String getFileId() { return fileId; }
    public String getFolderId() { return folderId; }
    public String getParentId() { return parentId; }
}
