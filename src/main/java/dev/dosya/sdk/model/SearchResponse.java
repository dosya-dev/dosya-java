package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Search results: matched files, folders, share links and file requests.
 *
 * @since 0.1.0
 */
public final class SearchResponse {

    private String query;
    private String ext;
    private List<SearchFile> files;
    private List<SearchFolder> folders;
    private List<SearchShared> shared;
    private List<SearchFileRequest> fileRequests;
    private SearchPagination pagination;

    private SearchResponse() {}

    /** The query as searched (lowercased, {@code ext:} token included). */
    public @NotNull String getQuery() { return query; }
    /** The {@code ext:} filter parsed from the query, without its dot. @since 0.3.0 */
    public @Nullable String getExt() { return ext; }
    public @NotNull List<SearchFile> getFiles() { return list(files); }
    public @NotNull List<SearchFolder> getFolders() { return list(folders); }
    public @NotNull List<SearchShared> getShared() { return list(shared); }
    public @NotNull List<SearchFileRequest> getFileRequests() { return list(fileRequests); }
    /** @since 0.3.0 (was the file-listing {@code Pagination}, which never matched this response) */
    public @NotNull SearchPagination getPagination() { return pagination; }

    private static <T> List<T> list(List<T> l) {
        return l != null ? Collections.unmodifiableList(l) : Collections.emptyList();
    }

    /**
     * A file hit.
     *
     * @since 0.1.0
     */
    public static final class SearchFile {
        private String id;
        private String name;
        private long sizeBytes;
        private String mimeType;
        private String extension;
        private String region;
        private String folderId;
        private String uploadedBy;
        private String uploaderName;
        private LockMode lockMode;
        private long createdAt;

        private SearchFile() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        public long getSizeBytes() { return sizeBytes; }
        public @NotNull String getMimeType() { return mimeType; }
        public @Nullable String getExtension() { return extension; }
        /** @since 0.3.0 */
        public @NotNull String getRegion() { return region; }
        /** Null at the workspace root. @since 0.3.0 */
        public @Nullable String getFolderId() { return folderId; }
        /** @since 0.3.0 */
        public @NotNull String getUploadedBy() { return uploadedBy; }
        /** @since 0.3.0 */
        public @Nullable String getUploaderName() { return uploaderName; }
        /** @since 0.3.0 */
        public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
        public long getCreatedAt() { return createdAt; }
    }

    /**
     * A folder hit.
     *
     * @since 0.1.0
     */
    public static final class SearchFolder {
        private String id;
        private String name;
        private String parentId;
        private int fileCount;
        private long createdAt;

        private SearchFolder() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        /** @since 0.3.0 */
        public @Nullable String getParentId() { return parentId; }
        /** @since 0.3.0 */
        public int getFileCount() { return fileCount; }
        public long getCreatedAt() { return createdAt; }
    }

    /**
     * A share link hit (a link the caller may see).
     *
     * @since 0.1.0
     */
    public static final class SearchShared {
        private String linkId;
        private String token;
        private String status;
        private String fileId;
        private String fileName;
        private String folderId;
        private String folderName;
        private Boolean isBundle;
        private Long sizeBytes;
        private String extension;
        private String region;
        private int viewCount;
        private int downloadCount;
        private boolean isRevoked;
        private Long expiresAt;
        private long sharedAt;
        private String createdBy;
        private String sharerName;

        private SearchShared() {}

        /** The share link id. @since 0.3.0 (0.2.x read a non-existent {@code id} field) */
        public @NotNull String getLinkId() { return linkId; }
        public @NotNull String getToken() { return token; }
        /** {@code active}, {@code expiring} (within 3 days), {@code expired} or {@code revoked}. @since 0.3.0 */
        public @NotNull String getStatus() { return status; }
        /** @since 0.3.0 */
        public @Nullable String getFileId() { return fileId; }
        /** Null for a folder link. */
        public @Nullable String getFileName() { return fileName; }
        /** @since 0.3.0 */
        public @Nullable String getFolderId() { return folderId; }
        /** @since 0.3.0 */
        public @Nullable String getFolderName() { return folderName; }
        /** @since 0.3.0 */
        public boolean isBundle() { return isBundle != null && isBundle; }
        /** @since 0.3.0 */
        public @Nullable Long getSizeBytes() { return sizeBytes; }
        /** @since 0.3.0 */
        public @Nullable String getExtension() { return extension; }
        /** @since 0.3.0 */
        public @Nullable String getRegion() { return region; }
        /** @since 0.3.0 */
        public int getViewCount() { return viewCount; }
        /** @since 0.3.0 */
        public int getDownloadCount() { return downloadCount; }
        /** @since 0.3.0 */
        public boolean isRevoked() { return isRevoked; }
        /** Unix seconds, or null for no expiry. @since 0.3.0 */
        public @Nullable Long getExpiresAt() { return expiresAt; }
        /** When the link was created, unix seconds. @since 0.3.0 */
        public long getSharedAt() { return sharedAt; }
        /** @since 0.3.0 */
        public @NotNull String getCreatedBy() { return createdBy; }
        /** @since 0.3.0 */
        public @Nullable String getSharerName() { return sharerName; }
    }

    /**
     * A file request hit. The upload token is never returned by search.
     *
     * @since 0.1.0
     */
    public static final class SearchFileRequest {
        private String id;
        private String title;
        private String message;
        private Long expiresAt;
        private int uploadCount;
        private boolean isRevoked;
        private long createdAt;
        private String createdByName;

        private SearchFileRequest() {}

        public @NotNull String getId() { return id; }
        /** Null when the request has no title (a match on its message). */
        public @Nullable String getTitle() { return title; }
        /** @since 0.3.0 */
        public @Nullable String getMessage() { return message; }
        /** Unix seconds, or null. @since 0.3.0 */
        public @Nullable Long getExpiresAt() { return expiresAt; }
        /** @since 0.3.0 */
        public int getUploadCount() { return uploadCount; }
        /** @since 0.3.0 */
        public boolean isRevoked() { return isRevoked; }
        public long getCreatedAt() { return createdAt; }
        /** @since 0.3.0 */
        public @Nullable String getCreatedByName() { return createdByName; }
    }
}
