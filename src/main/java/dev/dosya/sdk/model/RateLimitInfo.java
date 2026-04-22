package dev.dosya.sdk.model;

/**
 * Contains rate limit information extracted from API response headers.
 *
 * @since 0.1.0
 */
public final class RateLimitInfo {

    private final int limit;
    private final int remaining;
    private final long resetAt;

    public RateLimitInfo(int limit, int remaining, long resetAt) {
        this.limit = limit;
        this.remaining = remaining;
        this.resetAt = resetAt;
    }

    public int getLimit() { return limit; }
    public int getRemaining() { return remaining; }
    public long getResetAt() { return resetAt; }
}
