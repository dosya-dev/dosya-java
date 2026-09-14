package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code fileRequests().listRecipients()}.
 *
 * @since 0.3.0
 */
public final class FileRequestRecipientsResponse {

    private List<FileRequestRecipient> recipients;
    private String title;
    private String requestToken;

    private FileRequestRecipientsResponse() {}

    /** Oldest first. */
    public @NotNull List<FileRequestRecipient> getRecipients() {
        return recipients != null ? Collections.unmodifiableList(recipients) : Collections.emptyList();
    }
    public @Nullable String getTitle() { return title; }
    /** The request's public token. */
    public @NotNull String getRequestToken() { return requestToken; }
}
