package dev.dosya.sdk.model;

import java.util.List;

public class SharesListResponse {

    private List<ShareLinkDetail> links;
    private Stats stats;

    private SharesListResponse() {}

    public List<ShareLinkDetail> getLinks() { return links; }
    public Stats getStats() { return stats; }

    public static class Stats {
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
