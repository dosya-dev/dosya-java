package dev.dosya.sdk.internal;

import dev.dosya.sdk.DosyaClientOptions;
import dev.dosya.sdk.DosyaInterceptor;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaNetworkException;
import dev.dosya.sdk.model.RateLimitInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DosyaHttpClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    /**
     * Creates DosyaClientOptions with the MockWebServer's base URL.
     * Uses reflection to bypass the HTTPS-only check on baseUrl.
     */
    private static DosyaClientOptions testOptions(String baseUrl) {
        DosyaClientOptions options = new DosyaClientOptions("dos_test_key_123");
        try {
            Field field = DosyaClientOptions.class.getDeclaredField("baseUrl");
            field.setAccessible(true);
            field.set(options, baseUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set baseUrl via reflection", e);
        }
        return options;
    }

    private DosyaClientOptions defaultTestOptions() {
        return testOptions(server.url("/").toString());
    }

    // ---- Successful requests ----

    @Test
    void successfulJsonRequestReturnsParsedResponse() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true,\"data\":{\"id\":\"file-1\",\"name\":\"test.txt\"}}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        JsonObject result = client.request(HttpRequest.get("/api/v1/files/file-1"));

        assertThat(result.has("data")).isTrue();
        JsonObject data = result.getAsJsonObject("data");
        assertThat(data.get("id").getAsString()).isEqualTo("file-1");
        assertThat(data.get("name").getAsString()).isEqualTo("test.txt");
    }

    @Test
    void requestSendsAuthorizationHeader() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        client.request(HttpRequest.get("/api/v1/me"));

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer dos_test_key_123");
    }

    @Test
    void requestSendsUserAgentHeader() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        client.request(HttpRequest.get("/api/v1/me"));

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getHeader("User-Agent")).startsWith("dosya-java/");
    }

    // ---- API error responses ----

    @Test
    void apiErrorResponseThrowsDosyaApiException() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .setHeader("X-Request-Id", "req-abc")
                .setBody("{\"ok\":false,\"error\":\"Forbidden: insufficient permissions\"}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(403);
                    assertThat(apiEx.getErrorMessage()).isEqualTo("Forbidden: insufficient permissions");
                    assertThat(apiEx.getRaw()).contains("Forbidden");
                    assertThat(apiEx.getRequestId()).isEqualTo("req-abc");
                });
    }

    @Test
    void apiErrorWithMissingOkFieldThrowsException() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"error\":\"something wrong\"}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getErrorMessage()).isEqualTo("something wrong");
                });
    }

    @Test
    void apiErrorWithOkFalseAndNoErrorFieldUsesUnknown() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":false}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getErrorMessage()).isEqualTo("Unknown error");
                });
    }

    // ---- Invalid JSON ----

    @Test
    void invalidJsonResponseThrowsDosyaApiExceptionWithCause() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("X-Request-Id", "req-bad-json")
                .setBody("this is not json at all"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getErrorMessage()).contains("Invalid JSON response");
                    assertThat(apiEx.getRequestId()).isEqualTo("req-bad-json");
                    assertThat(apiEx.getCause()).isNotNull();
                });
    }

    // ---- Request ID extraction ----

    @Test
    void requestIdExtractedFromHeader() {
        server.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader("X-Request-Id", "req-unique-id-999")
                .setBody("{\"ok\":false,\"error\":\"Validation failed\"}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getRequestId()).isEqualTo("req-unique-id-999");
                });
    }

    // ---- Retry on 429 with Retry-After ----

    @Test
    void retryOn429WithRetryAfterHeader() throws InterruptedException {
        // First response: 429 rate limited with Retry-After of 1 second
        server.enqueue(new MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "1")
                .setBody("{\"ok\":false,\"error\":\"Rate limited\"}"));

        // Second response: success
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true,\"data\":\"retried\"}"));

        DosyaClientOptions options = defaultTestOptions();
        options.maxRetries(2);
        DosyaHttpClient client = new DosyaHttpClient(options);

        JsonObject result = client.request(HttpRequest.get("/api/v1/files"));
        assertThat(result.get("data").getAsString()).isEqualTo("retried");

        // Verify two requests were made
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void retryOn429ExhaustsRetriesAndThrows() {
        // All responses are 429
        DosyaClientOptions options = defaultTestOptions();
        options.maxRetries(1).baseDelay(10).maxDelay(50);

        for (int i = 0; i <= 1; i++) {
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "0")
                    .setBody("{\"ok\":false,\"error\":\"Rate limited\"}"));
        }

        DosyaHttpClient client = new DosyaHttpClient(options);

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(429);
                    assertThat(apiEx.getErrorMessage()).isEqualTo("Rate limited");
                });
    }

    // ---- Retry on 5xx ----

    @Test
    void retryOn5xxWithExponentialBackoff() throws InterruptedException {
        // First response: 500
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"ok\":false,\"error\":\"Internal error\"}"));

        // Second response: 502
        server.enqueue(new MockResponse()
                .setResponseCode(502)
                .setBody("{\"ok\":false,\"error\":\"Bad gateway\"}"));

        // Third response: success
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true,\"data\":\"recovered\"}"));

        DosyaClientOptions options = defaultTestOptions();
        options.maxRetries(3).baseDelay(10).maxDelay(100);
        DosyaHttpClient client = new DosyaHttpClient(options);

        JsonObject result = client.request(HttpRequest.get("/api/v1/files"));
        assertThat(result.get("data").getAsString()).isEqualTo("recovered");
        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    @Test
    void retryOn5xxExhaustsRetriesAndThrows() {
        DosyaClientOptions options = defaultTestOptions();
        options.maxRetries(1).baseDelay(10).maxDelay(50);

        // Two 500 responses (initial + 1 retry)
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"ok\":false,\"error\":\"Server error\"}"));
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"ok\":false,\"error\":\"Server error\"}"));

        DosyaHttpClient client = new DosyaHttpClient(options);

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(500);
                });
    }

    // ---- Network error ----

    @Test
    void networkErrorThrowsDosyaNetworkExceptionAfterRetries() throws IOException {
        // Shut down the server to force connection errors
        server.shutdown();

        DosyaClientOptions options = testOptions("http://localhost:1");  // unreachable port
        options.maxRetries(1).baseDelay(10).maxDelay(50).connectTimeout(100).readTimeout(100);
        DosyaHttpClient client = new DosyaHttpClient(options);

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files")))
                .isInstanceOf(DosyaNetworkException.class)
                .satisfies(ex -> {
                    assertThat(ex.getMessage()).contains("failed after");
                    assertThat(ex.getCause()).isNotNull();
                });
    }

    // ---- Rate limit headers extraction ----

    @Test
    void rateLimitHeadersExtractedAndPassedToCallback() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("X-RateLimit-Limit", "100")
                .setHeader("X-RateLimit-Remaining", "95")
                .setHeader("X-RateLimit-Reset", "1700000060")
                .setBody("{\"ok\":true}"));

        AtomicReference<RateLimitInfo> captured = new AtomicReference<>();
        DosyaClientOptions options = defaultTestOptions();
        options.onRateLimit(captured::set);
        DosyaHttpClient client = new DosyaHttpClient(options);

        client.request(HttpRequest.get("/api/v1/files"));

        RateLimitInfo info = captured.get();
        assertThat(info).isNotNull();
        assertThat(info.getLimit()).isEqualTo(100);
        assertThat(info.getRemaining()).isEqualTo(95);
        assertThat(info.getResetAt()).isEqualTo(1700000060L);
    }

    @Test
    void rateLimitCallbackNotCalledWhenHeadersMissing() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        AtomicReference<RateLimitInfo> captured = new AtomicReference<>();
        DosyaClientOptions options = defaultTestOptions();
        options.onRateLimit(captured::set);
        DosyaHttpClient client = new DosyaHttpClient(options);

        client.request(HttpRequest.get("/api/v1/files"));

        assertThat(captured.get()).isNull();
    }

    @Test
    void rateLimitCallbackNotCalledWhenNoCallbackRegistered() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("X-RateLimit-Limit", "100")
                .setHeader("X-RateLimit-Remaining", "95")
                .setHeader("X-RateLimit-Reset", "1700000060")
                .setBody("{\"ok\":true}"));

        // No onRateLimit callback set - should not throw
        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        JsonObject result = client.request(HttpRequest.get("/api/v1/files"));
        assertThat(result).isNotNull();
    }

    // ---- Interceptor ----

    @Test
    void interceptorBeforeRequestAndAfterResponseCalled() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("X-Request-Id", "req-intercepted")
                .setBody("{\"ok\":true}"));

        List<String> beforeCalls = new ArrayList<>();
        List<String> afterCalls = new ArrayList<>();

        DosyaInterceptor interceptor = new DosyaInterceptor() {
            @Override
            public void beforeRequest(String method, String url) {
                beforeCalls.add(method + " " + url);
            }

            @Override
            public void afterResponse(String method, String url, int statusCode, String requestId, long durationMs) {
                afterCalls.add(method + " " + url + " -> " + statusCode + " [" + requestId + "] " + durationMs + "ms");
            }
        };

        DosyaClientOptions options = defaultTestOptions();
        options.interceptor(interceptor);
        DosyaHttpClient client = new DosyaHttpClient(options);

        client.request(HttpRequest.get("/api/v1/files"));

        assertThat(beforeCalls).hasSize(1);
        assertThat(beforeCalls.get(0)).contains("GET");
        assertThat(beforeCalls.get(0)).contains("/api/v1/files");

        assertThat(afterCalls).hasSize(1);
        assertThat(afterCalls.get(0)).contains("GET");
        assertThat(afterCalls.get(0)).contains("200");
        assertThat(afterCalls.get(0)).contains("req-intercepted");
    }

    // ---- URL encoding of query parameters ----

    @Test
    void queryParametersAreUrlEncoded() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        HttpRequest req = HttpRequest.get("/api/v1/search")
                .query("q", "hello world")
                .query("type", "file&folder");

        client.request(req);

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        String path = recorded.getPath();
        assertThat(path).contains("q=hello+world");
        assertThat(path).contains("type=file%26folder");
    }

    @Test
    void requestWithNoQueryParamsHasNoQueryString() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        client.request(HttpRequest.get("/api/v1/me"));

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getPath()).isEqualTo("/api/v1/me");
    }

    // ---- Debug callback ----

    @Test
    void debugCallbackReceivesMessages() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        List<String> debugMessages = new ArrayList<>();
        DosyaClientOptions options = defaultTestOptions();
        options.debug(debugMessages::add);
        DosyaHttpClient client = new DosyaHttpClient(options);

        client.request(HttpRequest.get("/api/v1/me"));

        assertThat(debugMessages).isNotEmpty();
        // Should have at least the request attempt message and the response message
        assertThat(debugMessages).anyMatch(msg -> msg.contains("GET") && msg.contains("attempt"));
        assertThat(debugMessages).anyMatch(msg -> msg.contains("200"));
    }

    @Test
    void debugCallbackReceivesRetryMessages() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"ok\":false,\"error\":\"err\"}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        List<String> debugMessages = new ArrayList<>();
        DosyaClientOptions options = defaultTestOptions();
        options.debug(debugMessages::add).maxRetries(1).baseDelay(10).maxDelay(50);
        DosyaHttpClient client = new DosyaHttpClient(options);

        client.request(HttpRequest.get("/api/v1/files"));

        assertThat(debugMessages).anyMatch(msg -> msg.contains("Server error 500"));
    }

    // ---- doFetch directly ----

    @Test
    void doFetchReturnsHttpResult() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("X-Request-Id", "req-fetch")
                .setBody("{\"ok\":true,\"result\":\"direct\"}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        DosyaHttpClient.HttpResult result = client.doFetch(HttpRequest.get("/api/v1/test"));

        assertThat(result.statusCode).isEqualTo(200);
        assertThat(result.body).contains("direct");
        assertThat(result.requestId).isEqualTo("req-fetch");
    }

    // ---- POST with JSON body ----

    @Test
    void postRequestSendsJsonBody() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        java.util.Map<String, String> body = new java.util.LinkedHashMap<>();
        body.put("name", "My Folder");
        body.put("workspaceId", "ws-1");

        HttpRequest req = HttpRequest.post("/api/v1/folders").body(body);
        client.request(req);

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getHeader("Content-Type")).isEqualTo("application/json");
        String sentBody = recorded.getBody().readUtf8();
        assertThat(sentBody).contains("My Folder");
        assertThat(sentBody).contains("ws-1");
    }

    // ---- PUT with raw body ----

    @Test
    void putRequestSendsRawBody() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        byte[] data = "raw binary data".getBytes();
        HttpRequest req = HttpRequest.put("/api/v1/upload/part")
                .rawBody(data)
                .header("Content-Type", "application/octet-stream");
        client.request(req);

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo("PUT");
        assertThat(recorded.getBody().readUtf8()).isEqualTo("raw binary data");
    }

    // ---- DELETE request ----

    @Test
    void deleteRequestWorks() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        client.request(HttpRequest.delete("/api/v1/files/file-1"));

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getMethod()).isEqualTo("DELETE");
        assertThat(recorded.getPath()).isEqualTo("/api/v1/files/file-1");
    }

    // ---- No retries for 4xx (non-429) ----

    @Test
    void noRetryOn4xxOtherThan429() {
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"ok\":false,\"error\":\"Not found\"}"));

        DosyaClientOptions options = defaultTestOptions();
        options.maxRetries(3);
        DosyaHttpClient client = new DosyaHttpClient(options);

        assertThatThrownBy(() -> client.request(HttpRequest.get("/api/v1/files/nonexistent")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(ex -> {
                    DosyaApiException apiEx = (DosyaApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(404);
                });

        // Only 1 request should be made (no retries)
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    // ---- Getter methods on DosyaHttpClient ----

    @Test
    void getterMethodsReturnConfiguredValues() {
        DosyaClientOptions options = defaultTestOptions();
        options.maxRetries(5).baseDelay(250).maxDelay(60000);
        DosyaHttpClient client = new DosyaHttpClient(options);

        assertThat(client.getMaxRetries()).isEqualTo(5);
        assertThat(client.getBaseDelay()).isEqualTo(250);
        assertThat(client.getMaxDelay()).isEqualTo(60000);
        assertThat(client.getGson()).isNotNull();
    }

    @Test
    void baseUrlTrailingSlashIsStripped() {
        DosyaClientOptions options = testOptions("http://localhost:9999/");
        DosyaHttpClient client = new DosyaHttpClient(options);
        assertThat(client.getBaseUrl()).isEqualTo("http://localhost:9999");
    }

    // ---- Static encode method ----

    @Test
    void encodeHandlesSpecialCharacters() {
        assertThat(DosyaHttpClient.encode("hello world")).isEqualTo("hello+world");
        assertThat(DosyaHttpClient.encode("a&b=c")).isEqualTo("a%26b%3Dc");
        assertThat(DosyaHttpClient.encode("simple")).isEqualTo("simple");
    }

    // ---- Gson serialization/deserialization helpers ----

    @Test
    void fromJsonWorksWithClass() {
        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        json.addProperty("id", "test-id");
        json.addProperty("name", "test-name");

        // Use a simple type to test fromJson
        com.google.gson.JsonElement element = json;
        String result = client.fromJson(json.get("id"), String.class);
        assertThat(result).isEqualTo("test-id");
    }

    @Test
    void toJsonTreeProducesJsonElement() {
        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("key", "value");

        com.google.gson.JsonElement tree = client.toJsonTree(map);
        assertThat(tree.isJsonObject()).isTrue();
        assertThat(tree.getAsJsonObject().get("key").getAsString()).isEqualTo("value");
    }

    // ---- requestAs ----

    @Test
    void requestAsDeserializesToType() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true,\"workspace_id\":\"ws-1\",\"folder_id\":\"fold-1\"}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        // Use a known model type that can be deserialized from the response
        dev.dosya.sdk.model.ListFilesResponse resp = client.requestAs(
                HttpRequest.get("/api/v1/files"),
                dev.dosya.sdk.model.ListFilesResponse.class);

        assertThat(resp).isNotNull();
        assertThat(resp.getWorkspaceId()).isEqualTo("ws-1");
        assertThat(resp.getFolderId()).isEqualTo("fold-1");
    }

    // ---- Custom headers ----

    @Test
    void customHeadersAreSent() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"ok\":true}"));

        DosyaHttpClient client = new DosyaHttpClient(defaultTestOptions());

        HttpRequest req = HttpRequest.get("/api/v1/files")
                .header("X-Custom-Header", "custom-value");
        client.request(req);

        RecordedRequest recorded = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getHeader("X-Custom-Header")).isEqualTo("custom-value");
    }
}
