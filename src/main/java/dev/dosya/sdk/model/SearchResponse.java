package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response returned from a search query containing matched files, folders, shares, and file requests.
 *
 * @since 0.1.0
 */
public final class SearchResponse {

    private String query;
    private List<SearchFile> files;
    private List<SearchFolder> folders;
    private List<SearchShared> shared;
    private List<SearchFileRequest> fileRequests;
    private Pagination pagination;

    private SearchResponse() {}

    public @NotNull String getQuery() { return query; }
    public @NotNull List<SearchFile> getFiles() { return files != null ? Collections.unmodifiableList(files) : Collections.<SearchFile>emptyList(); }
    public @NotNull List<SearchFolder> getFolders() { return folders != null ? Collections.unmodifiableList(folders) : Collections.<SearchFolder>emptyList(); }
    public @NotNull List<SearchShared> getShared() { return shared != null ? Collections.unmodifiableList(shared) : Collections.<SearchShared>emptyList(); }
    public @NotNull List<SearchFileRequest> getFileRequests() { return fileRequests != null ? Collections.unmodifiableList(fileRequests) : Collections.<SearchFileRequest>emptyList(); }
    public @Nullable Pagination getPagination() { return pagination; }

    /**
     * A file matching the search query.
     *
     * @since 0.1.0
     */
    public static final class SearchFile {
        private String id;
        private String name;
        private long sizeBytes;
        private String mimeType;
        private String extension;
        private long createdAt;

        private SearchFile() {}

        public String getId() { return id; }
        public String getName() { return name; }
        public long getSizeBytes() { return sizeBytes; }
        public String getMimeType() { return mimeType; }
        public String getExtension() { return extension; }
        public long getCreatedAt() { return createdAt; }
    }

    /**
     * A folder matching the search query.
     *
     * @since 0.1.0
     */
    public static final class SearchFolder {
        private String id;
        private String name;
        private long createdAt;

        private SearchFolder() {}

        public String getId() { return id; }
        public String getName() { return name; }
        public long getCreatedAt() { return createdAt; }
    }

    /**
     * A shared link matching the search query.
     *
     * @since 0.1.0
     */
    public static final class SearchShared {
        private String id;
        private String fileName;
        private String token;
        private long createdAt;

        private SearchShared() {}

        public String getId() { return id; }
        public String getFileName() { return fileName; }
        public String getToken() { return token; }
        public long getCreatedAt() { return createdAt; }
    }

    /**
     * A file request matching the search query.
     *
     * @since 0.1.0
     */
    public static final class SearchFileRequest {
        private String id;
        private String title;
        private String token;
        private long createdAt;

        private SearchFileRequest() {}

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getToken() { return token; }
        public long getCreatedAt() { return createdAt; }
    }
}
