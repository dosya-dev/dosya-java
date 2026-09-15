package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The outcome of {@code upload().batch(...)}: one entry per input file, in input order.
 *
 * @since 0.3.0
 */
public final class UploadBatchResult {

    private final List<Entry> results;

    public UploadBatchResult(@NotNull List<Entry> results) {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    /** One entry per input file, in input order. */
    public @NotNull List<Entry> getResults() { return results; }

    /**
     * One file's outcome.
     *
     * @since 0.3.0
     */
    public static final class Entry {
        private final boolean ok;
        private final String fileId;
        private final String name;
        private final Integer version;
        private final String error;
        private final Integer currentVersion;

        private Entry(boolean ok, String fileId, String name, Integer version, String error, Integer currentVersion) {
            this.ok = ok;
            this.fileId = fileId;
            this.name = name;
            this.version = version;
            this.error = error;
            this.currentVersion = currentVersion;
        }

        /** A stored file. */
        public static @NotNull Entry success(@NotNull String fileId, @NotNull String name, int version) {
            return new Entry(true, fileId, name, version, null, null);
        }

        /** A refused file. */
        public static @NotNull Entry failure(@NotNull String error, @Nullable Integer currentVersion) {
            return new Entry(false, null, null, null, error, currentVersion);
        }

        public boolean isOk() { return ok; }
        /** The stored file's id, when ok. */
        public @Nullable String getFileId() { return fileId; }
        /** The stored (sanitized) name, when ok. */
        public @Nullable String getName() { return name; }
        /** Greater than 1 when a same-name file was adopted as a new version; null when refused. */
        public @Nullable Integer getVersion() { return version; }
        /** Message or machine code ({@code version_conflict}, {@code hash_mismatch}, {@code folder_locked}) when refused. */
        public @Nullable String getError() { return error; }
        /** With {@code version_conflict}. */
        public @Nullable Integer getCurrentVersion() { return currentVersion; }
    }
}
