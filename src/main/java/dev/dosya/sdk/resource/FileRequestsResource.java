package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateFileRequestParams;
import dev.dosya.sdk.model.FileRequestCreateResponse;
import dev.dosya.sdk.model.FileRequestDetail;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class FileRequestsResource {

    private final DosyaHttpClient http;

    public FileRequestsResource(DosyaHttpClient http) {
        this.http = http;
    }

    public FileRequestCreateResponse create(CreateFileRequestParams params) {
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

    public FileRequestDetail get(String requestId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests/" + encode(requestId)));
        return http.fromJson(resp.get("request"), FileRequestDetail.class);
    }

    public void update(String requestId, String title, String message) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (title != null) body.put("title", title);
        if (message != null) body.put("message", message);
        http.request(HttpRequest.put("/api/file-requests/" + encode(requestId)).body(body));
    }

    public void delete(String requestId) {
        http.request(HttpRequest.delete("/api/file-requests/" + encode(requestId)));
    }

    public List<FileRequestUpload> listUploads(String requestId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests/" + encode(requestId) + "/uploads"));
        Type listType = new TypeToken<List<FileRequestUpload>>() {}.getType();
        return http.fromJson(resp.get("uploads"), listType);
    }

    public List<FileRequestRecipient> listRecipients(String requestId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests/" + encode(requestId) + "/recipients"));
        Type listType = new TypeToken<List<FileRequestRecipient>>() {}.getType();
        return http.fromJson(resp.get("recipients"), listType);
    }

    public void resend(String requestId, List<String> recipientIds) {
        Map<String, Object> body = new HashMap<String, Object>();
        if (recipientIds != null) body.put("recipient_ids", recipientIds);
        http.request(HttpRequest.post("/api/file-requests/" + encode(requestId) + "/resend").body(body));
    }

    public void resend(String requestId) {
        resend(requestId, null);
    }

    public static class FileRequestUpload {
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

    public static class FileRequestRecipient {
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
