package dev.dosya.sdk.internal;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaNetworkException;
import dev.dosya.sdk.exception.DosyaTimeoutException;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/** 0.3 transport rules: retries, timeouts, PATCH, errors, bookmarks, ids. */
class TransportRulesTest extends ApiTestSupport {

    private DosyaHttpClient retrying() {
        return new DosyaHttpClient(options().maxRetries(3));
    }

    @Test
    void defaultsToApiHost() {
        assertThat(new dev.dosya.sdk.DosyaClientOptions("dos_x").getBaseUrl()).isEqualTo("https://api.dosya.dev");
    }

    @Test
    void doesNotRetryPostOn500() {
        enqueue(fail(500, "boom"), ok());
        assertThatThrownBy(() -> retrying().request(HttpRequest.post("/api/x").body(new HashMap<>())))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void doesNotRetryPostOn503EvenWithRetryAfter() {
        enqueue(fail(503, "Workspace is moving").setHeader("Retry-After", "0"), ok());
        assertThatThrownBy(() -> retrying().request(HttpRequest.post("/api/x")))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void retriesGetOn503WithRetryAfter() {
        enqueue(fail(503, "down").setHeader("Retry-After", "0"), ok("\"id\":1"));
        JsonObject res = retrying().request(HttpRequest.get("/api/x"));
        assertThat(res.get("id").getAsInt()).isEqualTo(1);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void retriesPostOn429WithRetryAfter() {
        enqueue(fail(429, "Too many requests").setHeader("Retry-After", "0"), ok());
        retrying().request(HttpRequest.post("/api/x"));
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void doesNotRetry429WithoutRetryAfter() {
        enqueue(fail(429, "Daily limit of 20 remote downloads reached"), ok());
        assertThatThrownBy(() -> retrying().request(HttpRequest.get("/api/x"))).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void surfacesRetryAfterLongerThanMaxDelay() {
        enqueue(fail(429, "Usage limit").setHeader("Retry-After", "3600"), ok());
        DosyaApiException e = catchThrowableOfType(() -> retrying().request(HttpRequest.get("/api/x")), DosyaApiException.class);
        assertThat(e.getRetryAfterSeconds()).isEqualTo(3600L);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void neverMeansOneAttempt() {
        enqueue(fail(503, "down").setHeader("Retry-After", "0"), ok());
        assertThatThrownBy(() -> retrying().request(HttpRequest.get("/api/x").retry(HttpRequest.Retry.NEVER)))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void doesNotRetryPostAfterNetworkError() {
        enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START), ok());
        assertThatThrownBy(() -> retrying().request(HttpRequest.post("/api/x")))
                .isInstanceOf(DosyaNetworkException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void timesOutABodyThatStallsAfterHeaders() {
        enqueue(ok("\"padding\":\"" + "x".repeat(4096) + "\"").throttleBody(16, 1, TimeUnit.SECONDS));
        DosyaHttpClient client = new DosyaHttpClient(options().readTimeout(200));
        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/x"))).isInstanceOf(DosyaTimeoutException.class);
    }

    @Test
    void sendsPatch() throws Exception {
        enqueue(ok());
        Map<String, Object> body = new HashMap<>();
        body.put("max_downloads", 5);
        http().request(HttpRequest.patch("/api/shares/shr_1").body(body));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PATCH");
        assertThat(jsonBody(r).get("max_downloads").getAsInt()).isEqualTo(5);
    }

    @Test
    void keepsExplicitNullsInBodies() throws Exception {
        enqueue(ok());
        Map<String, Object> body = new HashMap<>();
        body.put("parent_id", null);
        http().request(HttpRequest.put("/api/folders/f/move").body(body));
        assertThat(jsonBody(take()).has("parent_id")).isTrue();
    }

    @Test
    void exposesMachineCodeAndDetails() {
        enqueue(new MockResponse().setResponseCode(403)
                .setBody("{\"ok\":false,\"error\":\"folder_locked\",\"folder_id\":\"fld_1\",\"lock_mode\":\"full_lock\"}"));
        DosyaApiException e = catchThrowableOfType(() -> http().request(HttpRequest.get("/api/files")), DosyaApiException.class);
        assertThat(e.getCode()).isEqualTo("folder_locked");
        assertThat(e.getDetails().get("folder_id").getAsString()).isEqualTo("fld_1");
        assertThat(e.getMethod()).isEqualTo("GET");
        assertThat(e.getPath()).isEqualTo("/api/files");
    }

    @Test
    void doesNotInventACodeFromASentence() {
        enqueue(fail(404, "File not found"));
        DosyaApiException e = catchThrowableOfType(() -> http().request(HttpRequest.get("/api/x")), DosyaApiException.class);
        assertThat(e.getCode()).isNull();
    }

    @Test
    void readsCodeField() {
        enqueue(new MockResponse().setResponseCode(400).setBody("{\"ok\":false,\"error\":\"This address isn't allowed\",\"code\":\"ssrf_blocked\"}"));
        DosyaApiException e = catchThrowableOfType(() -> http().request(HttpRequest.post("/api/remote-downloads")), DosyaApiException.class);
        assertThat(e.getCode()).isEqualTo("ssrf_blocked");
    }

    @Test
    void reportsHtmlErrorPageByStatus() {
        enqueue(new MockResponse().setResponseCode(404).setBody("<html>nope</html>"));
        DosyaApiException e = catchThrowableOfType(() -> http().request(HttpRequest.get("/api/x")), DosyaApiException.class);
        assertThat(e.getErrorMessage()).startsWith("HTTP 404");
    }

    @Test
    void acceptsBareJsonWhenEnvelopeIsOff() {
        enqueue(new MockResponse().setBody("{\"url\":\"https://r2/x\",\"size\":3}"));
        JsonObject res = http().request(HttpRequest.get("/api/files/f/download-url").envelope(false));
        assertThat(res.get("url").getAsString()).isEqualTo("https://r2/x");
    }

    @Test
    void echoesTheNewestBookmark() throws Exception {
        String b1 = "00000085-0000024c-00004c6d-8e61117bf38d7adb71b934ebbf891683";
        String b2 = "00000085-0000024c-00004c6e-8e61117bf38d7adb71b934ebbf891683";
        enqueue(ok().setHeader("X-D1-Bookmark", b2), ok().setHeader("X-D1-Bookmark", b1), ok());
        DosyaHttpClient client = http();
        client.request(HttpRequest.post("/api/a"));
        client.request(HttpRequest.get("/api/b"));
        client.request(HttpRequest.get("/api/c"));
        assertThat(take().getHeader("X-D1-Bookmark")).isNull();
        assertThat(take().getHeader("X-D1-Bookmark")).isEqualTo(b2);
        assertThat(take().getHeader("X-D1-Bookmark")).isEqualTo(b2);
    }

    @Test
    void bookmarkCanBeTurnedOff() throws Exception {
        enqueue(ok().setHeader("X-D1-Bookmark", "00000085-0000024c-00004c6d-8e61117bf38d7adb71b934ebbf891683"), ok());
        DosyaHttpClient client = new DosyaHttpClient(options().readYourWrites(false));
        client.request(HttpRequest.get("/api/a"));
        client.request(HttpRequest.get("/api/a"));
        take();
        assertThat(take().getHeader("X-D1-Bookmark")).isNull();
    }

    @Test
    void segEncodesAndRejectsDotSegments() {
        assertThat(PathSegments.seg("../workspaces/ws_1")).isEqualTo("..%2Fworkspaces%2Fws_1");
        assertThat(PathSegments.seg("a b?c#d")).isEqualTo("a%20b%3Fc%23d");
        assertThatThrownBy(() -> PathSegments.seg("..")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PathSegments.seg(".")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PathSegments.seg("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parsesRetryAfterDates() {
        assertThat(DosyaHttpClient.parseRetryAfter("5")).isEqualTo(5L);
        assertThat(DosyaHttpClient.parseRetryAfter("soon")).isNull();
        assertThat(DosyaHttpClient.parseRetryAfter("Thu, 01 Jan 1970 00:00:10 GMT")).isEqualTo(0L);
    }
}
