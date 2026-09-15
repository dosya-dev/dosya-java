package dev.dosya.sdk.testing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dosya.sdk.DosyaClientOptions;
import dev.dosya.sdk.internal.DosyaHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Base for resource tests: a real {@link DosyaHttpClient} against a MockWebServer.
 *
 * <p>Enqueue replies with {@link #ok(String)} / {@link #fail(int, String)}, call the
 * resource, then assert on {@link #take()} (method, path, query, headers, body).
 */
public abstract class ApiTestSupport {

    protected MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    /** Options pointed at the mock server, no retries, tiny delays. */
    protected DosyaClientOptions options() {
        return new DosyaClientOptions("dos_test_key")
                .baseUrl("http://localhost:" + server.getPort())
                .maxRetries(0)
                .baseDelay(1)
                .maxDelay(50);
    }

    protected DosyaHttpClient http() {
        return new DosyaHttpClient(options());
    }

    /** A 200 enveloped JSON reply; {@code json} is the payload without {@code ok}, e.g. {@code "\"name\":\"a\""}. */
    protected static MockResponse ok(String json) {
        return new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true" + (json.isEmpty() ? "" : "," + json) + "}");
    }

    protected static MockResponse ok() {
        return ok("");
    }

    protected static MockResponse fail(int status, String error) {
        return new MockResponse().setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":false,\"error\":\"" + error + "\"}");
    }

    protected void enqueue(MockResponse... responses) {
        for (MockResponse r : responses) server.enqueue(r);
    }

    /** The next recorded request (fails the test if none arrives within 2 s). */
    protected RecordedRequest take() throws InterruptedException {
        RecordedRequest r = server.takeRequest(2, TimeUnit.SECONDS);
        if (r == null) throw new AssertionError("No request was sent");
        return r;
    }

    /** The recorded request's JSON body. */
    protected static JsonObject jsonBody(RecordedRequest r) {
        return JsonParser.parseString(r.getBody().readUtf8()).getAsJsonObject();
    }

    /** Path without the query string. */
    protected static String pathOf(RecordedRequest r) {
        String p = r.getPath();
        int q = p.indexOf('?');
        return q < 0 ? p : p.substring(0, q);
    }
}
