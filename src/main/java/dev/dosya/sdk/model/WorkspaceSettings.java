package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

/**
 * The full {@code workspace_settings} row, as returned by {@code workspaces().get()}.
 *
 * <p>Size limits are whole GB, null meaning unlimited. The IP, country and email-domain
 * lists are JSON array text as stored (for example {@code ["192.168.1.0/24"]}).
 * Change settings with {@code workspaces().updateSettings()}.
 *
 * @since 0.1.0
 */
public final class WorkspaceSettings {

    private String workspaceId;
    private Double maxFileSizeGb;
    private Double maxStoragePerMemberGb;
    private Double maxTotalStorageGb;
    private Integer maxConcurrentUploads;
    private String allowedExtensions;
    private String blockedExtensions;
    private boolean notifyOnUpload;
    private boolean autoShareLink;
    private boolean downloadTracking;
    private String ipAllowlist;
    private String ipBlocklist;
    private String countryAllowlist;
    private String countryBlocklist;
    private Integer sessionTimeoutMinutes;
    private boolean disableShareLinks;
    private boolean forceSharePassword;
    private Integer shareMaxExpiryDays;
    private Integer defaultShareExpiryDays;
    @SerializedName("require_2fa")
    private boolean require2fa;
    private String allowedEmailDomains;
    private boolean disablePasswordLogin;
    private Integer downloadRateLimit;
    private boolean downloadWatermark;
    private Integer maxVersions;
    private String availableRegions;
    private boolean recordUploadOrigin;
    private boolean duplicateScanEnabled;
    private Long createdAt;
    private Long updatedAt;

    private WorkspaceSettings() {}

    /** @since 0.3.0 */
    public @Nullable String getWorkspaceId() { return workspaceId; }
    /** Per-file cap in GB; null = unlimited. */
    public @Nullable Double getMaxFileSizeGb() { return maxFileSizeGb; }
    public @Nullable Double getMaxStoragePerMemberGb() { return maxStoragePerMemberGb; }
    public @Nullable Double getMaxTotalStorageGb() { return maxTotalStorageGb; }
    /** Open upload sessions one member may hold; null or 0 = no limit. */
    public @Nullable Integer getMaxConcurrentUploads() { return maxConcurrentUploads; }
    /** Comma-separated, dot-prefixed, e.g. {@code ".pdf,.mp4"}. */
    public @Nullable String getAllowedExtensions() { return allowedExtensions; }
    public @Nullable String getBlockedExtensions() { return blockedExtensions; }
    /** @since 0.3.0 */
    public boolean isNotifyOnUpload() { return notifyOnUpload; }
    /** @since 0.3.0 */
    public boolean isAutoShareLink() { return autoShareLink; }
    /** @since 0.3.0 */
    public boolean isDownloadTracking() { return downloadTracking; }
    /** JSON array text of CIDRs. @since 0.3.0 */
    public @Nullable String getIpAllowlist() { return ipAllowlist; }
    /** @since 0.3.0 */
    public @Nullable String getIpBlocklist() { return ipBlocklist; }
    /** JSON array text of ISO country codes. @since 0.3.0 */
    public @Nullable String getCountryAllowlist() { return countryAllowlist; }
    /** @since 0.3.0 */
    public @Nullable String getCountryBlocklist() { return countryBlocklist; }
    /** @since 0.3.0 */
    public @Nullable Integer getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public boolean isDisableShareLinks() { return disableShareLinks; }
    public boolean isForceSharePassword() { return forceSharePassword; }
    public @Nullable Integer getShareMaxExpiryDays() { return shareMaxExpiryDays; }
    /** @since 0.3.0 */
    public @Nullable Integer getDefaultShareExpiryDays() { return defaultShareExpiryDays; }
    public boolean isRequire2fa() { return require2fa; }
    /** JSON array text of domains invites are limited to. @since 0.3.0 */
    public @Nullable String getAllowedEmailDomains() { return allowedEmailDomains; }
    /** @since 0.3.0 */
    public boolean isDisablePasswordLogin() { return disablePasswordLogin; }
    /** Downloads per hour per user; null = unlimited. @since 0.3.0 */
    public @Nullable Integer getDownloadRateLimit() { return downloadRateLimit; }
    /** @since 0.3.0 */
    public boolean isDownloadWatermark() { return downloadWatermark; }
    /** @since 0.3.0 */
    public @Nullable Integer getMaxVersions() { return maxVersions; }
    /** Ignored by the API since workspace locations became fixed. @since 0.3.0 */
    public @Nullable String getAvailableRegions() { return availableRegions; }
    /** @since 0.3.0 */
    public boolean isRecordUploadOrigin() { return recordUploadOrigin; }
    /** @since 0.3.0 */
    public boolean isDuplicateScanEnabled() { return duplicateScanEnabled; }
    /** @since 0.3.0 */
    public @Nullable Long getCreatedAt() { return createdAt; }
    /** @since 0.3.0 */
    public @Nullable Long getUpdatedAt() { return updatedAt; }
}
