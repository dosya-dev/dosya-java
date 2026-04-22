package dev.dosya.sdk.model;

public class RateLimitInfo {

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
