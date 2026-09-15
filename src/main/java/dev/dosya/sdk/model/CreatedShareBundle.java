package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Result of creating a multi-file share link.
 *
 * @since 0.3.0
 */
public final class CreatedShareBundle {

    private CreatedShareLink link;
    private int sent;
    private List<String> failed;

    private CreatedShareBundle() {}

    public @NotNull CreatedShareLink getLink() { return link; }
    /** Addresses the link was emailed to (when notify was set). */
    public int getSent() { return sent; }
    /** Addresses the email could not be delivered to. */
    public @NotNull List<String> getFailed() { return failed != null ? Collections.unmodifiableList(failed) : Collections.emptyList(); }
}
