package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Result of editing a comment.
 *
 * @since 0.3.0
 */
public final class CommentEditResult {

    private String body;
    private long updatedAt;

    private CommentEditResult() {}

    /** The stored body. */
    public @NotNull String getBody() { return body; }
    /** Unix seconds. */
    public long getUpdatedAt() { return updatedAt; }
}
