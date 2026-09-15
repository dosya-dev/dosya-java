package dev.dosya.sdk.resource;

import com.google.gson.JsonParser;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.model.ArchiveListing;
import dev.dosya.sdk.model.DownloadLink;
import dev.dosya.sdk.model.DownloadOptions;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class DownloadResourceTest extends ApiTestSupport {

    @TempDir
    Path tmp;

    private DownloadResource download() {
        return new DownloadResource(http());
    }

    private String storageUrl() {
        return "http://localhost:" + server.getPort() + "/bucket/obj?X-Amz-Signature=abc";
    }

    /** The bare (un-enveloped) JSON that download-url answers. */
    private MockResponse link() {
        return new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("{\"url\":\"" + storageUrl() + "\",\"size\":5,\"name\":\"a.txt\",\"region\":\"apac\"}");
    }

    private static MockResponse bytes(int status, byte... data) {
        return new MockResponse().setResponseCode(status).setBody(new Buffer().write(data));
    }

    // ── getUrl ──

    @Test
    void getUrlReadsTheBareJsonResponseAndComputesExpiresAt() throws Exception {
        enqueue(link());
        long before = System.currentTimeMillis() / 1000;

        DownloadLink l = download().getUrl("f/1", new DownloadOptions().version(2).unlockToken("tok").ttl(900));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files/f%2F1/download-url");
        assertThat(r.getRequestUrl().queryParameter("version")).isEqualTo("2");
        assertThat(r.getRequestUrl().queryParameter("ut")).isEqualTo("tok");
        assertThat(r.getRequestUrl().queryParameter("ttl")).isEqualTo("900");
        assertThat(l.getUrl()).isEqualTo(storageUrl());
        assertThat(l.getSize()).isEqualTo(5);
        assertThat(l.getName()).isEqualTo("a.txt");
        assertThat(l.getRegion()).isEqualTo("apac");
        assertThat(l.getExpiresAt()).isBetween(before + 900, before + 901);
    }

    @Test
    void getUrlMirrorsTheServerTtlClamp() throws Exception {
        enqueue(link(), link());
        long now = System.currentTimeMillis() / 1000;

        assertThat(download().getUrl("f1").getExpiresAt() - now).isBetween(300L, 301L);
        assertThat(take().getRequestUrl().querySize()).isZero();
        assertThat(download().getUrl("f1", new DownloadOptions().ttl(99_999)).getExpiresAt() - now).isBetween(3600L, 3601L);
    }

    @Test
    void getUrlSurfacesApiErrorsInsteadOfReturningTheEndpoint() {
        enqueue(fail(403, "This file is view-only and cannot be downloaded"));

        DosyaApiException e = catchThrowableOfType(() -> download().getUrl("f1"), DosyaApiException.class);

        assertThat(e.getStatus()).isEqualTo(403);
        assertThat(e.getErrorMessage()).contains("view-only");
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedGetUrlStillReturnsTheUrlString() {
        enqueue(link());
        assertThat(download().getUrl("f1", 1, null)).isEqualTo(storageUrl());
    }

    // ── Bytes ──

    @Test
    void downloadBytesFetchesThePresignedUrlWithoutTheApiKeyAndPassesRange() throws Exception {
        enqueue(link(), bytes(206, (byte) 2, (byte) 3));

        byte[] data = download().downloadBytes("f1", new DownloadOptions().range(1, 2));

        assertThat(data).containsExactly(2, 3);
        RecordedRequest api = take();
        assertThat(api.getHeader("Authorization")).isEqualTo("Bearer dos_test_key");
        RecordedRequest storage = take();
        assertThat(pathOf(storage)).isEqualTo("/bucket/obj");
        assertThat(storage.getRequestUrl().queryParameter("X-Amz-Signature")).isEqualTo("abc");
        assertThat(storage.getHeader("Authorization")).isNull();
        assertThat(storage.getHeader("Range")).isEqualTo("bytes=1-2");
    }

    @Test
    void downloadStreamAndDownloadToWork() throws Exception {
        enqueue(link(), bytes(206, (byte) 2, (byte) 3), link(), bytes(200, (byte) 1, (byte) 2, (byte) 3));

        try (InputStream in = download().downloadStream("f1", new DownloadOptions().range(1))) {
            assertThat(in.readAllBytes()).containsExactly(2, 3);
        }
        take();
        assertThat(take().getHeader("Range")).isEqualTo("bytes=1-");

        Path target = tmp.resolve("out.bin");
        long written = download().downloadTo("f1", target);
        assertThat(written).isEqualTo(3);
        assertThat(Files.readAllBytes(target)).containsExactly(1, 2, 3);
        try (java.util.stream.Stream<Path> files = Files.list(tmp)) {
            assertThat(files).containsExactly(target);
        }
    }

    @Test
    void aStorageErrorSurfacesWithItsStatus() throws IOException {
        enqueue(link(), new MockResponse().setResponseCode(403).setBody("<Error>SignatureDoesNotMatch</Error>"));
        Path target = tmp.resolve("out.bin");

        DosyaApiException e = catchThrowableOfType(() -> download().downloadTo("f1", target), DosyaApiException.class);

        assertThat(e.getStatus()).isEqualTo(403);
        assertThat(e.getRaw()).contains("SignatureDoesNotMatch");
        assertThat(Files.exists(target)).isFalse();
        try (java.util.stream.Stream<Path> files = Files.list(tmp)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    void anInvalidRangeIsRejectedBeforeAnyRequest() {
        assertThat(catchThrowable(() -> new DownloadOptions().range(5, 2))).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> new DownloadOptions().range(-1))).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    // ── raw / thumbnail ──

    @Test
    void rawSendsRangeIfNoneMatchAndQuery() throws Exception {
        enqueue(bytes(206, (byte) 1));

        HttpResponse<InputStream> res = download().raw("f 1",
                new DownloadOptions().version(3).unlockToken("u").range(0, 0).ifNoneMatch("\"e1\""));

        assertThat(res.statusCode()).isEqualTo(206);
        try (InputStream in = res.body()) {
            assertThat(in.readAllBytes()).containsExactly(1);
        }
        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/files/f%201/raw");
        assertThat(r.getRequestUrl().queryParameter("version")).isEqualTo("3");
        assertThat(r.getRequestUrl().queryParameter("ut")).isEqualTo("u");
        assertThat(r.getHeader("Range")).isEqualTo("bytes=0-0");
        assertThat(r.getHeader("If-None-Match")).isEqualTo("\"e1\"");
    }

    @Test
    void aRefusedRawReadThrows() {
        enqueue(fail(404, "File not found"));
        assertThat(catchThrowable(() -> download().raw("f1"))).isInstanceOf(DosyaApiException.class);
    }

    @Test
    void thumbnailAcceptsOnlyTheServerWidths() throws Exception {
        enqueue(bytes(200, (byte) 9).setHeader("Content-Type", "image/webp"));

        HttpResponse<InputStream> res = download().thumbnail("f1", 256, new DownloadOptions().version(2));
        res.body().close();

        assertThat(res.headers().firstValue("content-type")).hasValue("image/webp");
        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/files/f1/thumb");
        assertThat(r.getRequestUrl().queryParameter("w")).isEqualTo("256");
        assertThat(r.getRequestUrl().queryParameter("version")).isEqualTo("2");
        assertThat(catchThrowable(() -> download().thumbnail("f1", 300))).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    // ── Archives ──

    @Test
    void archivePostsSnakeCaseIdsAndReturnsTheStream() throws Exception {
        enqueue(bytes(200, (byte) 0x50, (byte) 0x4b).setHeader("Content-Type", "application/zip"));

        HttpResponse<InputStream> res = download().archive(null, Collections.singletonList("fld_1"));
        res.body().close();

        assertThat(res.headers().firstValue("content-type")).hasValue("application/zip");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/download-archive");
        assertThat(jsonBody(r)).isEqualTo(JsonParser.parseString("{\"folder_ids\":[\"fld_1\"]}"));
    }

    @Test
    void archiveSendsBothListsWhenGiven() throws Exception {
        enqueue(bytes(200, (byte) 0x50));
        download().archive(Arrays.asList("f1", "f2"), Collections.singletonList("d1")).body().close();
        assertThat(jsonBody(take())).isEqualTo(JsonParser.parseString("{\"file_ids\":[\"f1\",\"f2\"],\"folder_ids\":[\"d1\"]}"));
    }

    @Test
    void archiveRequiresAtLeastOneId() {
        assertThat(catchThrowable(() -> download().archive(null, null))).isInstanceOf(IllegalArgumentException.class);
        assertThat(catchThrowable(() -> download().archive(Collections.emptyList(), Collections.emptyList())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void archiveEntriesReadsTheBareCamelCaseListing() throws Exception {
        enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("{\"archiveSize\":100,\"totalEntries\":2,\"truncated\":true,\"entries\":["
                        + "{\"i\":0,\"name\":\"a.txt\",\"size\":3,\"csize\":2,\"method\":8,\"dir\":false,\"encrypted\":false,\"mtime\":null},"
                        + "{\"i\":1,\"name\":\"d/\",\"size\":0,\"csize\":0,\"method\":0,\"dir\":true,\"encrypted\":false,\"mtime\":1700000000}]}"));

        ArchiveListing listing = download().archiveEntries("f1", new DownloadOptions().unlockToken("u"));

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/files/f1/archive");
        assertThat(r.getRequestUrl().queryParameter("ut")).isEqualTo("u");
        assertThat(r.getRequestUrl().querySize()).isEqualTo(1);
        assertThat(listing.getArchiveSize()).isEqualTo(100);
        assertThat(listing.getTotalEntries()).isEqualTo(2);
        assertThat(listing.isTruncated()).isTrue();
        assertThat(listing.getEntries()).hasSize(2);
        ArchiveListing.Entry a = listing.getEntries().get(0);
        assertThat(a.getIndex()).isZero();
        assertThat(a.getName()).isEqualTo("a.txt");
        assertThat(a.getSize()).isEqualTo(3);
        assertThat(a.getCompressedSize()).isEqualTo(2);
        assertThat(a.getMethod()).isEqualTo(8);
        assertThat(a.getMtime()).isNull();
        assertThat(listing.getEntries().get(1).isDirectory()).isTrue();
        assertThat(listing.getEntries().get(1).getMtime()).isEqualTo(1_700_000_000L);
    }

    @Test
    void archiveEntryStreamsOneEntryByIndex() throws Exception {
        enqueue(new MockResponse().setResponseCode(200).setBody("abc"));

        HttpResponse<InputStream> res = download().archiveEntry("f1", 0, new DownloadOptions().attachment(true));
        try (InputStream in = res.body()) {
            assertThat(new String(in.readAllBytes())).isEqualTo("abc");
        }

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/files/f1/archive/entry");
        assertThat(r.getRequestUrl().queryParameter("i")).isEqualTo("0");
        assertThat(r.getRequestUrl().queryParameter("dl")).isEqualTo("1");
        assertThat(catchThrowable(() -> download().archiveEntry("f1", -1))).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void archiveErrorsSurface() {
        enqueue(fail(415, "This file is not a zip archive."));
        DosyaApiException e = catchThrowableOfType(() -> download().archiveEntries("f1"), DosyaApiException.class);
        assertThat(e.getStatus()).isEqualTo(415);
    }
}
