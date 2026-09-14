package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The entries of a {@code .zip} file, listed without downloading it.
 *
 * @since 0.3.0
 */
public final class ArchiveListing {

    // The route answers camelCase keys.
    @SerializedName("archiveSize")
    private long archiveSize;
    @SerializedName("totalEntries")
    private int totalEntries;
    private boolean truncated;
    private List<Entry> entries;

    private ArchiveListing() {}

    /** Size of the zip file in bytes. */
    public long getArchiveSize() { return archiveSize; }
    public int getTotalEntries() { return totalEntries; }
    /** True when the listing stops before {@link #getTotalEntries()}. */
    public boolean isTruncated() { return truncated; }
    public @NotNull List<Entry> getEntries() { return entries != null ? Collections.unmodifiableList(entries) : Collections.<Entry>emptyList(); }

    /**
     * One entry of the archive.
     *
     * @since 0.3.0
     */
    public static final class Entry {
        private int i;
        private String name;
        private long size;
        private long csize;
        private int method;
        private boolean dir;
        private boolean encrypted;
        private Long mtime;

        private Entry() {}

        /** Index to pass to {@code archiveEntry()}. */
        public int getIndex() { return i; }
        public @NotNull String getName() { return name; }
        /** Uncompressed bytes. */
        public long getSize() { return size; }
        /** Compressed bytes. */
        public long getCompressedSize() { return csize; }
        /** ZIP compression method (0 store, 8 deflate). */
        public int getMethod() { return method; }
        public boolean isDirectory() { return dir; }
        public boolean isEncrypted() { return encrypted; }
        /** Unix seconds from the entry's DOS time, or null. */
        public @Nullable Long getMtime() { return mtime; }
    }
}
