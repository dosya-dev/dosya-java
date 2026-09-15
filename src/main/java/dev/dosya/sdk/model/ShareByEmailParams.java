package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Options for emailing a share link to a file or a folder.
 *
 * @since 0.3.0
 */
public final class ShareByEmailParams {

    private final List<String> emails;
    private String message;
    private String password;
    private Integer expiresInDays;
    private Long expiresAt;
    private Boolean restrictToRecipients;

    /** Up to 50 recipients. dosya.dev addresses are refused unless the sender is a dosya.dev account. */
    public ShareByEmailParams(@NotNull List<String> emails) {
        Objects.requireNonNull(emails, "emails");
        this.emails = new ArrayList<>(emails);
    }

    /** Up to 500 characters. */
    public ShareByEmailParams message(String message) { this.message = message; return this; }
    /** At least 8 characters. */
    public ShareByEmailParams password(String password) { this.password = password; return this; }
    /** Default 30 days (or the workspace default). */
    public ShareByEmailParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    /** Unix seconds. */
    public ShareByEmailParams expiresAt(long expiresAt) { this.expiresAt = expiresAt; return this; }
    /** Only the listed recipients can open the link. */
    public ShareByEmailParams restrictToRecipients(boolean restrict) { this.restrictToRecipients = restrict; return this; }

    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("emails", emails);
        if (message != null) body.put("message", message);
        if (password != null) body.put("password", password);
        if (expiresInDays != null) body.put("expires_in_days", expiresInDays);
        if (expiresAt != null) body.put("expires_at", expiresAt);
        if (restrictToRecipients != null) body.put("restrict_to_recipients", restrictToRecipients);
        return body;
    }
}
