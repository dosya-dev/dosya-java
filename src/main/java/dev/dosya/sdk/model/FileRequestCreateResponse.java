package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Response returned after creating a new file request.
 *
 * @since 0.1.0
 */
public final class FileRequestCreateResponse {

    private RequestSummary request;

    private FileRequestCreateResponse() {}

    public @NotNull RequestSummary getRequest() { return request; }

    /**
     * Summary of the newly created file request.
     *
     * @since 0.1.0
     */
    public static final class RequestSummary {
        private String id;
        private String token;
        private String url;
        private String title;
        private Long expiresAt;

        private RequestSummary() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getToken() { return token; }
        public @NotNull String getUrl() { return url; }
        public @Nullable String getTitle() { return title; }
        public @Nullable Long getExpiresAt() { return expiresAt; }
    }
}
