package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The lock state of a file or folder.
 *
 * @since 0.3.0
 */
public final class LockInfo {

    private LockMode lockMode;
    private String lockedBy;
    private String lockedByName;
    private Long lockedAt;

    private LockInfo() {}

    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    public @Nullable String getLockedBy() { return lockedBy; }
    public @Nullable String getLockedByName() { return lockedByName; }
    /** Unix seconds, or null. */
    public @Nullable Long getLockedAt() { return lockedAt; }
}
