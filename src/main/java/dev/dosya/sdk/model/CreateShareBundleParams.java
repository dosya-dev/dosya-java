package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Options for one share link covering several files (1-100, same workspace).
 *
 * @since 0.1.0
 */
public final class CreateShareBundleParams {

    private final List<String> fileIds;
    private Integer expiresInDays;
    private Long expiresAt;
    private String password;
    private ShareAccessMode accessMode;
    private List<String> recipientEmails;
    private Boolean notify;
    private String message;

    public CreateShareBundleParams(@NotNull List<String> fileIds) {
        this.fileIds = new ArrayList<>(fileIds);
    }

    public CreateShareBundleParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    /** @since 0.3.0 */
    public CreateShareBundleParams expiresAt(long expiresAt) { this.expiresAt = expiresAt; return this; }
    public CreateShareBundleParams password(String password) { this.password = password; return this; }
    /** @since 0.3.0 */
    public CreateShareBundleParams accessMode(ShareAccessMode accessMode) { this.accessMode = accessMode; return this; }
    /** @since 0.3.0 */
    public CreateShareBundleParams recipientEmails(List<String> emails) { this.recipientEmails = new ArrayList<>(emails); return this; }
    /** Email the link to the recipients. @since 0.3.0 */
    public CreateShareBundleParams notify(boolean notify) { this.notify = notify; return this; }
    /** Up to 500 characters, included in the email. @since 0.3.0 */
    public CreateShareBundleParams message(String message) { this.message = message; return this; }

    public @NotNull List<String> getFileIds() { return fileIds; }
    public @Nullable Integer getExpiresInDays() { return expiresInDays; }
    public @Nullable String getPassword() { return password; }

    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("file_ids", fileIds);
        if (expiresInDays != null) body.put("expires_in_days", expiresInDays);
        if (expiresAt != null) body.put("expires_at", expiresAt);
        if (password != null) body.put("password", password);
        if (accessMode != null) body.put("access_mode", accessMode.value());
        if (recipientEmails != null) body.put("recipient_emails", recipientEmails);
        if (notify != null) body.put("notify", notify);
        if (message != null) body.put("message", message);
        return body;
    }
}
