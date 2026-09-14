package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Options for creating a share link to a file or a folder. All optional.
 *
 * @since 0.1.0
 */
public final class CreateShareLinkParams {

    private Integer expiresInDays;
    private Long expiresAt;
    private String password;
    private LockMode lockMode;
    private ShareAccessMode accessMode;
    private List<String> recipientEmails;
    private Integer maxDownloads;
    private boolean maxDownloadsSet;

    public CreateShareLinkParams() {}

    /** Days from now; {@code 0} means never, still capped by the workspace's maximum. */
    public CreateShareLinkParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    /** Unix seconds; wins over {@link #expiresInDays(int)}. @since 0.3.0 */
    public CreateShareLinkParams expiresAt(long expiresAt) { this.expiresAt = expiresAt; return this; }
    /** At least 8 characters. Required for full_lock or when the workspace forces share passwords. */
    public CreateShareLinkParams password(String password) { this.password = password; return this; }
    /** @since 0.3.0 */
    public CreateShareLinkParams lockMode(LockMode lockMode) { this.lockMode = lockMode; return this; }
    /** @deprecated use {@link #lockMode(LockMode)}. */
    @Deprecated
    public CreateShareLinkParams lockMode(String lockMode) {
        for (LockMode m : LockMode.values()) if (m.value().equals(lockMode)) this.lockMode = m;
        return this;
    }
    /** {@code RESTRICTED} requires {@link #recipientEmails(List)}. @since 0.3.0 */
    public CreateShareLinkParams accessMode(ShareAccessMode accessMode) { this.accessMode = accessMode; return this; }
    /** 1-50 addresses for a restricted link. @since 0.3.0 */
    public CreateShareLinkParams recipientEmails(List<String> recipientEmails) { this.recipientEmails = new ArrayList<>(recipientEmails); return this; }
    /** Stop serving downloads after this many (1-10000); null for unlimited. @since 0.3.0 */
    public CreateShareLinkParams maxDownloads(@Nullable Integer maxDownloads) { this.maxDownloads = maxDownloads; this.maxDownloadsSet = true; return this; }

    public @Nullable Integer getExpiresInDays() { return expiresInDays; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public @Nullable String getPassword() { return password; }
    public @Nullable LockMode getLockModeValue() { return lockMode; }
    /** @deprecated use {@link #getLockModeValue()}. */
    @Deprecated
    public @Nullable String getLockMode() { return lockMode != null ? lockMode.value() : null; }
    public @Nullable ShareAccessMode getAccessMode() { return accessMode; }
    public @Nullable List<String> getRecipientEmails() { return recipientEmails; }
    public @Nullable Integer getMaxDownloads() { return maxDownloads; }

    /** The request body, with only the fields that were set. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (expiresInDays != null) body.put("expires_in_days", expiresInDays);
        if (expiresAt != null) body.put("expires_at", expiresAt);
        if (password != null) body.put("password", password);
        if (lockMode != null) body.put("lock_mode", lockMode.value());
        if (accessMode != null) body.put("access_mode", accessMode.value());
        if (recipientEmails != null) body.put("recipient_emails", recipientEmails);
        if (maxDownloadsSet) body.put("max_downloads", maxDownloads);
        return body;
    }
}
