package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A share link's state after {@code shares().update()}.
 *
 * @since 0.3.0
 */
public final class UpdatedShareLink {

    private String linkId;
    private Long expiresAt;
    private boolean isPasswordProtected;
    private LockMode lockMode;
    private ShareAccessMode accessMode;
    private int recipientCount;
    private int downloadCount;
    private Integer maxDownloads;

    private UpdatedShareLink() {}

    public @NotNull String getLinkId() { return linkId; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public boolean isPasswordProtected() { return isPasswordProtected; }
    public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
    public @NotNull ShareAccessMode getAccessMode() { return accessMode != null ? accessMode : ShareAccessMode.PUBLIC; }
    public int getRecipientCount() { return recipientCount; }
    public int getDownloadCount() { return downloadCount; }
    public @Nullable Integer getMaxDownloads() { return maxDownloads; }
}
