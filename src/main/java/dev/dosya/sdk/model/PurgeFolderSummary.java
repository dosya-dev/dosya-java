package dev.dosya.sdk.model;

/**
 * Outcome of a completed {@code folders().purge()}.
 *
 * @since 0.3.0
 */
public final class PurgeFolderSummary {

    private final long filesAffected;
    private final int passes;

    /** Built by the SDK. */
    public PurgeFolderSummary(long filesAffected, int passes) {
        this.filesAffected = filesAffected;
        this.passes = passes;
    }

    /** Files purged across every pass. */
    public long getFilesAffected() { return filesAffected; }
    /** Number of DELETE passes it took. */
    public int getPasses() { return passes; }
}
