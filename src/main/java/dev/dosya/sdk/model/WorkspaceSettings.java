package dev.dosya.sdk.model;

public class WorkspaceSettings {

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
    public String getAllowedExtensions() { return allowedExtensions; }
    public String getBlockedExtensions() { return blockedExtensions; }
    public int getRequire2fa() { return require2fa; }
    public int getDisableShareLinks() { return disableShareLinks; }
    public int getForceSharePassword() { return forceSharePassword; }
    public Integer getShareMaxExpiryDays() { return shareMaxExpiryDays; }
}
