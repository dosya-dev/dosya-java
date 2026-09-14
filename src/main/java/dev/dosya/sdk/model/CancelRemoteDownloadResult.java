package dev.dosya.sdk.model;

/**
 * The outcome of cancelling or dismissing a remote-download job.
 *
 * @since 0.3.0
 */
public final class CancelRemoteDownloadResult {

    private boolean cancelled;
    private boolean dismissed;

    private CancelRemoteDownloadResult() {}

    /** {@code true} when an active job was stopped; {@code false} if it finished during the race. */
    public boolean isCancelled() { return cancelled; }

    /** {@code true} when a finished job was removed from the list instead. */
    public boolean isDismissed() { return dismissed; }
}
