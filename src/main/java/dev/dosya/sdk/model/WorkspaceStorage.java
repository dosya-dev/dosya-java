package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Storage figures for one workspace, as the upload gate would judge them.
 *
 * <p>{@link #getFree()} is what an upload can actually use: it is bounded by both the
 * workspace cap and the owner's remaining account storage, so print it instead of
 * {@code total - used}.
 *
 * @since 0.3.0
 */
public final class WorkspaceStorage {

    private long used;
    private long total;
    private long free;
    private Long capBytes;
    private long accountLimitBytes;
    private long accountUsedBytes;

    private WorkspaceStorage() {}

    /** Bytes stored in this workspace. */
    public long getUsed() { return used; }
    /** The workspace cap when set, else the owner's account limit. */
    public long getTotal() { return total; }
    /** Bytes an upload can still use. */
    public long getFree() { return free; }
    /** The workspace cap in bytes, or null when uncapped. */
    public @Nullable Long getCapBytes() { return capBytes; }
    public long getAccountLimitBytes() { return accountLimitBytes; }
    public long getAccountUsedBytes() { return accountUsedBytes; }
}
