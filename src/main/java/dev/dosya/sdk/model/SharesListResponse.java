package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response returned when listing share links for a workspace.
 *
 * <p>Contains the list of share links and summary statistics.</p>
 *
 * @since 0.1.0
 */
public final class SharesListResponse {

    private List<ShareLinkDetail> links;
    private Stats stats;

    private SharesListResponse() {}

    public @NotNull List<ShareLinkDetail> getLinks() { return links != null ? Collections.unmodifiableList(links) : Collections.<ShareLinkDetail>emptyList(); }
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
        private int totalViews;

        private Stats() {}

        public int getTotal() { return total; }
        public int getActive() { return active; }
        public int getExpiring() { return expiring; }
        public int getTotalViews() { return totalViews; }
    }
}
