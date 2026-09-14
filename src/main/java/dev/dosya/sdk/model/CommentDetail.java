package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A comment on a file or folder.
 *
 * @since 0.1.0
 */
public final class CommentDetail {

    private String id;
    private String fileId;
    private String folderId;
    private String workspaceId;
    private String userId;
    private String parentId;
    private String body;
    private boolean isEdited;
    private long createdAt;
    private long updatedAt;
    private String userName;
    private String userEmail;
    private String userAvatar;

    private CommentDetail() {}

    public @NotNull String getId() { return id; }
    /** Set for a comment on a file. @since 0.3.0 */
    public @Nullable String getFileId() { return fileId; }
    /** Set for a comment on a folder. @since 0.3.0 */
    public @Nullable String getFolderId() { return folderId; }
    /** @since 0.3.0 */
    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getUserId() { return userId; }
    /** The comment this replies to; null for a top-level comment. */
    public @Nullable String getParentId() { return parentId; }
    public @NotNull String getBody() { return body; }
    /** @since 0.3.0 */
    public boolean isEdited() { return isEdited; }
    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }
    /** Unix seconds. @since 0.3.0 (was a nullable {@code Long}) */
    public long getUpdatedAt() { return updatedAt; }
    /** Null when the author's account is gone. @since 0.3.0 (was annotated not-null) */
    public @Nullable String getUserName() { return userName; }
    /** @since 0.3.0 */
    public @Nullable String getUserEmail() { return userEmail; }
    public @Nullable String getUserAvatar() { return userAvatar; }
}
