package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fields to change on a live share link. Only the fields whose setter was called are sent;
 * at least one is required.
 *
 * <pre>{@code
 * new UpdateShareLinkParams().expiresInDays(7).clearPassword();
 * }</pre>
 *
 * @since 0.3.0
 */
public final class UpdateShareLinkParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public UpdateShareLinkParams() {}

    /**
     * Unix seconds, at least a minute ahead and at most 3650 days out. A non-null value wins over
     * {@code expiresInDays}; null means never unless {@code expiresInDays} is also set.
     */
    public @NotNull UpdateShareLinkParams expiresAt(@Nullable Long expiresAt) { fields.put("expires_at", expiresAt); return this; }
    /** Days from now; 0 or null means never, still capped by the workspace's maximum. */
    public @NotNull UpdateShareLinkParams expiresInDays(@Nullable Integer days) { fields.put("expires_in_days", days); return this; }
    /** At least 8 characters. Null or empty removes the password. */
    public @NotNull UpdateShareLinkParams password(@Nullable String password) { fields.put("password", password); return this; }
    /** Removes the password. */
    public @NotNull UpdateShareLinkParams clearPassword() { return password(null); }
    public @NotNull UpdateShareLinkParams lockMode(@NotNull LockMode lockMode) {
        fields.put("lock_mode", Objects.requireNonNull(lockMode, "lockMode").value());
        return this;
    }
    public @NotNull UpdateShareLinkParams accessMode(@NotNull ShareAccessMode accessMode) {
        fields.put("access_mode", Objects.requireNonNull(accessMode, "accessMode").value());
        return this;
    }
    /** Replaces the whole recipient list (1-50 addresses for a restricted link). */
    public @NotNull UpdateShareLinkParams recipientEmails(@NotNull List<String> emails) {
        fields.put("recipient_emails", new ArrayList<>(Objects.requireNonNull(emails, "emails")));
        return this;
    }
    /** 1-10 000, or null for unlimited. */
    public @NotNull UpdateShareLinkParams maxDownloads(@Nullable Integer maxDownloads) { fields.put("max_downloads", maxDownloads); return this; }

    /** The request body, with only the fields that were set. */
    public @NotNull Map<String, Object> toBody() {
        return new LinkedHashMap<>(fields);
    }
}
