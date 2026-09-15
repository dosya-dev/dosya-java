package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.*;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileRequestsResourceTest extends ApiTestSupport {

    private FileRequestsResource requests() {
        return new FileRequestsResource(http());
    }

    private static final String DETAIL = "\"request\":{\"id\":\"fr_1\",\"workspace_id\":\"ws_1\",\"folder_id\":null,\"token\":\"t\","
            + "\"title\":\"T\",\"message\":null,\"is_revoked\":0,\"is_password_protected\":1,\"expires_at\":null,"
            + "\"allowed_extensions\":\".pdf\",\"max_file_size_bytes\":1048576,\"max_files\":3,\"upload_count\":1,"
            + "\"created_at\":5,\"created_by_name\":\"Ana\",\"folder_name\":null,\"url\":\"https://dosya.dev/upload-request/t\"},"
            + "\"uploads\":[{\"id\":\"fru_1\",\"file_id\":\"f1\",\"uploader_email\":\"g@x.y\",\"uploader_name\":null,\"created_at\":6,"
            + "\"file_name\":\"a.pdf\",\"size_bytes\":10,\"mime_type\":\"application/pdf\",\"extension\":\".pdf\",\"updated_at\":6,"
            + "\"current_version\":1,\"lock_mode\":\"none\",\"is_hidden\":null,\"uploaded_by\":\"u1\",\"region\":\"eu\",\"origin\":null}],"
            + "\"recipients\":[{\"id\":\"frr_1\",\"email\":\"g@x.y\",\"token\":\"rt\",\"sent_at\":4,\"uploaded_at\":6,\"created_at\":3}],"
            + "\"title\":\"T\"";

    @Test
    void list() throws Exception {
        enqueue(ok("\"requests\":[{\"id\":\"fr_1\",\"token\":\"t\",\"title\":null,\"message\":null,\"is_password_protected\":0,"
                + "\"expires_at\":null,\"allowed_extensions\":null,\"max_file_size_bytes\":null,\"max_files\":null,\"upload_count\":0,"
                + "\"is_revoked\":1,\"created_at\":1,\"folder_id\":\"fld_1\",\"created_by_name\":null,\"folder_name\":\"Docs\","
                + "\"url\":\"https://dosya.dev/upload-request/t\"}]"));
        List<FileRequestListItem> items = requests().list("ws_1");
        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/file-requests");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(items.get(0).isRevoked()).isTrue();
        assertThat(items.get(0).getFolderName()).isEqualTo("Docs");
        assertThat(items.get(0).getMaxFiles()).isNull();
    }

    @Test
    void createSendsSetFields() throws Exception {
        enqueue(ok("\"request\":{\"id\":\"fr_2\",\"token\":\"t2\",\"url\":\"u\",\"title\":\"T\",\"expires_at\":null}"));
        FileRequestCreateResponse res = requests().create(new CreateFileRequestParams("ws_1")
                .folderId("fld_1").title("T").expiresInDays(7).maxFileSizeMb(5).emails(Arrays.asList("g@x.y")));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/create");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("workspace_id", "folder_id", "title", "expires_in_days",
                "max_file_size_mb", "emails");
        assertThat(body.getAsJsonArray("emails").get(0).getAsString()).isEqualTo("g@x.y");
        assertThat(res.getRequest().getId()).isEqualTo("fr_2");
    }

    @Test
    void getReadsTheUploadsRoute() throws Exception {
        enqueue(ok(DETAIL));
        FileRequestWithActivity res = requests().get("fr/1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/fr%2F1/uploads");
        assertThat(res.getRequest().getWorkspaceId()).isEqualTo("ws_1");
        assertThat(res.getRequest().isPasswordProtected()).isTrue();
        assertThat(res.getRequest().getMaxFileSizeBytes()).isEqualTo(1048576L);
        assertThat(res.getUploads().get(0).getFileName()).isEqualTo("a.pdf");
        assertThat(res.getUploads().get(0).getIsHidden()).isNull();
        assertThat(res.getUploads().get(0).getLockMode()).isEqualTo(LockMode.NONE);
        assertThat(res.getRecipients().get(0).getUploadedAt()).isEqualTo(6L);
        assertThat(res.getTitle()).isEqualTo("T");
    }

    @Test
    void listUploadsIsTheSameCall() throws Exception {
        enqueue(ok(DETAIL));
        assertThat(requests().listUploads("fr_1").getUploads()).hasSize(1);
        assertThat(pathOf(take())).isEqualTo("/api/file-requests/fr_1/uploads");
    }

    @Test
    void updateIsPatchWithFullFieldSet() throws Exception {
        enqueue(ok());
        requests().update("fr_1", new UpdateFileRequestParams()
                .title(null).message("m").expiresInDays(null).clearPassword().folderId(null)
                .allowedExtensions(".pdf").maxFileSizeMb(0).maxFiles(4));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PATCH");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/fr_1");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("title", "message", "expires_in_days", "password",
                "folder_id", "allowed_extensions", "max_file_size_mb", "max_files");
        assertThat(body.get("title").isJsonNull()).isTrue();
        assertThat(body.get("folder_id").isJsonNull()).isTrue();
        assertThat(body.get("password").getAsString()).isEmpty();
        assertThat(body.get("max_files").getAsInt()).isEqualTo(4);
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedUpdateAlsoUsesPatch() throws Exception {
        enqueue(ok());
        requests().update("fr_1", "New title", null);
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PATCH");
        assertThat(jsonBody(r).keySet()).containsExactly("title");
    }

    @Test
    void deleteIsNeverRetried() throws Exception {
        enqueue(fail(503, "down").setHeader("Retry-After", "0"), ok());
        FileRequestsResource retrying = new FileRequestsResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.delete("fr_1")).isInstanceOf(DosyaApiException.class);
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/fr_1");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void listRecipients() throws Exception {
        enqueue(ok("\"recipients\":[{\"id\":\"frr_1\",\"email\":\"g@x.y\",\"token\":\"rt\",\"sent_at\":null,"
                + "\"uploaded_at\":null,\"created_at\":3}],\"title\":null,\"request_token\":\"t\""));
        FileRequestRecipientsResponse res = requests().listRecipients("fr_1");
        assertThat(pathOf(take())).isEqualTo("/api/file-requests/fr_1/recipients");
        assertThat(res.getRecipients().get(0).getSentAt()).isNull();
        assertThat(res.getRequestToken()).isEqualTo("t");
    }

    @Test
    void addRecipient() throws Exception {
        enqueue(ok("\"id\":\"frr_2\"").setResponseCode(201));
        String id = requests().addRecipient("fr_1", "h@x.y");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/fr_1/recipients");
        assertThat(jsonBody(r).get("email").getAsString()).isEqualTo("h@x.y");
        assertThat(id).isEqualTo("frr_2");
    }

    @Test
    void removeRecipientSendsRecipientIdQuery() throws Exception {
        enqueue(ok());
        requests().removeRecipient("fr_1", "frr_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/fr_1/recipients");
        assertThat(r.getRequestUrl().queryParameter("recipient_id")).isEqualTo("frr_1");
    }

    @Test
    void removeRecipientRejectsEmptyId() {
        assertThatThrownBy(() -> requests().removeRecipient("fr_1", "")).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void resendSendsSingleRecipientId() throws Exception {
        enqueue(ok());
        requests().resend("fr_1", "frr_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/file-requests/fr_1/resend");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactly("recipient_id");
        assertThat(body.get("recipient_id").getAsString()).isEqualTo("frr_1");
    }
}
