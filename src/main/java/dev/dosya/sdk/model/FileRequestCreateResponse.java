package dev.dosya.sdk.model;

public class FileRequestCreateResponse {

    private RequestSummary request;

    private FileRequestCreateResponse() {}

    public RequestSummary getRequest() { return request; }

    public static class RequestSummary {
        private String id;
        private String token;
        private String url;
        private String title;
        private Long expiresAt;

        private RequestSummary() {}

        public String getId() { return id; }
        public String getToken() { return token; }
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public Long getExpiresAt() { return expiresAt; }
    }
}
