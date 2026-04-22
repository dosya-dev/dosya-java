package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Configuration settings for a Dosya workspace.
 *
 * <p>Controls upload limits, allowed/blocked extensions, security policies,
 * and share link settings.</p>
 *
 * @since 0.1.0
 */
public final class WorkspaceSettings {

    private double maxFileSizeGb;
    private double maxTotalStorageGb;
    private double maxStoragePerMemberGb;
    private int maxConcurrentUploads;
    private String allowedExtensions;
    private String blockedExtensions;
    private int require2fa;
    private int disableShareLinks;
    private int forceSharePassword;
    private Integer shareMaxExpiryDays;

    private WorkspaceSettings() {}

    public double getMaxFileSizeGb() { return maxFileSizeGb; }
    public double getMaxTotalStorageGb() { return maxTotalStorageGb; }
    public double getMaxStoragePerMemberGb() { return maxStoragePerMemberGb; }
    public int getMaxConcurrentUploads() { return maxConcurrentUploads; }
    public @Nullable String getAllowedExtensions() { return allowedExtensions; }
    public @Nullable String getBlockedExtensions() { return blockedExtensions; }
    public int getRequire2fa() { return require2fa; }
    public int getDisableShareLinks() { return disableShareLinks; }
    public int getForceSharePassword() { return forceSharePassword; }
    public @Nullable Integer getShareMaxExpiryDays() { return shareMaxExpiryDays; }
}
