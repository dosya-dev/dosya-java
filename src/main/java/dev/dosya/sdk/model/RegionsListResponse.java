package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code regions().list()}.
 *
 * @since 0.3.0
 */
public final class RegionsListResponse {

    private List<Region> regions;
    private String suggested;

    private RegionsListResponse() {}

    /** Ordered by continent, then city. */
    public @NotNull List<Region> getRegions() {
        return regions != null ? Collections.unmodifiableList(regions) : Collections.emptyList();
    }
    /** The code nearest the caller; what the server picks when no region is given. */
    public @NotNull String getSuggested() { return suggested; }
}
