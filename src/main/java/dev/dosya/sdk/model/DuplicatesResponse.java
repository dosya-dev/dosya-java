package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Groups of identical files (same content hash) in a workspace.
 *
 * @since 0.3.0
 */
public final class DuplicatesResponse {

    private boolean scanEnabled;
    private List<Group> groups;
    private int totalGroups;
    private long totalWastedBytes;
    private Scanning scanning;

    private DuplicatesResponse() {}

    /** False when the workspace has duplicate scanning turned off; everything else is then empty. */
    public boolean isScanEnabled() { return scanEnabled; }
    /** Up to 100 groups, most wasted bytes first. */
    public @NotNull List<Group> getGroups() { return groups != null ? Collections.unmodifiableList(groups) : Collections.<Group>emptyList(); }
    /** Over every group, not just the returned ones. */
    public int getTotalGroups() { return totalGroups; }
    /** Over every group, not just the returned ones. */
    public long getTotalWastedBytes() { return totalWastedBytes; }
    /** Files still waiting to be hashed. */
    public int getPendingScans() { return scanning != null ? scanning.pending : 0; }

    /** Files sharing one content hash. */
    public static final class Group {
        private String contentHash;
        private long sizeBytes;
        private int count;
        private long wastedBytes;
        private List<DuplicateFile> files;

        private Group() {}

        public @NotNull String getContentHash() { return contentHash; }
        /** Size of each copy. */
        public long getSizeBytes() { return sizeBytes; }
        public int getCount() { return count; }
        /** {@code (count - 1) * sizeBytes}. */
        public long getWastedBytes() { return wastedBytes; }
        /** Newest first. */
        public @NotNull List<DuplicateFile> getFiles() { return files != null ? Collections.unmodifiableList(files) : Collections.<DuplicateFile>emptyList(); }
    }

    /** One copy in a duplicate group. */
    public static final class DuplicateFile {
        private String id;
        private String name;
        private String folderId;
        private long createdAt;
        private String uploadedBy;
        private String uploaderName;
        private String mimeType;
        private String extension;
        private String contentHash;
        private long sizeBytes;
        private String folderPath;

        private DuplicateFile() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        /** Null at the workspace root. */
        public @Nullable String getFolderId() { return folderId; }
        /** Unix seconds. */
        public long getCreatedAt() { return createdAt; }
        public @NotNull String getUploadedBy() { return uploadedBy; }
        public @Nullable String getUploaderName() { return uploaderName; }
        public @NotNull String getMimeType() { return mimeType; }
        public @Nullable String getExtension() { return extension; }
        public @NotNull String getContentHash() { return contentHash; }
        public long getSizeBytes() { return sizeBytes; }
        /** Folder path such as {@code "Projects / 2026"}; null at the root. */
        public @Nullable String getFolderPath() { return folderPath; }
    }

    private static final class Scanning {
        private int pending;
    }
}
