package dev.dosya.sdk.model;

public class CommentDetail {

    private String id;
    private String body;
    private String userId;
    private String userName;
    private String userAvatar;
    private String parentId;
    private long createdAt;
    private Long updatedAt;

    private CommentDetail() {}

    public String getId() { return id; }
    public String getBody() { return body; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getParentId() { return parentId; }
    public long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
}
