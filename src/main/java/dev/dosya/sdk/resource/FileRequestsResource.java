package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateFileRequestParams;
import dev.dosya.sdk.model.FileRequestCreateResponse;
import dev.dosya.sdk.model.FileRequestDetail;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for managing file requests in Dosya workspaces.
 *
 * <p>File requests allow users to collect file uploads from external recipients
 * via a shareable link. This resource handles creating, retrieving, updating,
 * deleting file requests, and managing their uploads and recipients.</p>
 *
 * @since 0.1.0
 */
public final class FileRequestsResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code FileRequestsResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public FileRequestsResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Creates a new file request.
     *
     * @param params the creation parameters including workspace ID and optional settings
     * @return the response containing the created file request summary
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileRequestCreateResponse create(@NotNull CreateFileRequestParams params) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("workspace_id", params.getWorkspaceId());
        if (params.getFolderId() != null) body.put("folder_id", params.getFolderId());
        if (params.getTitle() != null) body.put("title", params.getTitle());
        if (params.getMessage() != null) body.put("message", params.getMessage());
        if (params.getPassword() != null) body.put("password", params.getPassword());
        if (params.getExpiresInDays() != null) body.put("expires_in_days", params.getExpiresInDays());
        if (params.getAllowedExtensions() != null) body.put("allowed_extensions", params.getAllowedExtensions());
        if (params.getMaxFileSizeMb() != null) body.put("max_file_size_mb", params.getMaxFileSizeMb());
        if (params.getMaxFiles() != null) body.put("max_files", params.getMaxFiles());
        if (params.getEmails() != null) body.put("emails", params.getEmails());
        return http.requestAs(HttpRequest.post("/api/file-requests/create").body(body), FileRequestCreateResponse.class);
    }

    /**
     * Retrieves detailed information about a file request.
     *
     * @param requestId the unique identifier of the file request
     * @return the file request detail
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull FileRequestDetail get(@NotNull String requestId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests/" + encode(requestId)));
        return http.fromJson(resp.get("request"), FileRequestDetail.class);
    }

    /**
     * Updates a file request's title and/or message.
     *
     * @param requestId the unique identifier of the file request
     * @param title     the new title, or {@code null} to leave unchanged
     * @param message   the new message, or {@code null} to leave unchanged
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void update(@NotNull String requestId, @Nullable String title, @Nullable String message) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (title != null) body.put("title", title);
        if (message != null) body.put("message", message);
        http.request(HttpRequest.put("/api/file-requests/" + encode(requestId)).body(body));
    }

    /**
     * Deletes a file request.
     *
     * @param requestId the unique identifier of the file request to delete
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void delete(@NotNull String requestId) {
        http.request(HttpRequest.delete("/api/file-requests/" + encode(requestId)));
    }

    /**
     * Lists all uploads received through a file request.
     *
     * @param requestId the unique identifier of the file request
     * @return the list of uploads received
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<FileRequestUpload> listUploads(@NotNull String requestId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests/" + encode(requestId) + "/uploads"));
        Type listType = new TypeToken<List<FileRequestUpload>>() {}.getType();
        return http.fromJson(resp.get("uploads"), listType);
    }

    /**
     * Lists all recipients of a file request.
     *
     * @param requestId the unique identifier of the file request
     * @return the list of recipients
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<FileRequestRecipient> listRecipients(@NotNull String requestId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests/" + encode(requestId) + "/recipients"));
        Type listType = new TypeToken<List<FileRequestRecipient>>() {}.getType();
        return http.fromJson(resp.get("recipients"), listType);
    }

    /**
     * Resends the file request notification to specific recipients or all recipients.
     *
     * @param requestId    the unique identifier of the file request
     * @param recipientIds the list of recipient IDs to resend to, or {@code null} to resend to all
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void resend(@NotNull String requestId, @Nullable List<String> recipientIds) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (recipientIds != null) body.put("recipient_ids", recipientIds);
        http.request(HttpRequest.post("/api/file-requests/" + encode(requestId) + "/resend").body(body));
    }

    /**
     * Resends the file request notification to all recipients.
     *
     * @param requestId the unique identifier of the file request
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void resend(@NotNull String requestId) {
        resend(requestId, null);
    }

    /**
     * Represents a file that was uploaded in response to a file request.
     *
     * @since 0.1.0
     */
    public static final class FileRequestUpload {
        private String id;
        private String fileName;
        private long sizeBytes;
        private String mimeType;
        private long uploadedAt;
        private String uploaderEmail;

        private FileRequestUpload() {}

        public String getId() { return id; }
        public String getFileName() { return fileName; }
        public long getSizeBytes() { return sizeBytes; }
        public String getMimeType() { return mimeType; }
        public long getUploadedAt() { return uploadedAt; }
        public String getUploaderEmail() { return uploaderEmail; }
    }

    /**
     * Represents a recipient of a file request invitation.
     *
     * @since 0.1.0
     */
    public static final class FileRequestRecipient {
        private String id;
        private String email;
        private String status;
        private long sentAt;

        private FileRequestRecipient() {}

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public long getSentAt() { return sentAt; }
    }
}
