package dev.dosya.sdk.model;

/**
 * Pagination of an activity log page.
 *
 * @since 0.3.0
 */
public final class ActivityPagination {

    private int page;
    private int perPage;
    private long total;
    private int totalPages;

    private ActivityPagination() {}

    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    /** Matching entries across all pages. */
    public long getTotal() { return total; }
    /** At least 1, also when there are no entries. */
    public int getTotalPages() { return totalPages; }
}
