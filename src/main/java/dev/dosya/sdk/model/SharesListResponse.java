package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code shares().list()}: the links in a workspace and summary statistics.
 *
 * @since 0.1.0
 */
public final class SharesListResponse {

    private List<WorkspaceShareLink> links;
    private Stats stats;

    private SharesListResponse() {}

    /**
     * At most 100, newest first, revoked and expired included. Links to items hidden from
     * the caller are left out.
     *
     * @since 0.3.0 (returned {@code List<ShareLinkDetail>} before)
     */
    public @NotNull List<WorkspaceShareLink> getLinks() {
        return links != null ? Collections.unmodifiableList(links) : Collections.emptyList();
    }
    public @Nullable Stats getStats() { return stats; }

    /**
     * Summary statistics for share links in a workspace.
     *
     * @since 0.1.0
     */
    public static final class Stats {
        private int total;
        private int active;
        private int expiring;
        private long totalViews;

        private Stats() {}

        public int getTotal() { return total; }
        public int getActive() { return active; }
        public int getExpiring() { return expiring; }
        public long getTotalViews() { return totalViews; }
    }
}
