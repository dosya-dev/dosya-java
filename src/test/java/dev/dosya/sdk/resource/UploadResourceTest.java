package dev.dosya.sdk.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaUploadException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.UploadBatchFile;
import dev.dosya.sdk.model.UploadBatchResult;
import dev.dosya.sdk.model.UploadManyOptions;
import dev.dosya.sdk.model.UploadManyResult;
import dev.dosya.sdk.model.UploadParams;
import dev.dosya.sdk.model.UploadPartResult;
import dev.dosya.sdk.model.UploadProgress;
import dev.dosya.sdk.model.UploadResult;
import dev.dosya.sdk.model.UploadResumeOptions;
import dev.dosya.sdk.model.UploadSource;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class UploadResourceTest extends ApiTestSupport {

    private static final int MIB = 1024 * 1024;

    @TempDir
    Path tmp;

    private Router router;

    @BeforeEach
    void installRouter() {
        router = new Router();
        server.setDispatcher(router);
    }

    // ── Fixtures ──

    private UploadResource upload() {
        return new UploadResource(http());
    }

    /** A client that retries twice, with tiny delays. */
    private UploadResource retrying() {
        return new UploadResource(new DosyaHttpClient(options().maxRetries(2)));
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body);
    }

    private static String fileRow(String extra) {
        return "{\"id\":\"file_1\",\"name\":\"a.txt\",\"r2_key\":\"ws_1/file_1/a.txt\",\"size_bytes\":5,"
                + "\"mime_type\":\"text/plain\",\"extension\":\".txt\",\"region\":\"apac\",\"version\":1,"
                + "\"created_at\":1700000000,\"content_hash\":null,\"hash_verified\":false,\"etag\":\"etag-1\""
                + (extra.isEmpty() ? "" : "," + extra) + "}";
    }

    private static MockResponse fileOk(String extra) {
        return json(201, "{\"ok\":true,\"file\":" + fileRow(extra) + "}");
    }

    private static MockResponse singleInit(String sessionId, long size) {
        return json(201, "{\"ok\":true,\"session_id\":\"" + sessionId + "\",\"upload_url\":\"/api/upload/" + sessionId
                + "\",\"workspace_id\":\"ws_1\",\"file_name\":\"a.txt\",\"file_size\":" + size
                + ",\"mime_type\":\"text/plain\",\"extension\":\".txt\",\"region\":\"apac\",\"resumable\":null}");
    }

    private static MockResponse multipartInit(String sessionId, long size, int partSize) {
        long totalParts = (size + partSize - 1) / partSize;
        return json(201, "{\"ok\":true,\"session_id\":\"" + sessionId + "\",\"upload_url\":\"/api/upload/" + sessionId
                + "\",\"workspace_id\":\"ws_1\",\"file_name\":\"big.bin\",\"file_size\":" + size
                + ",\"mime_type\":\"application/octet-stream\",\"extension\":\".bin\",\"region\":\"apac\","
                + "\"resumable\":{\"part_size\":" + partSize + ",\"total_parts\":" + totalParts
                + ",\"part_upload_url\":\"/api/upload/" + sessionId + "/part\",\"complete_url\":\"/api/upload/" + sessionId
                + "/complete\",\"status_url\":\"/api/upload/" + sessionId + "/status\"}}");
    }

    private static MockResponse statusReply(String sessionId, String status, long size, Integer partSize,
                                            Integer totalParts, String uploadedParts, boolean hasMultipart, long bytesUploaded) {
        return json(200, "{\"ok\":true,\"session_id\":\"" + sessionId + "\",\"status\":\"" + status + "\",\"size_bytes\":"
                + size + ",\"part_size\":" + partSize + ",\"total_parts\":" + totalParts + ",\"bytes_uploaded\":"
                + bytesUploaded + ",\"uploaded_parts\":" + uploadedParts + ",\"has_multipart\":" + hasMultipart + "}");
    }

    private static MockResponse singleStatus(String sessionId, String status) {
        return statusReply(sessionId, status, 5, null, null, "[]", false, 0);
    }

    private static MockResponse partOk(int n) {
        return json(201, "{\"ok\":true,\"part_number\":" + n + ",\"etag\":\"e" + n + "\"}");
    }

    private void partRoutes(String sessionId, int count, Function<Integer, MockResponse> handler) {
        for (int n = 1; n <= count; n++) {
            int part = n;
            router.handle("PUT /api/upload/" + sessionId + "/part/" + n, r -> handler.apply(part));
        }
    }

    private static byte[] bytes(int n) {
        byte[] out = new byte[n];
        for (int i = 0; i < n; i++) out[i] = (byte) (i % 251);
        return out;
    }

    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── Single request ──

    @Test
    void singleSendsInitAllowlistSourceTimesAndSha256() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5));
        router.route("PUT /api/upload/upl_1", fileOk("\"content_hash\":\"" + "ab".repeat(32) + "\",\"hash_verified\":true"));
        List<UploadProgress> events = Collections.synchronizedList(new ArrayList<>());
        String sha = "AB".repeat(32);

        @SuppressWarnings("deprecation")
        UploadParams params = UploadParams.fromBytes("ws_1", "a.txt", bytes(5))
                .mimeType("text/plain")
                .region("eu")
                .folderId("fld_1")
                .expectedVersion(3)
                .sha256(sha)
                .sourceModifiedAt(1_700_000_123L)
                .sourceCreatedAt(Instant.ofEpochMilli(1_600_000_000_500L))
                .onProgress(events::add);
        UploadResult result = upload().file(params);

        JsonObject init = JsonParser.parseString(router.calls.get(0).utf8()).getAsJsonObject();
        assertThat(init.keySet()).containsExactlyInAnyOrder("workspace_id", "file_name", "file_size", "mime_type",
                "folder_id", "expected_version");
        assertThat(init.get("file_size").getAsLong()).isEqualTo(5);
        assertThat(init.get("expected_version").getAsInt()).isEqualTo(3);

        Call put = router.calls.get(1);
        assertThat(put.key).isEqualTo("PUT /api/upload/upl_1");
        assertThat(put.header("X-Dosya-Sha256")).isEqualTo(sha.toLowerCase());
        assertThat(put.header("X-Dosya-Source-Mtime")).isEqualTo("1700000123");
        assertThat(put.header("X-Dosya-Source-Ctime")).isEqualTo("1600000000");
        assertThat(put.header("Content-Type")).isEqualTo("text/plain");
        assertThat(put.body).isEqualTo(bytes(5));

        assertThat(result.getSessionId()).isEqualTo("upl_1");
        assertThat(result.getFile().getId()).isEqualTo("file_1");
        assertThat(result.getFile().getExtension()).isEqualTo(".txt");
        assertThat(result.getFile().getContentHash()).isEqualTo("ab".repeat(32));
        assertThat(result.getFile().isHashVerified()).isTrue();
        assertThat(result.getFile().getEtag()).isEqualTo("etag-1");
        assertThat(events.stream().map(UploadProgress::getStatus)).containsExactly("initializing", "uploading", "complete");
        assertThat(events.get(events.size() - 1).getPercent()).isEqualTo(100);
    }

    @Test
    void computesSha256ForBytesAndFiles() throws IOException {
        router.route("POST /api/upload/init", singleInit("upl_1", 3));
        router.route("PUT /api/upload/upl_1", fileOk(""));
        String abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

        upload().file(UploadParams.fromBytes("ws_1", "a.txt", "abc".getBytes(StandardCharsets.UTF_8)).computeSha256(true));
        assertThat(router.calls.get(1).header("X-Dosya-Sha256")).isEqualTo(abc);

        Path file = tmp.resolve("a.txt");
        Files.write(file, "abc".getBytes(StandardCharsets.UTF_8));
        upload().file(UploadParams.fromPath("ws_1", file).computeSha256(true));
        assertThat(router.calls.get(3).header("X-Dosya-Sha256")).isEqualTo(abc);
        assertThat(router.calls.get(3).body).isEqualTo("abc".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void fromFileReadsTheContentAtUploadTimeNotAtConstruction() throws IOException {
        router.route("POST /api/upload/init", singleInit("upl_1", 4));
        router.route("PUT /api/upload/upl_1", fileOk(""));
        Path file = tmp.resolve("a.txt");
        Files.write(file, "old!".getBytes(StandardCharsets.UTF_8));

        UploadParams params = UploadParams.fromFile("ws_1", file.toFile());
        Files.write(file, "new!".getBytes(StandardCharsets.UTF_8));
        upload().file(params);

        assertThat(new String(router.calls.get(1).body, StandardCharsets.UTF_8)).isEqualTo("new!");
    }

    @Test
    void fileSourcesAreNeverReadWhole() throws IOException {
        // A sparse file larger than any byte array, so any whole-file read would fail.
        Path big = tmp.resolve("big.bin");
        long size = Integer.MAX_VALUE + 16L;
        try (RandomAccessFile raf = new RandomAccessFile(big.toFile(), "rw")) {
            raf.setLength(size);
            raf.seek(size - 3);
            raf.write(new byte[] {7, 8, 9});
        }
        UploadParams params = UploadParams.fromFile("ws_1", big.toFile());
        assertThat(params.getFileSize()).isEqualTo(size);
        assertThat(params.getSource().read(size - 3, 10)).containsExactly(7, 8, 9);
        assertThat(params.getSource().read(0, 4)).containsExactly(0, 0, 0, 0);
    }

    @Test
    void buffersAStreamForTheSinglePutAndRefusesAShortOne() {
        router.route("POST /api/upload/init", singleInit("upl_1", 6));
        router.route("PUT /api/upload/upl_1", fileOk(""));
        upload().file(UploadParams.of("ws_1", "a.txt",
                UploadSource.ofStream(new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6}), 6)));
        assertThat(router.calls.get(1).body).containsExactly(1, 2, 3, 4, 5, 6);

        DosyaUploadException e = catchThrowableOfType(() -> upload().file(UploadParams.of("ws_1", "a.txt",
                UploadSource.ofStream(new ByteArrayInputStream(new byte[] {1, 2}), 6))), DosyaUploadException.class);
        assertThat(e.getMessage()).contains("ended after 2 bytes");
        assertThat(router.keys()).filteredOn(k -> k.startsWith("PUT")).hasSize(1);
    }

    @Test
    void fromStreamWithUnknownSizeKeepsTheLegacyBuffering() throws IOException {
        UploadParams params = UploadParams.fromStream("ws_1", "a.txt", new ByteArrayInputStream(new byte[] {1, 2, 3}), 0);
        assertThat(params.getFileSize()).isEqualTo(3);
        assertThat(params.getSource().isStream()).isFalse();
    }

    @Test
    void reInitsAFreshSessionWhenThePutFailsAndTheSessionFailed() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5), singleInit("upl_2", 5));
        router.route("PUT /api/upload/upl_1", fail(500, "File upload to storage failed"));
        router.route("GET /api/upload/upl_1/status", singleStatus("upl_1", "failed"));
        router.route("PUT /api/upload/upl_2", fileOk(""));

        UploadResult result = retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5)));

        assertThat(router.keys()).containsExactly(
                "POST /api/upload/init",
                "PUT /api/upload/upl_1",
                "GET /api/upload/upl_1/status",
                "POST /api/upload/init",
                "PUT /api/upload/upl_2");
        assertThat(result.getSessionId()).isEqualTo("upl_2");
    }

    @Test
    void waitsForAnAttemptStillProcessingThenReInitsOnceItFailed() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5), singleInit("upl_2", 5));
        router.route("PUT /api/upload/upl_1", fail(502, "Bad gateway"));
        router.route("GET /api/upload/upl_1/status", singleStatus("upl_1", "uploading"), singleStatus("upl_1", "failed"));
        router.route("PUT /api/upload/upl_2", fileOk(""));

        UploadResult result = retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5)));

        assertThat(result.getSessionId()).isEqualTo("upl_2");
        assertThat(router.keys()).filteredOn(k -> k.endsWith("/status")).hasSize(2);
    }

    @Test
    void doesNotReSendWhileTheServerKeepsProcessing() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5));
        router.route("PUT /api/upload/upl_1", fail(504, "Gateway timeout"));
        router.route("GET /api/upload/upl_1/status", singleStatus("upl_1", "uploading"));

        DosyaUploadException e = catchThrowableOfType(
                () -> retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5))), DosyaUploadException.class);

        assertThat(e.getMessage()).contains("still processing");
        assertThat(router.keys()).filteredOn("POST /api/upload/init"::equals).hasSize(1);
        assertThat(router.keys()).filteredOn(k -> k.endsWith("/status")).hasSize(7);
    }

    @Test
    void doesNotReSendWhenTheOutcomeCannotBeRead() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5));
        router.route("PUT /api/upload/upl_1", fail(500, "boom"));
        router.route("GET /api/upload/upl_1/status", fail(500, "boom"));

        DosyaUploadException e = catchThrowableOfType(
                () -> retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5))), DosyaUploadException.class);

        assertThat(e.getMessage()).contains("could not be confirmed");
        assertThat(router.keys()).filteredOn("POST /api/upload/init"::equals).hasSize(1);
    }

    @Test
    void doesNotReSendAfterALostResponseWhenTheSessionIsComplete() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5));
        router.route("PUT /api/upload/upl_1", new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        router.route("GET /api/upload/upl_1/status", singleStatus("upl_1", "complete"));

        DosyaUploadException e = catchThrowableOfType(
                () -> retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5))), DosyaUploadException.class);

        assertThat(e.getSessionId()).isEqualTo("upl_1");
        assertThat(e.getMessage()).contains("id is unknown");
        assertThat(router.keys()).filteredOn("POST /api/upload/init"::equals).hasSize(1);
        assertThat(router.keys()).filteredOn("PUT /api/upload/upl_1"::equals).hasSize(1);
    }

    @Test
    void aRateLimitWithRetryAfterSkipsTheStatusCheck() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5), singleInit("upl_2", 5));
        router.route("PUT /api/upload/upl_1", fail(429, "Too many requests").setHeader("Retry-After", "0"));
        router.route("PUT /api/upload/upl_2", fileOk(""));

        retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5)));

        assertThat(router.keys()).containsExactly("POST /api/upload/init", "PUT /api/upload/upl_1",
                "POST /api/upload/init", "PUT /api/upload/upl_2");
    }

    @Test
    void neverRetriesA4xxAndSurfacesTheMachineCode() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5));
        router.route("PUT /api/upload/upl_1", json(400, "{\"ok\":false,\"error\":\"hash_mismatch\",\"expected\":\"a\",\"received\":\"b\"}"));

        DosyaApiException e = catchThrowableOfType(
                () -> retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5))), DosyaApiException.class);

        assertThat(e.getCode()).isEqualTo("hash_mismatch");
        assertThat(router.calls).hasSize(2);
    }

    @Test
    void surfacesInitRefusalsWithTheirCodes() {
        router.route("POST /api/upload/init",
                json(400, "{\"ok\":false,\"error\":\"You have 3 uploads in progress.\",\"error_code\":\"concurrent_upload_limit\"}"),
                json(409, "{\"ok\":false,\"error\":\"version_conflict\",\"current_version\":7}"));
        UploadResource up = retrying();

        DosyaApiException a = catchThrowableOfType(() -> up.file(UploadParams.fromBytes("ws_1", "a.txt", bytes(1))), DosyaApiException.class);
        assertThat(a.getCode()).isEqualTo("concurrent_upload_limit");
        DosyaApiException b = catchThrowableOfType(() -> up.file(UploadParams.fromBytes("ws_1", "a.txt", bytes(1))), DosyaApiException.class);
        assertThat(b.getCode()).isEqualTo("version_conflict");
        assertThat(b.getDetails().get("current_version").getAsInt()).isEqualTo(7);
        assertThat(router.calls).hasSize(2);
    }

    // ── Multipart ──

    @Test
    void uploadsTheFirstPartAloneBeforeFanningOut() {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        router.route("POST /api/upload/init", multipartInit("upl_m", 10, 4));
        partRoutes("upl_m", 3, n -> {
            events.add("start " + n);
            pause(n == 1 ? 300 : 150);
            events.add("end " + n);
            return partOk(n);
        });
        router.route("POST /api/upload/upl_m/complete", fileOk(""));

        upload().file(UploadParams.fromBytes("ws_1", "big.bin", bytes(10)).concurrency(3));

        assertThat(events.indexOf("end 1")).isLessThan(events.indexOf("start 2"));
        assertThat(events.indexOf("end 1")).isLessThan(events.indexOf("start 3"));
        // After part 1, parts 2 and 3 run concurrently.
        assertThat(events.indexOf("start 3")).isLessThan(events.indexOf("end 2"));
    }

    @Test
    void readsPartRangesFromAFile() throws IOException {
        Path file = tmp.resolve("big.bin");
        Files.write(file, bytes(10));
        router.route("POST /api/upload/init", multipartInit("upl_m", 10, 4));
        partRoutes("upl_m", 3, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", fileOk("\"size_bytes\":10"));

        upload().file(UploadParams.fromPath("ws_1", file));

        Map<String, byte[]> parts = new HashMap<>();
        for (Call c : router.calls) if (c.key.contains("/part/")) parts.put(c.key, c.body);
        assertThat(parts.get("PUT /api/upload/upl_m/part/1")).isEqualTo(Arrays.copyOfRange(bytes(10), 0, 4));
        assertThat(parts.get("PUT /api/upload/upl_m/part/2")).isEqualTo(Arrays.copyOfRange(bytes(10), 4, 8));
        assertThat(parts.get("PUT /api/upload/upl_m/part/3")).isEqualTo(Arrays.copyOfRange(bytes(10), 8, 10));
        assertThat(router.calls.get(1).header("Content-Type")).isEqualTo("application/octet-stream");
    }

    @Test
    void reportsExactBytesIncludingAShortLastPart() {
        List<UploadProgress> events = Collections.synchronizedList(new ArrayList<>());
        router.route("POST /api/upload/init", multipartInit("upl_m", 10, 4));
        partRoutes("upl_m", 3, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", fileOk("\"size_bytes\":10"));

        upload().file(UploadParams.fromBytes("ws_1", "big.bin", bytes(10)).concurrency(1).onProgress(events::add));

        assertThat(events.stream().filter(e -> e.getStatus().equals("uploading")).map(UploadProgress::getBytesUploaded))
                .containsExactly(0L, 4L, 8L, 10L);
        List<String> statuses = events.stream().map(UploadProgress::getStatus).collect(Collectors.toList());
        assertThat(statuses.subList(statuses.size() - 2, statuses.size())).containsExactly("completing", "complete");
    }

    @Test
    void retriesAPartOn500ButNotOn4xx() {
        int[] part2 = {0};
        router.route("POST /api/upload/init", multipartInit("upl_m", 10, 4));
        partRoutes("upl_m", 3, n -> {
            if (n == 2 && part2[0]++ == 0) return fail(500, "Failed to upload part to storage");
            if (n == 3) return fail(413, "Part exceeds the 10 MB part size");
            return partOk(n);
        });

        DosyaUploadException e = catchThrowableOfType(() -> retrying().file(
                UploadParams.fromBytes("ws_1", "big.bin", bytes(10)).concurrency(1)), DosyaUploadException.class);

        assertThat(router.keys().stream().filter(k -> k.contains("/part/")).map(k -> k.substring(k.lastIndexOf('/') + 1)))
                .containsExactly("1", "2", "2", "3");
        assertThat(e.getPartNumber()).isEqualTo(3);
        assertThat(e.getSessionId()).isEqualTo("upl_m");
        assertThat(((DosyaApiException) e.getCause()).getStatus()).isEqualTo(413);
        assertThat(router.keys()).noneMatch(k -> k.endsWith("/complete"));
    }

    @Test
    void stopsStartingPartsAfterTheFirstFailure() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 40, 4));
        partRoutes("upl_m", 10, n -> n == 2 ? fail(400, "Upload session not found") : partOk(n));

        catchThrowableOfType(() -> upload().file(
                UploadParams.fromBytes("ws_1", "big.bin", bytes(40)).concurrency(1)), DosyaUploadException.class);

        assertThat(router.keys().stream().filter(k -> k.contains("/part/"))).hasSize(2);
    }

    @Test
    void sendsSourceTimesOnCompleteAndExplainsA409AfterARetriedComplete() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 8, 4));
        partRoutes("upl_m", 2, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", fail(502, "Bad gateway"), fail(409, "Upload session is already complete"));
        // The first complete ended without finishing, so a second one is safe.
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "failed", 8, 4, 2, "[1,2]", true, 8));

        DosyaUploadException e = catchThrowableOfType(() -> retrying().file(
                UploadParams.fromBytes("ws_1", "big.bin", bytes(8)).sourceModifiedAt(1_700_000_000L)), DosyaUploadException.class);

        List<Call> completes = router.calls.stream().filter(c -> c.key.endsWith("/complete")).collect(Collectors.toList());
        assertThat(completes).hasSize(2);
        assertThat(completes.get(0).header("X-Dosya-Source-Mtime")).isEqualTo("1700000000");
        assertThat(e.getMessage()).contains("id is unknown");
    }

    @Test
    void reportsALostCompleteResponseInsteadOfCompletingTwice() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 8, 4));
        partRoutes("upl_m", 2, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "complete", 8, 4, 2, "[1,2]", true, 8));

        DosyaUploadException e = catchThrowableOfType(() -> retrying().file(
                UploadParams.fromBytes("ws_1", "big.bin", bytes(8))), DosyaUploadException.class);

        assertThat(e.getMessage()).contains("id is unknown");
        assertThat(router.keys()).filteredOn(k -> k.endsWith("/complete")).hasSize(1);
    }

    @Test
    void retriesCompleteWhenTheSessionStillReadsUploadingAfterPolling() {
        // A session with parts reads `uploading` until complete commits, so a failed complete
        // that left no trace must still be repeated once polling has given it time to finish.
        router.route("POST /api/upload/init", multipartInit("upl_m", 8, 4));
        partRoutes("upl_m", 2, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", fail(502, "Bad gateway"), fileOk(""));
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "uploading", 8, 4, 2, "[1,2]", true, 8));

        UploadResult result = retrying().file(UploadParams.fromBytes("ws_1", "big.bin", bytes(8)));

        assertThat(result.getFile().getId()).isEqualTo("file_1");
        assertThat(router.keys()).filteredOn(k -> k.endsWith("/complete")).hasSize(2);
        assertThat(router.keys()).filteredOn(k -> k.endsWith("/status")).hasSize(7);
    }

    @Test
    void doesNotResendAFirstPartWhoseLostResponseWasStored() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 8, 4));
        int[] firstPart = {0};
        router.handle("PUT /api/upload/upl_m/part/1", r -> firstPart[0]++ == 0
                ? new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                : partOk(1));
        router.handle("PUT /api/upload/upl_m/part/2", r -> partOk(2));
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "uploading", 8, 4, 2, "[1]", true, 4));
        router.route("POST /api/upload/upl_m/complete", fileOk(""));

        retrying().file(UploadParams.fromBytes("ws_1", "big.bin", bytes(8)));

        assertThat(router.keys()).filteredOn("PUT /api/upload/upl_m/part/1"::equals).hasSize(1);
        assertThat(router.keys()).filteredOn("PUT /api/upload/upl_m/part/2"::equals).hasSize(1);
    }

    @Test
    void retriesTheFirstPartOnceTheMultipartUploadIsRecorded() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 8, 4));
        int[] firstPart = {0};
        router.handle("PUT /api/upload/upl_m/part/1", r -> firstPart[0]++ == 0 ? fail(502, "Bad gateway") : partOk(1));
        router.handle("PUT /api/upload/upl_m/part/2", r -> partOk(2));
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "uploading", 8, 4, 2, "[]", true, 0));
        router.route("POST /api/upload/upl_m/complete", fileOk(""));

        retrying().file(UploadParams.fromBytes("ws_1", "big.bin", bytes(8)));

        assertThat(router.keys()).filteredOn("PUT /api/upload/upl_m/part/1"::equals).hasSize(2);
        // One status read: has_multipart was already true, so the retry could go at once.
        assertThat(router.keys()).filteredOn(k -> k.endsWith("/status")).hasSize(1);
    }

    @Test
    void reusesASessionThatNeverSawTheFailedPut() {
        router.route("POST /api/upload/init", singleInit("upl_1", 5));
        int[] put = {0};
        router.handle("PUT /api/upload/upl_1", r -> put[0]++ == 0
                ? new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                : fileOk(""));
        router.route("GET /api/upload/upl_1/status", singleStatus("upl_1", "pending"));

        UploadResult result = retrying().file(UploadParams.fromBytes("ws_1", "a.txt", bytes(5)));

        assertThat(result.getSessionId()).isEqualTo("upl_1");
        assertThat(router.keys()).filteredOn("POST /api/upload/init"::equals).hasSize(1);
        assertThat(router.keys()).filteredOn("PUT /api/upload/upl_1"::equals).hasSize(2);
    }

    @Test
    void readsAStreamPartByPart() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 10, 4));
        partRoutes("upl_m", 3, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", fileOk("\"size_bytes\":10"));

        upload().file(UploadParams.of("ws_1", "big.bin",
                UploadSource.ofStream(new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}), 10)));

        List<byte[]> parts = router.calls.stream().filter(c -> c.key.contains("/part/")).map(c -> c.body).collect(Collectors.toList());
        assertThat(parts).hasSize(3);
        assertThat(parts.get(0)).containsExactly(1, 2, 3, 4);
        assertThat(parts.get(1)).containsExactly(5, 6, 7, 8);
        assertThat(parts.get(2)).containsExactly(9, 10);
    }

    @Test
    void refusesAStreamLongerThanDeclared() {
        router.route("POST /api/upload/init", multipartInit("upl_m", 8, 4));
        partRoutes("upl_m", 2, UploadResourceTest::partOk);

        DosyaUploadException e = catchThrowableOfType(() -> upload().file(UploadParams.of("ws_1", "big.bin",
                UploadSource.ofStream(new ByteArrayInputStream(bytes(9)), 8))), DosyaUploadException.class);

        assertThat(e.getMessage()).contains("longer than the declared size");
        assertThat(router.keys()).noneMatch(k -> k.endsWith("/complete"));
    }

    // ── Resume ──

    @Test
    void resumesASessionWithNoPartsYetFirstPartAlone() {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "failed", 10, 4, 3, "[]", false, 0));
        partRoutes("upl_m", 3, n -> {
            events.add("start " + n);
            pause(n == 1 ? 200 : 20);
            events.add("end " + n);
            return partOk(n);
        });
        router.route("POST /api/upload/upl_m/complete", fileOk("\"size_bytes\":10"));

        UploadResult result = upload().resume("upl_m", UploadSource.ofBytes(bytes(10)));

        assertThat(result.getFile().getSizeBytes()).isEqualTo(10);
        assertThat(events.indexOf("end 1")).isLessThan(events.indexOf("start 2"));
        assertThat(events.indexOf("end 1")).isLessThan(events.indexOf("start 3"));
    }

    @Test
    void resumeSkipsStoredPartsAndSeedsProgress() {
        List<UploadProgress> events = Collections.synchronizedList(new ArrayList<>());
        router.route("GET /api/upload/upl_m/status", statusReply("upl_m", "failed", 10, 4, 3, "[1,3]", true, 6));
        partRoutes("upl_m", 3, UploadResourceTest::partOk);
        router.route("POST /api/upload/upl_m/complete", fileOk("\"size_bytes\":10"));

        upload().resume("upl_m", UploadSource.ofBytes(bytes(10)),
                new UploadResumeOptions().onProgress(events::add).sourceCreatedAt(Instant.ofEpochSecond(1_650_000_000)));

        assertThat(router.keys()).filteredOn(k -> k.contains("/part/")).containsExactly("PUT /api/upload/upl_m/part/2");
        assertThat(events.get(0).getBytesUploaded()).isEqualTo(6);
        assertThat(events.get(0).getPartsCompleted()).isEqualTo(2);
        assertThat(events.get(0).getTotalParts()).isEqualTo(3);
        Call complete = router.calls.get(router.calls.size() - 1);
        assertThat(complete.header("X-Dosya-Source-Ctime")).isEqualTo("1650000000");
    }

    @Test
    void resumeRefusesAWrongSizeAndSingleRequestSessions() {
        router.route("GET /api/upload/upl_m/status",
                statusReply("upl_m", "failed", 10, 4, 3, "[]", false, 0),
                statusReply("upl_m", "failed", 10, null, null, "[]", false, 0),
                statusReply("upl_m", "complete", 10, 4, 3, "[1,2,3]", true, 10));
        UploadResource up = upload();

        assertThat(catchThrowable(() -> up.resume("upl_m", UploadSource.ofBytes(bytes(9)))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("expects 10");
        assertThat(catchThrowable(() -> up.resume("upl_m", UploadSource.ofBytes(bytes(10)))))
                .isInstanceOf(DosyaUploadException.class).hasMessageContaining("single-request");
        assertThat(catchThrowable(() -> up.resume("upl_m", UploadSource.ofBytes(bytes(10)))))
                .isInstanceOf(DosyaUploadException.class).hasMessageContaining("already complete");
        assertThat(router.keys()).allMatch(k -> k.startsWith("GET "));
    }

    // ── Low level ──

    @Test
    void lowLevelMethodsEncodeSessionIdsAndValidatePartNumbers() {
        router.route("GET /api/upload/a%2Fb/status", json(200, "{\"ok\":true,\"session_id\":\"a/b\",\"status\":\"pending\",\"uploaded_parts\":[]}"));
        router.route("PUT /api/upload/a%2Fb/part/2", json(200, "{\"ok\":true,\"part_number\":2,\"etag\":\"x\",\"already_uploaded\":true}"));
        router.route("POST /api/upload/a%2Fb/complete", fileOk(""));
        UploadResource up = upload();

        assertThat(up.status("a/b").getStatus()).isEqualTo("pending");
        UploadPartResult part = up.uploadPart("a/b", 2, bytes(3));
        assertThat(part.getPartNumber()).isEqualTo(2);
        assertThat(part.getEtag()).isEqualTo("x");
        assertThat(part.isAlreadyUploaded()).isTrue();
        UploadResult done = up.complete("a/b", null, Instant.ofEpochSecond(1_650_000_000));
        assertThat(done.getFile().getId()).isEqualTo("file_1");
        assertThat(router.calls.get(2).header("X-Dosya-Source-Ctime")).isEqualTo("1650000000");
        assertThat(router.calls.get(2).header("X-Dosya-Source-Mtime")).isNull();

        assertThat(catchThrowable(() -> up.uploadPart("a/b", 0, bytes(1)))).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> up.status(".."))).isInstanceOf(IllegalArgumentException.class);
        assertThat(router.calls).hasSize(3);
    }

    @Test
    void partPutsAreNeverRetriedByTheTransport() {
        router.route("PUT /api/upload/upl_1/part/1", fail(503, "Service unavailable"));
        assertThat(catchThrowable(() -> retrying().uploadPart("upl_1", 1, bytes(1)))).isInstanceOf(DosyaApiException.class);
        assertThat(router.calls).hasSize(1);
    }

    // ── Batch ──

    @Test
    void batchBuildsASnakeCaseManifestAndMapsResultsToInputOrder() throws IOException {
        router.route("POST /api/upload/batch", json(200, "{\"ok\":true,\"results\":["
                + "{\"field\":\"f2\",\"ok\":true,\"fileId\":\"file_c\",\"name\":\"c.txt\",\"version\":2},"
                + "{\"field\":\"f0\",\"ok\":false,\"error\":\"version_conflict\",\"current_version\":4}]}"));
        Path c = tmp.resolve("c.txt");
        Files.write(c, new byte[3]);

        UploadBatchResult res = upload().batch("ws_1", Arrays.asList(
                UploadBatchFile.of("a.txt", "aa".getBytes(StandardCharsets.UTF_8)).folderId("fld_1").expectedVersion(3)
                        .sourceModifiedAt(1_700_000_000L),
                UploadBatchFile.of("b.txt", new byte[] {1}).sha256("0".repeat(64)),
                UploadBatchFile.of(c).fileId("file_c").sourceCreatedAt(Instant.ofEpochMilli(1_600_000_000_000L))));

        Call call = router.calls.get(0);
        assertThat(call.header("Content-Type")).startsWith("multipart/form-data; boundary=");
        Map<String, Part> form = Part.parse(call);
        JsonObject manifest = JsonParser.parseString(form.get("manifest").text()).getAsJsonObject();
        assertThat(manifest.get("workspace_id").getAsString()).isEqualTo("ws_1");
        JsonArray files = manifest.getAsJsonArray("files");
        assertThat(files.get(0)).isEqualTo(JsonParser.parseString(
                "{\"name\":\"a.txt\",\"folder_id\":\"fld_1\",\"file_id\":null,\"field\":\"f0\",\"expected_version\":3,\"source_modified_at\":1700000000}"));
        assertThat(files.get(1)).isEqualTo(JsonParser.parseString(
                "{\"name\":\"b.txt\",\"folder_id\":null,\"file_id\":null,\"field\":\"f1\",\"sha256\":\"" + "0".repeat(64) + "\"}"));
        assertThat(files.get(2)).isEqualTo(JsonParser.parseString(
                "{\"name\":\"c.txt\",\"folder_id\":null,\"file_id\":\"file_c\",\"field\":\"f2\",\"source_created_at\":1600000000}"));
        assertThat(form.get("manifest").filename).isNull();
        assertThat(form.get("f0").data).isEqualTo("aa".getBytes(StandardCharsets.UTF_8));
        assertThat(form.get("f1").data).containsExactly(1);
        assertThat(form.get("f2").data).hasSize(3);
        assertThat(form.get("f2").filename).isEqualTo("f2");
        assertThat(Long.parseLong(call.header("Content-Length"))).isEqualTo(call.body.length);

        List<UploadBatchResult.Entry> r = res.getResults();
        assertThat(r).hasSize(3);
        assertThat(r.get(0).isOk()).isFalse();
        assertThat(r.get(0).getError()).isEqualTo("version_conflict");
        assertThat(r.get(0).getCurrentVersion()).isEqualTo(4);
        assertThat(r.get(1).isOk()).isFalse();
        assertThat(r.get(1).getError()).isEqualTo("The server did not report a result for this file");
        assertThat(r.get(2).isOk()).isTrue();
        assertThat(r.get(2).getFileId()).isEqualTo("file_c");
        assertThat(r.get(2).getName()).isEqualTo("c.txt");
        assertThat(r.get(2).getVersion()).isEqualTo(2);
    }

    @Test
    void batchValidatesLimitsBeforeSendingAndNeverRetries() {
        router.route("POST /api/upload/batch", fail(503, "Service unavailable"));
        UploadResource up = retrying();
        UploadBatchFile one = UploadBatchFile.of("x", new byte[1]);

        assertThat(catchThrowable(() -> up.batch("ws_1", Collections.nCopies(201, one))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at most 200");
        assertThat(catchThrowable(() -> up.batch("ws_1", Collections.singletonList(UploadBatchFile.of("x", new byte[5 * MIB + 1])))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("5 MiB");
        UploadBatchFile big = UploadBatchFile.of("x", new byte[5 * MIB]);
        assertThat(catchThrowable(() -> up.batch("ws_1", Collections.nCopies(21, big))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("100 MiB");
        assertThat(catchThrowable(() -> up.batch("ws_1", Collections.emptyList()))).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> up.batch(" ", Collections.singletonList(one)))).isInstanceOf(IllegalArgumentException.class);
        assertThat(router.calls).isEmpty();

        assertThat(catchThrowable(() -> up.batch("ws_1", Collections.singletonList(one)))).isInstanceOf(DosyaApiException.class);
        assertThat(router.calls).hasSize(1);
    }

    // ── Many ──

    @Test
    void manyRoutesSmallFilesToBatchAndLargeFilesOrStreamsToFileInInputOrder() {
        byte[] large = new byte[5 * MIB + 1];
        router.handle("POST /api/upload/batch", r -> batchEcho(Part.parse(r), name -> name.equals("bad.txt")
                ? "{\"ok\":false,\"error\":\"folder_locked\"}" : null));
        router.route("POST /api/upload/init", singleInit("upl_1", large.length), singleInit("upl_2", 2));
        router.route("PUT /api/upload/upl_1", json(201, "{\"ok\":true,\"file\":" + fileRow("").replace("\"file_1\"", "\"file_big\"") + "}"));
        router.route("PUT /api/upload/upl_2", json(201, "{\"ok\":true,\"file\":" + fileRow("").replace("\"file_1\"", "\"file_s\"") + "}"));

        List<UploadManyResult> results = upload().many("ws_1", Arrays.asList(
                UploadBatchFile.of("a.txt", new byte[] {1}),
                UploadBatchFile.of("big.bin", large),
                UploadBatchFile.of("bad.txt", new byte[] {'x'}),
                new UploadBatchFile("s.txt", UploadSource.ofStream(new ByteArrayInputStream(new byte[] {1, 2}), 2))),
                new UploadManyOptions().concurrency(1));

        List<Call> batches = router.calls.stream().filter(c -> c.key.equals("POST /api/upload/batch")).collect(Collectors.toList());
        assertThat(batches).hasSize(1);
        JsonArray manifestFiles = JsonParser.parseString(Part.parse(batches.get(0)).get("manifest").text())
                .getAsJsonObject().getAsJsonArray("files");
        assertThat(manifestFiles).hasSize(2);
        assertThat(manifestFiles.get(0).getAsJsonObject().get("name").getAsString()).isEqualTo("a.txt");
        assertThat(manifestFiles.get(1).getAsJsonObject().get("name").getAsString()).isEqualTo("bad.txt");
        assertThat(router.keys()).filteredOn("POST /api/upload/init"::equals).hasSize(2);

        assertThat(results.stream().map(UploadManyResult::isOk)).containsExactly(true, true, false, true);
        assertThat(results.get(0).getName()).isEqualTo("a.txt");
        assertThat(results.get(0).getFileId()).isEqualTo("id_a.txt");
        assertThat(results.get(0).getVersion()).isEqualTo(1);
        assertThat(results.get(1).getFileId()).isEqualTo("file_big");
        assertThat(results.get(1).getFile()).isNotNull();
        assertThat(results.get(2).getName()).isEqualTo("bad.txt");
        assertThat(results.get(2).getError()).isEqualTo("folder_locked");
        assertThat(results.get(2).getCode()).isEqualTo("folder_locked");
        assertThat(results.get(2).getCause()).isNull();
        assertThat(results.get(3).getFileId()).isEqualTo("file_s");
    }

    @Test
    void manyFailsEveryFileOfARefusedBatchWithoutThrowing() {
        router.route("POST /api/upload/batch", fail(403, "No upload permission"));

        List<UploadManyResult> results = upload().many("ws_1", Arrays.asList(
                UploadBatchFile.of("a", new byte[1]), UploadBatchFile.of("b", new byte[1])));

        assertThat(results).hasSize(2);
        for (UploadManyResult r : results) {
            assertThat(r.isOk()).isFalse();
            assertThat(r.getError()).isEqualTo("No upload permission");
            assertThat(r.getCause()).isInstanceOf(DosyaApiException.class);
        }
    }

    @Test
    void manySplitsBatchesAt200Files() {
        router.handle("POST /api/upload/batch", r -> batchEcho(Part.parse(r), name -> null));
        List<UploadBatchFile> files = IntStream.range(0, 450)
                .mapToObj(i -> UploadBatchFile.of("f" + i, new byte[1])).collect(Collectors.toList());

        List<UploadManyResult> results = upload().many("ws_1", files);

        assertThat(router.calls).hasSize(3);
        assertThat(results.stream().map(UploadManyResult::getFileId))
                .containsExactlyElementsOf(files.stream().map(f -> "id_" + f.getName()).collect(Collectors.toList()));
    }

    /** Answers a batch by echoing each manifest entry as stored, unless {@code refusal} returns a result body. */
    private static MockResponse batchEcho(Map<String, Part> form, Function<String, String> refusal) {
        JsonArray files = JsonParser.parseString(form.get("manifest").text()).getAsJsonObject().getAsJsonArray("files");
        StringBuilder sb = new StringBuilder("{\"ok\":true,\"results\":[");
        for (int i = 0; i < files.size(); i++) {
            JsonObject f = files.get(i).getAsJsonObject();
            String name = f.get("name").getAsString();
            String field = f.get("field").getAsString();
            String refused = refusal.apply(name);
            if (i > 0) sb.append(',');
            if (refused != null) {
                sb.append(refused.replace("{", "{\"field\":\"" + field + "\","));
            } else {
                sb.append("{\"field\":\"").append(field).append("\",\"ok\":true,\"fileId\":\"id_").append(name)
                        .append("\",\"name\":\"").append(name).append("\",\"version\":1}");
            }
        }
        return json(200, sb.append("]}").toString());
    }

    // ── Test plumbing ──

    /** One recorded request. */
    static final class Call {
        final String key;
        final Map<String, String> headers = new LinkedHashMap<>();
        final byte[] body;

        Call(RecordedRequest r) {
            String path = r.getPath();
            int q = path.indexOf('?');
            this.key = r.getMethod() + " " + (q < 0 ? path : path.substring(0, q));
            for (String name : r.getHeaders().names()) headers.put(name.toLowerCase(), r.getHeader(name));
            this.body = r.getBody().readByteArray();
        }

        String header(String name) {
            return headers.get(name.toLowerCase());
        }

        String utf8() {
            return new String(body, StandardCharsets.UTF_8);
        }
    }

    /** Routes "METHOD /path" to replies (the last one repeats) or handlers, recording every call. */
    static final class Router extends Dispatcher {
        final List<Call> calls = Collections.synchronizedList(new ArrayList<>());
        private final Map<String, Deque<MockResponse>> replies = new ConcurrentHashMap<>();
        private final Map<String, Function<Call, MockResponse>> handlers = new ConcurrentHashMap<>();

        void route(String key, MockResponse... responses) {
            replies.put(key, new ArrayDeque<>(Arrays.asList(responses)));
        }

        void handle(String key, Function<Call, MockResponse> handler) {
            handlers.put(key, handler);
        }

        List<String> keys() {
            synchronized (calls) {
                return calls.stream().map(c -> c.key).collect(Collectors.toList());
            }
        }

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            Call call = new Call(request);
            calls.add(call);
            Function<Call, MockResponse> handler = handlers.get(call.key);
            if (handler != null) return handler.apply(call);
            Deque<MockResponse> queue = replies.get(call.key);
            if (queue == null) {
                return json(404, "{\"ok\":false,\"error\":\"No route for " + call.key + "\"}");
            }
            synchronized (queue) {
                return queue.size() > 1 ? queue.poll() : queue.peek();
            }
        }
    }

    /** One part of a recorded multipart/form-data body. */
    static final class Part {
        String name;
        String filename;
        byte[] data;

        String text() {
            return new String(data, StandardCharsets.UTF_8);
        }

        static Map<String, Part> parse(Call call) {
            String type = call.header("Content-Type");
            String boundary = type.substring(type.indexOf("boundary=") + "boundary=".length());
            // ISO-8859-1 maps bytes to chars one to one.
            String body = new String(call.body, StandardCharsets.ISO_8859_1);
            Map<String, Part> parts = new LinkedHashMap<>();
            String delimiter = "--" + boundary;
            int pos = body.indexOf(delimiter);
            while (pos >= 0) {
                int start = pos + delimiter.length();
                if (body.startsWith("--", start)) break;
                start += 2; // CRLF
                int headerEnd = body.indexOf("\r\n\r\n", start);
                String headers = body.substring(start, headerEnd);
                int next = body.indexOf("\r\n" + delimiter, headerEnd + 4);
                Part part = new Part();
                part.data = body.substring(headerEnd + 4, next).getBytes(StandardCharsets.ISO_8859_1);
                for (String token : headers.split("\r\n")[0].split(";")) {
                    String t = token.trim();
                    if (t.startsWith("name=")) part.name = t.substring(6, t.length() - 1);
                    if (t.startsWith("filename=")) part.filename = t.substring(10, t.length() - 1);
                }
                parts.put(part.name, part);
                pos = next + 2;
            }
            return parts;
        }
    }
}
