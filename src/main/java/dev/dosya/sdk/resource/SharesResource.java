package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ShareAnalytics;
import dev.dosya.sdk.model.SharedWithMeResponse;
import dev.dosya.sdk.model.SharesListResponse;
import dev.dosya.sdk.model.UpdateShareLinkParams;
import dev.dosya.sdk.model.UpdatedShareLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Share links across a workspace. Create links with {@code files().createShareLink()} /
 * {@code folders().createShareLink()}.
 *
 * <p>Key scope: GET needs {@code read} or {@code full}, the rest {@code full}; {@code upload}
 * keys cannot call these. Workspace-pinned keys can only call {@link #list(String)} (for their
 * own workspace); the by-link-id methods and {@link #withMe()} are refused for them (403).
 *
 * @since 0.1.0
 */
public final class SharesResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code SharesResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public SharesResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Up to 100 links in a workspace, newest first, with totals. Needs {@code access_shared}
     * plus {@code view_all_shares} (or {@code view_own_shares}, which lists only the caller's
     * links).
     */
    public @NotNull SharesListResponse list(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/shares").query("workspace_id", workspaceId),
                SharesListResponse.class);
    }

    /**
     * Changes a live link in place; its URL keeps working. Only the fields set change, and
     * {@code recipientEmails} replaces the whole list.
     *
     * <p>The creator may edit; anyone else needs {@code view_all_shares}. 409 when revoked.
     * Invalid values (empty patch, short password, bad expiry, download cap or recipients) are
     * a 400 whose error message explains the problem. A new expiry on a link whose creator is on
     * the free plan is clamped to 7 days.
     * dosya.dev recipient addresses are refused unless the caller is a dosya.dev account.
     *
     * @since 0.3.0
     */
    public @NotNull UpdatedShareLink update(@NotNull String linkId, @NotNull UpdateShareLinkParams params) {
        JsonObject resp = http.request(HttpRequest.patch("/api/shares/" + seg(linkId)).body(params.toBody()));
        return http.fromJson(resp.get("link"), UpdatedShareLink.class);
    }

    /**
     * Per-link analytics over the last 30 days, first page of the access log.
     *
     * @see #analytics(String, Integer, Integer)
     * @since 0.3.0
     */
    public @NotNull ShareAnalytics analytics(@NotNull String linkId) {
        return analytics(linkId, null, null);
    }

    /**
     * Per-link analytics: daily opens and downloads, device and browser reach, recipients
     * (restricted links) and a 25-row access log page. Needs {@code view_all_shares}, or
     * {@code view_own_shares} for the caller's own links.
     *
     * @param range  days: 7, 30 or 90 (400 otherwise); null for 30
     * @param offset offset into the access log; null for 0
     * @since 0.3.0
     */
    public @NotNull ShareAnalytics analytics(@NotNull String linkId, @Nullable Integer range, @Nullable Integer offset) {
        return http.requestAs(HttpRequest.get("/api/shares/" + seg(linkId) + "/analytics")
                .query("range", range)
                .query("offset", offset), ShareAnalytics.class);
    }

    /**
     * Revokes a link permanently. The creator may revoke; anyone else needs
     * {@code view_all_shares}. 409 when already revoked.
     */
    public void revoke(@NotNull String linkId) {
        http.request(HttpRequest.post("/api/shares/" + seg(linkId) + "/revoke"));
    }

    /**
     * Links other people shared with the caller's verified email address (up to 100).
     *
     * @since 0.3.0
     */
    public @NotNull SharedWithMeResponse withMe() {
        return http.requestAs(HttpRequest.get("/api/shares/with-me"), SharedWithMeResponse.class);
    }
}
