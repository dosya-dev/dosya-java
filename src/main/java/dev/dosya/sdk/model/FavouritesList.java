package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The caller's favourite folders and files in one workspace.
 *
 * <p>Both lists are sorted by name. Trashed, hidden and lock-sealed items are left out.
 *
 * @since 0.3.0
 */
public final class FavouritesList {

    private List<FavouriteFolder> folders;
    private List<FavouriteFile> files;

    private FavouritesList() {}

    public @NotNull List<FavouriteFolder> getFolders() { return folders != null ? Collections.unmodifiableList(folders) : Collections.<FavouriteFolder>emptyList(); }
    public @NotNull List<FavouriteFile> getFiles() { return files != null ? Collections.unmodifiableList(files) : Collections.<FavouriteFile>emptyList(); }

    /** A starred folder. */
    public static final class FavouriteFolder {
        private String id;
        private String folderId;
        private long createdAt;
        private String folderName;
        private String parentId;

        private FavouriteFolder() {}

        /** Favourite id ({@code fav_...}). */
        public @NotNull String getId() { return id; }
        public @NotNull String getFolderId() { return folderId; }
        /** When it was starred (unix seconds). */
        public long getCreatedAt() { return createdAt; }
        public @NotNull String getFolderName() { return folderName; }
        /** Null for a top-level folder. */
        public @Nullable String getParentId() { return parentId; }
    }

    /** A starred file. */
    public static final class FavouriteFile {
        private String id;
        private String fileId;
        private long createdAt;
        private String fileName;
        private long sizeBytes;
        private String mimeType;
        private String extension;
        private LockMode lockMode;
        private int currentVersion;
        private String folderId;

        private FavouriteFile() {}

        /** Favourite id ({@code fav_...}). */
        public @NotNull String getId() { return id; }
        public @NotNull String getFileId() { return fileId; }
        /** When it was starred (unix seconds). */
        public long getCreatedAt() { return createdAt; }
        public @NotNull String getFileName() { return fileName; }
        public long getSizeBytes() { return sizeBytes; }
        public @NotNull String getMimeType() { return mimeType; }
        public @Nullable String getExtension() { return extension; }
        public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
        public int getCurrentVersion() { return currentVersion; }
        /** Null at the workspace root. */
        public @Nullable String getFolderId() { return folderId; }
    }
}
