package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Detailed information about a comment on a file or folder.
 *
 * @since 0.1.0
 */
public final class CommentDetail {

    private String id;
    private String body;
    private String userId;
    private String userName;
    private String userAvatar;
    private String parentId;
    private long createdAt;
    private Long updatedAt;

    private CommentDetail() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getBody() { return body; }
    public @NotNull String getUserId() { return userId; }
    public @NotNull String getUserName() { return userName; }
    public @Nullable String getUserAvatar() { return userAvatar; }
    public @Nullable String getParentId() { return parentId; }
    public long getCreatedAt() { return createdAt; }
    public @Nullable Long getUpdatedAt() { return updatedAt; }
}
