package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Result of {@code workspaces().requestDeletion()}: a code was emailed to the owner.
 *
 * @since 0.3.0
 */
public final class WorkspaceDeletionRequest {

    private long expiresAt;
    private String sentTo;

    private WorkspaceDeletionRequest() {}

    /** Unix seconds; the code is valid for 15 minutes. */
    public long getExpiresAt() { return expiresAt; }
    /** The owner's email address the code was sent to. */
    public @NotNull String getSentTo() { return sentTo; }
}
