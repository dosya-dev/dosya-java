package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Result of {@code folders().delete()}: either a move to the trash ({@link #isPermanent()} false)
 * or one permanent purge pass on a folder that was already in the trash ({@link #isPermanent()} true).
 *
 * @since 0.3.0
 */
public final class DeleteFolderResult {

    private boolean permanent;
    private Boolean complete;
    private Long remaining;
    private long filesAffected;
    private Integer foldersRemoved;

    private DeleteFolderResult() {}

    /** False when the subtree went to the trash; true for a permanent purge pass. */
    public boolean isPermanent() { return permanent; }

    /**
     * For a purge pass, false (HTTP 202) when the pass ran out of budget - call again to finish.
     * Always true for a trash result.
     */
    public boolean isComplete() { return complete == null || complete; }

    /** Trashed files still left to purge; {@code 0} for a trash result. */
    public long getRemaining() { return remaining != null ? remaining : 0L; }

    /** Files moved to the trash, or purged by this pass. */
    public long getFilesAffected() { return filesAffected; }

    /** Folders moved to the trash; {@code null} for a purge pass. */
    public @Nullable Integer getFoldersRemoved() { return foldersRemoved; }
}
