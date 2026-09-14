package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Options for {@code folders().purge()}.
 *
 * @since 0.3.0
 */
public final class PurgeFolderOptions {

    private Integer maxR2Calls;
    private int maxPasses = 100;

    /** Lower the R2 call budget of each purge pass (the API never raises it). */
    public @NotNull PurgeFolderOptions maxR2Calls(@Nullable Integer maxR2Calls) { this.maxR2Calls = maxR2Calls; return this; }

    /** Give up after this many passes. Default 100. */
    public @NotNull PurgeFolderOptions maxPasses(int maxPasses) {
        if (maxPasses < 1) throw new IllegalArgumentException("maxPasses must be at least 1");
        this.maxPasses = maxPasses;
        return this;
    }

    public @Nullable Integer getMaxR2Calls() { return maxR2Calls; }
    public int getMaxPasses() { return maxPasses; }
}
