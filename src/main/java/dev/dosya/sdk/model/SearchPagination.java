package dev.dosya.sdk.model;

/**
 * Pagination of a search response. Each result set is paged separately.
 *
 * @since 0.3.0
 */
public final class SearchPagination {

    private int page;
    private int perPage;
    private long totalFiles;
    private long totalFolders;
    private long totalShares;
    private long totalRequests;
    private boolean hasMore;

    private SearchPagination() {}

    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    /** Approximate: hidden or locked rows withheld from other pages still count. */
    public long getTotalFiles() { return totalFiles; }
    /** Approximate, see {@link #getTotalFiles()}. */
    public long getTotalFolders() { return totalFolders; }
    /** Approximate, see {@link #getTotalFiles()}. */
    public long getTotalShares() { return totalShares; }
    public long getTotalRequests() { return totalRequests; }
    /** True when any result set filled the page. */
    public boolean hasMore() { return hasMore; }
}
