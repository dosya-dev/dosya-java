package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A link someone sent to the caller's verified email address.
 *
 * @since 0.3.0
 */
public final class SharedWithMeLink {

    private String linkId;
    private String url;
    private String displayName;
    private boolean isFolder;
    private boolean isBundle;
    private Long sizeBytes;
    private String extension;
    private boolean isPasswordProtected;
    private LockMode lockMode;
    private Long expiresAt;
    private Long revokedAt;
    private ShareStatus status;
    private long sharedAt;
    private long invitedAt;
    private Long verifiedAt;
    private String senderName;

    private SharedWithMeLink() {}

    public @NotNull String getLinkId() { return linkId; }
    public @NotNull String getUrl() { return url; }
    public @NotNull String getDisplayName() { return displayName; }
    public boolean isFolder() { return isFolder; }
    public boolean isBundle() { return isBundle; }
    public @Nullable Long getSizeBytes() { return sizeBytes; }
    public @Nullable String getExtension() { return extension; }
    public boolean isPasswordProtected() { return isPasswordProtected; }
    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public @Nullable Long getRevokedAt() { return revokedAt; }
    public @Nullable ShareStatus getStatus() { return status; }
    public long getSharedAt() { return sharedAt; }
    public long getInvitedAt() { return invitedAt; }
    /** When the caller first verified this address for the link; null if never opened. */
    public @Nullable Long getVerifiedAt() { return verifiedAt; }
    public @Nullable String getSenderName() { return senderName; }
}
