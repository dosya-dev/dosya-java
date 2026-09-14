package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code shares().withMe()}.
 *
 * @since 0.3.0
 */
public final class SharedWithMeResponse {

    private List<SharedWithMeLink> links;
    private boolean emailVerified;
    private Stats stats;

    private SharedWithMeResponse() {}

    /** At most 100, newest invitation first. Empty when the account email is unverified. */
    public @NotNull List<SharedWithMeLink> getLinks() {
        return links != null ? Collections.unmodifiableList(links) : Collections.emptyList();
    }
    public boolean isEmailVerified() { return emailVerified; }
    /** Null when the email is unverified. */
    public @Nullable Stats getStats() { return stats; }

    /** Counts over the listed links. */
    public static final class Stats {
        private int total;
        private int active;
        private int unopened;

        private Stats() {}

        public int getTotal() { return total; }
        public int getActive() { return active; }
        public int getUnopened() { return unopened; }
    }
}
