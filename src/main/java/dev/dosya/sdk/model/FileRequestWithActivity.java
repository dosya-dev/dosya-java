package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * One file request with its uploads and recipients.
 *
 * @since 0.3.0
 */
public final class FileRequestWithActivity {

    private FileRequestDetail request;
    private List<FileRequestUpload> uploads;
    private List<FileRequestRecipient> recipients;
    private String title;

    private FileRequestWithActivity() {}

    public @NotNull FileRequestDetail getRequest() { return request; }
    public @NotNull List<FileRequestUpload> getUploads() {
        return uploads != null ? Collections.unmodifiableList(uploads) : Collections.emptyList();
    }
    public @NotNull List<FileRequestRecipient> getRecipients() {
        return recipients != null ? Collections.unmodifiableList(recipients) : Collections.emptyList();
    }
    public @Nullable String getTitle() { return title; }
}
