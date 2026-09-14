package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * The share-relevant workspace settings any member may read.
 *
 * @since 0.3.0
 */
public final class WorkspaceShareSettings {

    private Integer defaultShareExpiryDays;
    private Integer shareMaxExpiryDays;
    private boolean disableShareLinks;
    private boolean forceSharePassword;

    private WorkspaceShareSettings() {}

    public @Nullable Integer getDefaultShareExpiryDays() { return defaultShareExpiryDays; }
    public @Nullable Integer getShareMaxExpiryDays() { return shareMaxExpiryDays; }
    public boolean isDisableShareLinks() { return disableShareLinks; }
    public boolean isForceSharePassword() { return forceSharePassword; }
}
