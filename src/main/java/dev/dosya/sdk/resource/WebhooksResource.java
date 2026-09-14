package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateWebhookParams;
import dev.dosya.sdk.model.CreatedWebhookEndpoint;
import dev.dosya.sdk.model.UpdateWebhookParams;
import dev.dosya.sdk.model.WebhookDeliveriesResponse;
import dev.dosya.sdk.model.WebhookEndpoint;
import dev.dosya.sdk.model.WebhookEndpointDetail;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Outbound webhook endpoints.
 *
 * <p>Every method (GETs included) needs a {@code full} scope key and a role with the
 * {@code manage_settings} permission in the endpoint's workspace. Workspace-pinned keys
 * can only call {@link #list(String)}; every other method answers 403 for them.
 *
 * <p>Verify deliveries on your server with {@link dev.dosya.sdk.webhook.WebhookSignature}.
 *
 * @since 0.3.0
 */
public final class WebhooksResource {

    private static final Type ENDPOINT_LIST = new TypeToken<List<WebhookEndpoint>>() {}.getType();

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code WebhooksResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public WebhooksResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists a workspace's endpoints, newest first (max 16, not paginated). Secrets are never included.
     *
     * @param workspaceId the workspace
     * @return the endpoints
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<WebhookEndpoint> list(@NotNull String workspaceId) {
        JsonObject resp = http.request(HttpRequest.get("/api/webhooks").query("workspace_id", workspaceId));
        List<WebhookEndpoint> list = http.fromJson(resp.get("webhooks"), ENDPOINT_LIST);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * Registers an endpoint. The secret in the result is shown only here - store it.
     * Limits: 16 endpoints per workspace (400); free plan 3 endpoints in total across all
     * workspaces (403). The URL must be public {@code https://} (400 otherwise).
     * Not retried on 5xx or network errors (a replay would create a second endpoint).
     *
     * @param params workspace, URL, events and optional description
     * @return the created endpoint including its {@code whsec_...} secret
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CreatedWebhookEndpoint create(@NotNull CreateWebhookParams params) {
        JsonObject resp = http.request(HttpRequest.post("/api/webhooks").body(params.toBody()));
        return http.fromJson(resp.get("webhook"), CreatedWebhookEndpoint.class);
    }

    /**
     * Gets one endpoint, including failure-health fields. 404 {@code Webhook not found}.
     *
     * @param id the endpoint id ({@code whep_...})
     * @return the endpoint
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull WebhookEndpointDetail get(@NotNull String id) {
        JsonObject resp = http.request(HttpRequest.get("/api/webhooks/" + seg(id)));
        return http.fromJson(resp.get("webhook"), WebhookEndpointDetail.class);
    }

    /**
     * Changes an endpoint (PATCH). Only the fields set on {@code params} are updated.
     * {@code active(true)} re-enables an auto-disabled endpoint and resets its failure counter;
     * {@code description(null)} clears the description. Returns nothing; call
     * {@link #get(String)} for the new state.
     *
     * @param id     the endpoint id
     * @param params the changes
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void update(@NotNull String id, @NotNull UpdateWebhookParams params) {
        http.request(HttpRequest.patch("/api/webhooks/" + seg(id)).body(params.toBody()));
    }

    /**
     * Deletes an endpoint and its whole delivery log. Not retried.
     *
     * @param id the endpoint id
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public void delete(@NotNull String id) {
        http.request(HttpRequest.delete("/api/webhooks/" + seg(id)).retry(HttpRequest.Retry.NEVER));
    }

    /**
     * Replaces the signing secret and returns the new one (shown only here). The old secret
     * stops verifying IMMEDIATELY - there is no grace period, so deliveries fail until your
     * receiver has the new value. Rate limited to 10 per hour per user.
     *
     * @param id the endpoint id
     * @return the new {@code whsec_...} secret
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String rollSecret(@NotNull String id) {
        JsonObject resp = http.request(HttpRequest.post("/api/webhooks/" + seg(id) + "/roll-secret"));
        return resp.get("secret").getAsString();
    }

    /**
     * Sends a synthetic {@code file.uploaded} event ({@code data.test: true}) to the endpoint now,
     * even if it is inactive or not subscribed to that event. Check the outcome with
     * {@link #listDeliveries(String)}. Rate limited to 20 per 5 minutes per user.
     *
     * @param id the endpoint id
     * @return the new delivery id ({@code whd_...})
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String test(@NotNull String id) {
        JsonObject resp = http.request(HttpRequest.post("/api/webhooks/" + seg(id) + "/test"));
        return resp.get("delivery_id").getAsString();
    }

    /**
     * Returns the first page of an endpoint's delivery log (25 rows), newest first.
     *
     * @param id the endpoint id
     * @return deliveries and pagination
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull WebhookDeliveriesResponse listDeliveries(@NotNull String id) {
        return listDeliveries(id, null, null);
    }

    /**
     * Pages through an endpoint's delivery log, newest first.
     *
     * @param id      the endpoint id
     * @param page    1-based page, or {@code null} for 1
     * @param perPage page size, clamped by the API to 10..100; {@code null} for 25
     * @return deliveries and pagination
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull WebhookDeliveriesResponse listDeliveries(@NotNull String id, @Nullable Integer page,
                                                             @Nullable Integer perPage) {
        return http.requestAs(
                HttpRequest.get("/api/webhooks/" + seg(id) + "/deliveries")
                        .query("page", page)
                        .query("per_page", perPage),
                WebhookDeliveriesResponse.class);
    }

    /**
     * Re-sends a past delivery's exact payload as a new delivery (new delivery id, same event id).
     * 404 {@code Delivery not found}. Rate limited to 20 per 5 minutes per user.
     *
     * @param id         the endpoint id
     * @param deliveryId the delivery to repeat ({@code whd_...})
     * @return the new delivery id
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String redeliver(@NotNull String id, @NotNull String deliveryId) {
        JsonObject resp = http.request(HttpRequest.post(
                "/api/webhooks/" + seg(id) + "/deliveries/" + seg(deliveryId) + "/redeliver"));
        return resp.get("delivery_id").getAsString();
    }
}
