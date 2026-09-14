package dev.dosya.sdk.model;

/**
 * How much of an owner's plan storage is promised to workspace caps. All caps across
 * an owner's workspaces share one plan pool.
 *
 * @since 0.3.0
 */
public final class CapAllocation {

    private long planGb;
    private long allocatedGb;
    private long remainingGb;

    private CapAllocation() {}

    public long getPlanGb() { return planGb; }
    public long getAllocatedGb() { return allocatedGb; }
    /** What a new or raised workspace cap may still draw. */
    public long getRemainingGb() { return remainingGb; }
}
