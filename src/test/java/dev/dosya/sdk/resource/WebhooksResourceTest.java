package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.CreateWebhookParams;
import dev.dosya.sdk.model.CreatedWebhookEndpoint;
import dev.dosya.sdk.model.UpdateWebhookParams;
import dev.dosya.sdk.model.WebhookDeliveriesResponse;
import dev.dosya.sdk.model.WebhookDelivery;
import dev.dosya.sdk.model.WebhookEndpoint;
import dev.dosya.sdk.model.WebhookEndpointDetail;
import dev.dosya.sdk.testing.ApiTestSupport;
import dev.dosya.sdk.webhook.WebhookEventType;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhooksResourceTest extends ApiTestSupport {

    private static final String ROW = "{\"id\":\"whep_1\",\"workspace_id\":\"ws_1\",\"url\":\"https://hooks.example.com/in\","
            + "\"events\":[\"file.uploaded\",\"future.event\"],\"description\":null,\"active\":1,\"consecutive_failures\":2,"
            + "\"disabled_at\":null,\"created_by\":\"usr_1\",\"created_at\":1700000000,\"updated_at\":1700000001}";

    private DosyaHttpClient retrying() {
        return new DosyaHttpClient(options().maxRetries(3));
    }

    @Test
    void eventTypeWireValues() {
        assertThat(WebhookEventType.FILE_UPLOADED.value()).isEqualTo("file.uploaded");
        assertThat(WebhookEventType.FILE_DELETED.value()).isEqualTo("file.deleted");
        assertThat(WebhookEventType.SHARE_ACCESSED.value()).isEqualTo("share.accessed");
        assertThat(WebhookEventType.values()).hasSize(3);
        assertThat(WebhookEventType.fromValue("share.accessed")).isEqualTo(WebhookEventType.SHARE_ACCESSED);
        assertThat(WebhookEventType.fromValue("nope")).isNull();
    }

    @Test
    void listSendsWorkspaceIdAndParsesRows() throws Exception {
        enqueue(ok("\"webhooks\":[" + ROW + "]"));
        List<WebhookEndpoint> hooks = new WebhooksResource(http()).list("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/webhooks");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        WebhookEndpoint h = hooks.get(0);
        assertThat(h.getId()).isEqualTo("whep_1");
        assertThat(h.getWorkspaceId()).isEqualTo("ws_1");
        assertThat(h.getUrl()).isEqualTo("https://hooks.example.com/in");
        assertThat(h.getEvents()).containsExactly("file.uploaded", "future.event");
        assertThat(h.getEventTypes()).containsExactly(WebhookEventType.FILE_UPLOADED);
        assertThat(h.getDescription()).isNull();
        assertThat(h.isActive()).isTrue();
        assertThat(h.getConsecutiveFailures()).isEqualTo(2);
        assertThat(h.getDisabledAt()).isNull();
        assertThat(h.getCreatedBy()).isEqualTo("usr_1");
        assertThat(h.getCreatedAt()).isEqualTo(1700000000L);
        assertThat(h.getUpdatedAt()).isEqualTo(1700000001L);
        assertThatThrownBy(() -> hooks.add(h)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void createPostsSnakeCaseBodyAndReturnsSecret() throws Exception {
        enqueue(ok("\"webhook\":{\"id\":\"whep_1\",\"workspace_id\":\"ws_1\",\"url\":\"https://h.example/x\","
                + "\"events\":[\"file.uploaded\",\"file.deleted\"],\"description\":\"d\",\"active\":1,"
                + "\"secret\":\"whsec_abc\",\"created_at\":1700000000}").setResponseCode(201));

        CreatedWebhookEndpoint hook = new WebhooksResource(http()).create(
                new CreateWebhookParams("ws_1", "https://h.example/x",
                        WebhookEventType.FILE_UPLOADED, WebhookEventType.FILE_DELETED).description("d"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/webhooks");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("url").getAsString()).isEqualTo("https://h.example/x");
        assertThat(body.getAsJsonArray("events").toString()).isEqualTo("[\"file.uploaded\",\"file.deleted\"]");
        assertThat(body.get("description").getAsString()).isEqualTo("d");
        assertThat(body.size()).isEqualTo(4);
        assertThat(hook.getSecret()).isEqualTo("whsec_abc");
        assertThat(hook.getWorkspaceId()).isEqualTo("ws_1");
        assertThat(hook.isActive()).isTrue();
        assertThat(hook.getCreatedAt()).isEqualTo(1700000000L);
        assertThat(hook.toString()).doesNotContain("whsec_abc");
    }

    @Test
    void createOmitsDescriptionAndIsNotRetriedOn5xx() throws Exception {
        enqueue(fail(500, "boom"), ok("\"webhook\":{}"));
        assertThatThrownBy(() -> new WebhooksResource(retrying()).create(
                new CreateWebhookParams("ws_1", "https://h.example/x", WebhookEventType.FILE_DELETED)))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
        JsonObject body = jsonBody(take());
        assertThat(body.has("description")).isFalse();
        assertThat(body.getAsJsonArray("events").toString()).isEqualTo("[\"file.deleted\"]");
    }

    @Test
    void getEncodesIdAndReadsHealthFields() throws Exception {
        enqueue(ok("\"webhook\":{\"id\":\"whep/1\",\"workspace_id\":\"ws_1\",\"url\":\"https://x\",\"events\":[\"share.accessed\"],"
                + "\"description\":\"hi\",\"active\":0,\"consecutive_failures\":15,\"disabled_at\":1700000100,"
                + "\"first_failure_at\":1700000050,\"health_notified\":2,\"created_by\":\"usr_1\","
                + "\"created_at\":1700000000,\"updated_at\":1700000100}"));

        WebhookEndpointDetail d = new WebhooksResource(http()).get("whep/1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(r.getPath()).isEqualTo("/api/webhooks/whep%2F1");
        assertThat(d.isActive()).isFalse();
        assertThat(d.getConsecutiveFailures()).isEqualTo(15);
        assertThat(d.getDisabledAt()).isEqualTo(1700000100L);
        assertThat(d.getFirstFailureAt()).isEqualTo(1700000050L);
        assertThat(d.getHealthNotified()).isEqualTo(2);
        assertThat(d.getEventTypes()).containsExactly(WebhookEventType.SHARE_ACCESSED);
        assertThat(d.getDescription()).isEqualTo("hi");
    }

    @Test
    void getNotFound() {
        enqueue(fail(404, "Webhook not found"));
        assertThatThrownBy(() -> new WebhooksResource(http()).get("whep_x"))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(e -> assertThat(((DosyaApiException) e).getStatus()).isEqualTo(404));
    }

    @Test
    void updatePatchesOnlyGivenFields() throws Exception {
        enqueue(ok());
        new WebhooksResource(http()).update("whep_1", new UpdateWebhookParams().active(true));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PATCH");
        assertThat(pathOf(r)).isEqualTo("/api/webhooks/whep_1");
        JsonObject body = jsonBody(r);
        assertThat(body.size()).isEqualTo(1);
        assertThat(body.get("active").getAsBoolean()).isTrue();
    }

    @Test
    void updateSendsExplicitNullDescriptionAndAllFields() throws Exception {
        enqueue(ok());
        new WebhooksResource(http()).update("whep_1", new UpdateWebhookParams()
                .url("https://new.example/x")
                .events(WebhookEventType.FILE_DELETED)
                .active(false)
                .description(null));
        JsonObject body = jsonBody(take());
        assertThat(body.get("url").getAsString()).isEqualTo("https://new.example/x");
        assertThat(body.getAsJsonArray("events").toString()).isEqualTo("[\"file.deleted\"]");
        assertThat(body.get("active").getAsBoolean()).isFalse();
        assertThat(body.has("description")).isTrue();
        assertThat(body.get("description").isJsonNull()).isTrue();
    }

    @Test
    void deleteIsNeverRetried() throws Exception {
        enqueue(fail(503, "unavailable"), ok());
        assertThatThrownBy(() -> new WebhooksResource(retrying()).delete("whep 1"))
                .isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(r.getPath()).isEqualTo("/api/webhooks/whep%201");
    }

    @Test
    void rollSecretReturnsNewSecret() throws Exception {
        enqueue(ok("\"secret\":\"whsec_new\""));
        assertThat(new WebhooksResource(http()).rollSecret("whep_1")).isEqualTo("whsec_new");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/webhooks/whep_1/roll-secret");
    }

    @Test
    void testReturnsDeliveryId() throws Exception {
        enqueue(ok("\"delivery_id\":\"whd_1\""));
        assertThat(new WebhooksResource(http()).test("whep_1")).isEqualTo("whd_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/webhooks/whep_1/test");
    }

    @Test
    void listDeliveriesSendsPagingAndParsesRows() throws Exception {
        enqueue(ok("\"deliveries\":[{\"id\":\"whd_1\",\"event_id\":\"evt_1\",\"event_type\":\"file.uploaded\","
                + "\"status\":\"failed\",\"attempts\":3,\"next_attempt_at\":1700000300,\"last_attempt_at\":1700000200,"
                + "\"response_status\":500,\"response_snippet\":\"oops\",\"error\":null,\"created_at\":1700000000}],"
                + "\"pagination\":{\"page\":2,\"per_page\":10,\"total\":11,\"total_pages\":2}"));

        WebhookDeliveriesResponse res = new WebhooksResource(http()).listDeliveries("whep_1", 2, 10);

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/webhooks/whep_1/deliveries");
        assertThat(r.getRequestUrl().queryParameter("page")).isEqualTo("2");
        assertThat(r.getRequestUrl().queryParameter("per_page")).isEqualTo("10");
        WebhookDelivery d = res.getDeliveries().get(0);
        assertThat(d.getId()).isEqualTo("whd_1");
        assertThat(d.getEventId()).isEqualTo("evt_1");
        assertThat(d.getEventType()).isEqualTo("file.uploaded");
        assertThat(d.getEventTypeValue()).isEqualTo(WebhookEventType.FILE_UPLOADED);
        assertThat(d.getStatus()).isEqualTo(WebhookDelivery.Status.FAILED);
        assertThat(d.getAttempts()).isEqualTo(3);
        assertThat(d.getNextAttemptAt()).isEqualTo(1700000300L);
        assertThat(d.getLastAttemptAt()).isEqualTo(1700000200L);
        assertThat(d.getResponseStatus()).isEqualTo(500);
        assertThat(d.getResponseSnippet()).isEqualTo("oops");
        assertThat(d.getError()).isNull();
        assertThat(res.getPagination().getPage()).isEqualTo(2);
        assertThat(res.getPagination().getPerPage()).isEqualTo(10);
        assertThat(res.getPagination().getTotal()).isEqualTo(11);
        assertThat(res.getPagination().getTotalPages()).isEqualTo(2);
    }

    @Test
    void listDeliveriesWithoutPagingSendsNoQuery() throws Exception {
        enqueue(ok("\"deliveries\":[],\"pagination\":{\"page\":1,\"per_page\":25,\"total\":0,\"total_pages\":1}"));
        WebhookDeliveriesResponse res = new WebhooksResource(http()).listDeliveries("whep_1");
        RecordedRequest r = take();
        assertThat(r.getRequestUrl().querySize()).isZero();
        assertThat(res.getDeliveries()).isEmpty();
    }

    @Test
    void redeliverEncodesBothIds() throws Exception {
        enqueue(ok("\"delivery_id\":\"whd_2\""));
        assertThat(new WebhooksResource(http()).redeliver("whep_1", "whd/1")).isEqualTo("whd_2");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(r.getPath()).isEqualTo("/api/webhooks/whep_1/deliveries/whd%2F1/redeliver");
    }
}
