package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Result of {@code workspaces().delete()}.
 *
 * @since 0.3.0
 */
public final class DeleteWorkspaceResult {

    private boolean pending;
    private String operationId;

    private DeleteWorkspaceResult() {}

    /** True when the deletion was accepted but is still running (HTTP 202). */
    public boolean isPending() { return pending; }
    /** The background operation id when pending, else null. */
    public @Nullable String getOperationId() { return operationId; }
}
