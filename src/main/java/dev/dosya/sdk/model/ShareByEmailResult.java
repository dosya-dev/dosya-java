package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Result of emailing a share link.
 *
 * @since 0.3.0
 */
public final class ShareByEmailResult {

    private String shareUrl;
    private ShareAccessMode accessMode;
    private int sent;
    private List<String> failed;

    private ShareByEmailResult() {}

    public @NotNull String getShareUrl() { return shareUrl; }
    public @NotNull ShareAccessMode getAccessMode() { return accessMode != null ? accessMode : ShareAccessMode.PUBLIC; }
    public int getSent() { return sent; }
    /** Addresses the email could not be delivered to. */
    public @NotNull List<String> getFailed() { return failed != null ? Collections.unmodifiableList(failed) : Collections.emptyList(); }
}
