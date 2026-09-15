package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Temporary access to a {@code full_lock} item, from unlocking it with its password.
 * Unlocking does not remove the lock.
 *
 * @since 0.3.0
 */
public final class UnlockGrant {

    private String unlockToken;
    private long expiresAt;

    private UnlockGrant() {}

    /** Pass as the unlock token to download, raw and thumbnail calls. Valid for one hour. */
    public @NotNull String getUnlockToken() { return unlockToken; }
    /** Unix seconds. */
    public long getExpiresAt() { return expiresAt; }
}
