package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.CancelRemoteDownloadResult;
import dev.dosya.sdk.model.CreateRemoteDownloadParams;
import dev.dosya.sdk.model.RemoteDownloadJob;
import dev.dosya.sdk.resource.RemoteDownloadsResource.RemoteDownloadFailedException;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteDownloadsResourceTest extends ApiTestSupport {

    private static String job(String id, String status, long bytesDone, String errorCode, String fileId) {
        return "{\"id\":\"" + id + "\",\"url\":\"https://src.example/a.zip\",\"filename\":\"a.zip\",\"status\":\"" + status + "\","
                + "\"bytes_total\":100,\"bytes_done\":" + bytesDone + ","
                + "\"error_code\":" + (errorCode == null ? "null" : "\"" + errorCode + "\"") + ","
                + "\"file_id\":" + (fileId == null ? "null" : "\"" + fileId + "\"") + ",\"created_at\":1700000000}";
    }

    private static MockResponse jobs(String... rows) {
        return ok("\"jobs\":[" + String.join(",", rows) + "]");
    }

    private DosyaHttpClient retrying() {
        return new DosyaHttpClient(options().maxRetries(3));
    }

    @Test
    void listSendsWorkspaceIdAndParsesJobs() throws Exception {
        enqueue(jobs(job("rdl_1", "done", 100, null, "file_1")));
        List<RemoteDownloadJob> list = new RemoteDownloadsResource(http()).list("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/remote-downloads");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        RemoteDownloadJob j = list.get(0);
        assertThat(j.getId()).isEqualTo("rdl_1");
        assertThat(j.getUrl()).isEqualTo("https://src.example/a.zip");
        assertThat(j.getFilename()).isEqualTo("a.zip");
        assertThat(j.getStatus()).isEqualTo(RemoteDownloadJob.Status.DONE);
        assertThat(j.getRawStatus()).isEqualTo("done");
        assertThat(j.getBytesTotal()).isEqualTo(100L);
        assertThat(j.getBytesDone()).isEqualTo(100L);
        assertThat(j.getErrorCode()).isNull();
        assertThat(j.getFileId()).isEqualTo("file_1");
        assertThat(j.getCreatedAt()).isEqualTo(1700000000L);
    }

    @Test
    void unknownStatusMapsToUnknown() {
        enqueue(jobs(job("rdl_1", "paused", 0, null, null)));
        RemoteDownloadJob j = new RemoteDownloadsResource(http()).list("ws_1").get(0);
        assertThat(j.getStatus()).isEqualTo(RemoteDownloadJob.Status.UNKNOWN);
        assertThat(j.getRawStatus()).isEqualTo("paused");
    }

    @Test
    void createPostsUrlWorkspaceAndFolder() throws Exception {
        enqueue(ok("\"job\":" + job("rdl_1", "queued", 0, null, null)).setResponseCode(201));
        RemoteDownloadJob j = new RemoteDownloadsResource(http()).create(
                new CreateRemoteDownloadParams("ws_1", "https://src.example/a.zip").folderId("fld_1"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/remote-downloads");
        JsonObject body = jsonBody(r);
        assertThat(body.get("url").getAsString()).isEqualTo("https://src.example/a.zip");
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("folder_id").getAsString()).isEqualTo("fld_1");
        assertThat(body.size()).isEqualTo(3);
        assertThat(j.getStatus()).isEqualTo(RemoteDownloadJob.Status.QUEUED);
        assertThat(j.getBytesTotal()).isEqualTo(100L);
    }

    @Test
    void createExposesProbeRefusalCodeAndOmitsFolder() throws Exception {
        enqueue(new MockResponse().setResponseCode(400).setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":false,\"error\":\"This address isn't allowed\",\"code\":\"ssrf_blocked\"}"));
        assertThatThrownBy(() -> new RemoteDownloadsResource(http()).create(
                new CreateRemoteDownloadParams("ws_1", "http://127.0.0.1/")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(e -> {
                    assertThat(((DosyaApiException) e).getStatus()).isEqualTo(400);
                    assertThat(((DosyaApiException) e).getCode()).isEqualTo("ssrf_blocked");
                });
        JsonObject body = jsonBody(take());
        assertThat(body.has("folder_id")).isFalse();
        assertThat(body.size()).isEqualTo(2);
    }

    @Test
    void createDoesNotRetryDailyCap429WithoutRetryAfter() {
        enqueue(fail(429, "Daily limit of 20 remote downloads reached"), ok("\"job\":" + job("rdl_1", "queued", 0, null, null)));
        assertThatThrownBy(() -> new RemoteDownloadsResource(retrying()).create(
                new CreateRemoteDownloadParams("ws_1", "https://src.example/a.zip")))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void cancelDeletesWithWorkspaceIdEncodesIdAndIsNeverRetried() throws Exception {
        enqueue(fail(500, "boom"), ok("\"cancelled\":false,\"dismissed\":true"));
        assertThatThrownBy(() -> new RemoteDownloadsResource(retrying()).cancel("rdl/1", "ws_1"))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/remote-downloads/rdl%2F1");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
    }

    @Test
    void cancelReturnsBothFlags() {
        enqueue(ok("\"cancelled\":true,\"dismissed\":false"));
        CancelRemoteDownloadResult res = new RemoteDownloadsResource(http()).cancel("rdl_1", "ws_1");
        assertThat(res.isCancelled()).isTrue();
        assertThat(res.isDismissed()).isFalse();
    }

    @Test
    void waitForPollsUntilDoneAndReportsProgress() throws Exception {
        enqueue(jobs(job("rdl_other", "queued", 0, null, null), job("rdl_1", "downloading", 40, null, null)),
                jobs(job("rdl_1", "finalizing", 100, null, null)),
                jobs(job("rdl_1", "done", 100, null, "file_9")));
        List<String> seen = new ArrayList<>();

        RemoteDownloadJob done = new RemoteDownloadsResource(http())
                .waitFor("rdl_1", "ws_1", 1, null, j -> seen.add(j.getRawStatus()));

        assertThat(done.getFileId()).isEqualTo("file_9");
        assertThat(seen).containsExactly("downloading", "finalizing", "done");
        assertThat(server.getRequestCount()).isEqualTo(3);
        assertThat(take().getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
    }

    @Test
    void waitForThrowsWithJobOnError() {
        enqueue(jobs(job("rdl_1", "error", 0, "http_404", null)));
        assertThatThrownBy(() -> new RemoteDownloadsResource(http()).waitFor("rdl_1", "ws_1", 1, null, null))
                .isInstanceOf(RemoteDownloadFailedException.class)
                .isInstanceOf(DosyaException.class)
                .hasMessageContaining("http_404")
                .satisfies(e -> assertThat(((RemoteDownloadFailedException) e).getJob().getErrorCode()).isEqualTo("http_404"));
    }

    @Test
    void waitForThrowsOnCancelled() {
        enqueue(jobs(job("rdl_1", "cancelled", 0, null, null)));
        assertThatThrownBy(() -> new RemoteDownloadsResource(http()).waitFor("rdl_1", "ws_1", 1, null, null))
                .isInstanceOf(RemoteDownloadFailedException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void waitForThrowsWhenJobIsNotListed() {
        enqueue(jobs());
        assertThatThrownBy(() -> new RemoteDownloadsResource(http()).waitFor("rdl_1", "ws_1", 1, null, null))
                .isInstanceOf(RemoteDownloadFailedException.class)
                .hasMessageContaining("not found")
                .satisfies(e -> assertThat(((RemoteDownloadFailedException) e).getJob()).isNull());
    }

    @Test
    void waitForTimesOutWithLastJobState() {
        enqueue(jobs(job("rdl_1", "downloading", 10, null, null)));
        assertThatThrownBy(() -> new RemoteDownloadsResource(http()).waitFor("rdl_1", "ws_1", 10_000, 50L, null))
                .isInstanceOf(RemoteDownloadFailedException.class)
                .hasMessageContaining("did not finish within 50ms (last status: downloading)")
                .satisfies(e -> assertThat(((RemoteDownloadFailedException) e).getJob().getBytesDone()).isEqualTo(10L));
        assertThat(server.getRequestCount()).isEqualTo(1);
    }
}
