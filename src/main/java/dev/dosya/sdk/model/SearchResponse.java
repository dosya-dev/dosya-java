package dev.dosya.sdk.model;

import java.util.List;

public class SearchResponse {

    private String query;
    private List<SearchFile> files;
    private List<SearchFolder> folders;
    private List<SearchShared> shared;
    private List<SearchFileRequest> fileRequests;
    private Pagination pagination;

    private SearchResponse() {}

    public String getQuery() { return query; }
    public List<SearchFile> getFiles() { return files; }
    public List<SearchFolder> getFolders() { return folders; }
    public List<SearchShared> getShared() { return shared; }
    public List<SearchFileRequest> getFileRequests() { return fileRequests; }
    public Pagination getPagination() { return pagination; }

    public static class SearchFile {
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

    public static class SearchFolder {
        private String id;
        private String name;
        private long createdAt;

        private SearchFolder() {}

        public String getId() { return id; }
        public String getName() { return name; }
        public long getCreatedAt() { return createdAt; }
    }

    public static class SearchShared {
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

    public static class SearchFileRequest {
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
