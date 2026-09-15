package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An address a file request was sent to.
 *
 * @since 0.3.0
 */
public final class FileRequestRecipient {

    private String id;
    private String email;
    private String token;
    private Long sentAt;
    private Long uploadedAt;
    private long createdAt;

    private FileRequestRecipient() {}

    /** Use with {@code fileRequests().removeRecipient()} / {@code resend()}. */
    public @NotNull String getId() { return id; }
    public @NotNull String getEmail() { return email; }
    /** The recipient's personal {@code ?r=} sub-token for the upload page. */
    public @Nullable String getToken() { return token; }
    /** Last time the request email went out; null if never sent. */
    public @Nullable Long getSentAt() { return sentAt; }
    /** First upload naming this address; null until then. */
    public @Nullable Long getUploadedAt() { return uploadedAt; }
    public long getCreatedAt() { return createdAt; }
}
