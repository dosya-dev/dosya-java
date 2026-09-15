package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateFileRequestParams;
import dev.dosya.sdk.model.FileRequestCreateResponse;
import dev.dosya.sdk.model.FileRequestListItem;
import dev.dosya.sdk.model.FileRequestRecipientsResponse;
import dev.dosya.sdk.model.FileRequestWithActivity;
import dev.dosya.sdk.model.UpdateFileRequestParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Upload requests: public pages where anyone with the link can upload into a workspace folder.
 *
 * <p>Key scope: GET needs {@code read} or {@code full}, the rest {@code full}; {@code upload}
 * keys cannot call these. Workspace-pinned keys can only call {@link #list(String)} (for their
 * own workspace); every other method is refused for them (403). dosya.dev recipient addresses
 * are refused (400) unless the caller is a dosya.dev account.
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
     * Every request in a workspace (a confined member sees only their folder's).
     *
     * @since 0.3.0
     */
    public @NotNull List<FileRequestListItem> list(@NotNull String workspaceId) {
        JsonObject resp = http.request(HttpRequest.get("/api/file-requests").query("workspace_id", workspaceId));
        List<FileRequestListItem> requests = http.fromJson(resp.get("requests"),
                new TypeToken<List<FileRequestListItem>>() {}.getType());
        return requests != null ? Collections.unmodifiableList(requests) : Collections.emptyList();
    }

    /**
     * Creates a request and emails it to the given addresses. 404 when the folder is missing
     * or hidden, 403 {@code folder_locked} when it is fully locked.
     */
    public @NotNull FileRequestCreateResponse create(@NotNull CreateFileRequestParams params) {
        return http.requestAs(HttpRequest.post("/api/file-requests/create").body(params.toBody()),
                FileRequestCreateResponse.class);
    }

    /**
     * One request with its uploads and recipients. 404 when unknown.
     *
     * @since 0.3.0 (returned {@code FileRequestDetail} from a route that does not exist)
     */
    public @NotNull FileRequestWithActivity get(@NotNull String requestId) {
        return http.requestAs(HttpRequest.get("/api/file-requests/" + seg(requestId) + "/uploads"),
                FileRequestWithActivity.class);
    }

    /**
     * Edits a request; only the fields set change. 400 "Nothing to update" for an empty
     * patch, 403 {@code folder_locked} when moving into a locked folder.
     *
     * @since 0.3.0
     */
    public void update(@NotNull String requestId, @NotNull UpdateFileRequestParams params) {
        http.request(HttpRequest.patch("/api/file-requests/" + seg(requestId)).body(params.toBody()));
    }

    /**
     * Updates a request's title and/or message.
     *
     * @param title   the new title, or null to leave unchanged
     * @param message the new message, or null to leave unchanged
     * @deprecated use {@link #update(String, UpdateFileRequestParams)}, which can also clear fields
     */
    @Deprecated
    public void update(@NotNull String requestId, @Nullable String title, @Nullable String message) {
        if (title == null && message == null) return; // nothing to change; the API would answer 400
        UpdateFileRequestParams params = new UpdateFileRequestParams();
        if (title != null) params.title(title);
        if (message != null) params.message(message);
        update(requestId, params);
    }

    /**
     * Revokes a request (its page stops accepting uploads) and notifies recipients.
     * Never retried: a repeated DELETE notifies every recipient again.
     */
    public void delete(@NotNull String requestId) {
        http.request(HttpRequest.delete("/api/file-requests/" + seg(requestId)).retry(HttpRequest.Retry.NEVER));
    }

    /**
     * Files uploaded through a request, with the request and its recipients. Same call as
     * {@link #get(String)}.
     *
     * @since 0.3.0 (returned a list of uploads before)
     */
    public @NotNull FileRequestWithActivity listUploads(@NotNull String requestId) {
        return get(requestId);
    }

    /**
     * Invited addresses, oldest first, with their personal upload tokens.
     *
     * @since 0.3.0 (returned a list of recipients before)
     */
    public @NotNull FileRequestRecipientsResponse listRecipients(@NotNull String requestId) {
        return http.requestAs(HttpRequest.get("/api/file-requests/" + seg(requestId) + "/recipients"),
                FileRequestRecipientsResponse.class);
    }

    /**
     * Invites one more address, emails it and returns the recipient id. 409 when already a
     * recipient, 410 when the request is revoked.
     *
     * @since 0.3.0
     */
    public @NotNull String addRecipient(@NotNull String requestId, @NotNull String email) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", Objects.requireNonNull(email, "email"));
        JsonObject resp = http.request(HttpRequest.post("/api/file-requests/" + seg(requestId) + "/recipients").body(body));
        return resp.get("id").getAsString();
    }

    /**
     * Removes a recipient. Succeeds when already gone.
     *
     * @since 0.3.0
     */
    public void removeRecipient(@NotNull String requestId, @NotNull String recipientId) {
        seg(recipientId); // validates: an empty id would otherwise be dropped from the query
        http.request(HttpRequest.delete("/api/file-requests/" + seg(requestId) + "/recipients")
                .query("recipient_id", recipientId));
    }

    /**
     * Emails the request to one recipient again. 404 when the request is revoked or the
     * recipient unknown; 500 when the email could not be sent.
     *
     * @since 0.3.0 (replaces {@code resend(requestId)} and {@code resend(requestId, List)}, which the API refuses)
     */
    public void resend(@NotNull String requestId, @NotNull String recipientId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("recipient_id", Objects.requireNonNull(recipientId, "recipientId"));
        http.request(HttpRequest.post("/api/file-requests/" + seg(requestId) + "/resend").body(body));
    }
}
