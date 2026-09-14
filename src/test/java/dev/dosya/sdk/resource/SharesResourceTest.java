package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.model.*;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SharesResourceTest extends ApiTestSupport {

    private SharesResource shares() {
        return new SharesResource(http());
    }

    @Test
    void listUsesWorkspaceShareLinkShape() throws Exception {
        enqueue(ok("\"links\":[{\"link_id\":\"sl_1\",\"token\":\"t\",\"expires_at\":null,\"view_count\":4,\"download_count\":1,"
                + "\"is_revoked\":0,\"revoked_at\":null,\"shared_at\":100,\"created_by\":\"u1\",\"folder_id\":null,"
                + "\"lock_mode\":\"view_only\",\"is_bundle\":0,\"is_password_protected\":1,\"access_mode\":\"restricted\","
                + "\"max_downloads\":null,\"recipient_count\":2,\"file_id\":\"f1\",\"file_name\":\"a.pdf\",\"size_bytes\":10,"
                + "\"extension\":\".pdf\",\"region\":\"eu\",\"folder_name\":null,\"sharer_name\":\"Ana\",\"status\":\"expiring\","
                + "\"is_folder\":false,\"display_name\":\"a.pdf\",\"url\":\"https://dosya.dev/s/t\",\"is_mine\":true}],"
                + "\"stats\":{\"total\":1,\"active\":0,\"expiring\":1,\"total_views\":4}"));

        SharesListResponse res = shares().list("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/shares");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        WorkspaceShareLink l = res.getLinks().get(0);
        assertThat(l.getLinkId()).isEqualTo("sl_1");
        assertThat(l.getSharedAt()).isEqualTo(100L);
        assertThat(l.getStatus()).isEqualTo(ShareStatus.EXPIRING);
        assertThat(l.getLockMode()).isEqualTo(LockMode.VIEW_ONLY);
        assertThat(l.getAccessMode()).isEqualTo(ShareAccessMode.RESTRICTED);
        assertThat(l.isPasswordProtected()).isTrue();
        assertThat(l.isRevoked()).isFalse();
        assertThat(l.isMine()).isTrue();
        assertThat(l.getMaxDownloads()).isNull();
        assertThat(l.getRecipientCount()).isEqualTo(2);
        assertThat(res.getStats().getTotalViews()).isEqualTo(4);
    }

    @Test
    void updateIsPatchWithOnlySetFields() throws Exception {
        enqueue(ok("\"link\":{\"link_id\":\"sl_1\",\"expires_at\":500,\"is_password_protected\":0,\"lock_mode\":\"none\","
                + "\"access_mode\":\"restricted\",\"recipient_count\":2,\"download_count\":0,\"max_downloads\":null}"));

        UpdatedShareLink link = shares().update("sl/1", new UpdateShareLinkParams()
                .expiresInDays(7)
                .clearPassword()
                .accessMode(ShareAccessMode.RESTRICTED)
                .recipientEmails(Arrays.asList("a@x.y", "b@x.y"))
                .maxDownloads(null));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PATCH");
        assertThat(pathOf(r)).isEqualTo("/api/shares/sl%2F1");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("expires_in_days", "password", "access_mode",
                "recipient_emails", "max_downloads");
        assertThat(body.get("expires_in_days").getAsInt()).isEqualTo(7);
        assertThat(body.get("password").isJsonNull()).isTrue();
        assertThat(body.get("access_mode").getAsString()).isEqualTo("restricted");
        assertThat(body.getAsJsonArray("recipient_emails")).hasSize(2);
        assertThat(body.get("max_downloads").isJsonNull()).isTrue();
        assertThat(link.getExpiresAt()).isEqualTo(500L);
        assertThat(link.isPasswordProtected()).isFalse();
        assertThat(link.getAccessMode()).isEqualTo(ShareAccessMode.RESTRICTED);
    }

    @Test
    void updateSendsLockModeWireValue() throws Exception {
        enqueue(ok("\"link\":{\"link_id\":\"sl_1\",\"lock_mode\":\"full_lock\"}"));
        shares().update("sl_1", new UpdateShareLinkParams().lockMode(LockMode.FULL_LOCK).password("longenough"));
        JsonObject body = jsonBody(take());
        assertThat(body.get("lock_mode").getAsString()).isEqualTo("full_lock");
        assertThat(body.get("password").getAsString()).isEqualTo("longenough");
    }

    @Test
    void analyticsSendsRangeAndOffset() throws Exception {
        enqueue(ok("\"link\":{\"link_id\":\"sl_1\",\"url\":\"u\",\"display_name\":\"a\",\"is_folder\":false,\"is_bundle\":false,"
                + "\"created_at\":1,\"created_by\":\"u1\",\"is_mine\":true,\"is_revoked\":false,\"is_password_protected\":false,"
                + "\"lock_mode\":\"none\",\"access_mode\":\"public\",\"status\":\"active\",\"view_count\":3,\"download_count\":1,"
                + "\"max_downloads\":10,\"downloads_left\":9},\"range\":7,"
                + "\"timeline\":[{\"day\":\"2026-09-01\",\"opens\":2,\"downloads\":1}],"
                + "\"reach\":{\"visitors\":2,\"truncated\":false,\"devices\":[{\"label\":\"Desktop\",\"count\":2}],\"browsers\":[]},"
                + "\"recipients\":null,"
                + "\"log\":{\"total\":30,\"offset\":25,\"limit\":25,\"rows\":[{\"id\":\"v1\",\"visitor\":\"abcd1234\",\"event\":\"download\","
                + "\"device\":\"desktop\",\"device_label\":\"Chrome\",\"country\":\"AU\",\"viewed_at\":9}]},"
                + "\"gaps\":{\"repeat_visits\":\"x\",\"per_recipient\":null}"));

        ShareAnalytics a = shares().analytics("sl_1", 7, 25);

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/shares/sl_1/analytics");
        assertThat(r.getRequestUrl().queryParameter("range")).isEqualTo("7");
        assertThat(r.getRequestUrl().queryParameter("offset")).isEqualTo("25");
        assertThat(a.getRange()).isEqualTo(7);
        assertThat(a.getLink().getDownloadsLeft()).isEqualTo(9);
        assertThat(a.getLink().getStatus()).isEqualTo(ShareStatus.ACTIVE);
        assertThat(a.getTimeline().get(0).getOpens()).isEqualTo(2);
        assertThat(a.getReach().getDevices().get(0).getLabel()).isEqualTo("Desktop");
        assertThat(a.getRecipients()).isNull();
        assertThat(a.getLog().getRows().get(0).getDeviceLabel()).isEqualTo("Chrome");
        assertThat(a.getGaps().getPerRecipient()).isNull();
    }

    @Test
    void analyticsWithoutOptionsSendsNoQuery() throws Exception {
        enqueue(ok("\"link\":{\"link_id\":\"sl_1\"},\"range\":30"));
        shares().analytics("sl_1");
        RecordedRequest r = take();
        assertThat(r.getRequestUrl().querySize()).isZero();
    }

    @Test
    void revoke() throws Exception {
        enqueue(ok());
        shares().revoke("sl_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/shares/sl_1/revoke");
    }

    @Test
    void withMe() throws Exception {
        enqueue(ok("\"links\":[{\"link_id\":\"sl_2\",\"url\":\"u\",\"display_name\":\"b\",\"is_folder\":true,\"is_bundle\":false,"
                + "\"size_bytes\":null,\"extension\":null,\"is_password_protected\":0,\"lock_mode\":\"none\",\"expires_at\":null,"
                + "\"revoked_at\":null,\"status\":\"active\",\"shared_at\":1,\"invited_at\":2,\"verified_at\":null,\"sender_name\":\"Bo\"}],"
                + "\"email_verified\":true,\"stats\":{\"total\":1,\"active\":1,\"unopened\":1}"));
        SharedWithMeResponse res = shares().withMe();
        assertThat(pathOf(take())).isEqualTo("/api/shares/with-me");
        assertThat(res.isEmailVerified()).isTrue();
        assertThat(res.getLinks().get(0).isFolder()).isTrue();
        assertThat(res.getLinks().get(0).getVerifiedAt()).isNull();
        assertThat(res.getStats().getUnopened()).isEqualTo(1);
    }

    @Test
    void withMeUnverifiedHasNoStats() throws Exception {
        enqueue(ok("\"links\":[],\"email_verified\":false"));
        SharedWithMeResponse res = shares().withMe();
        assertThat(res.isEmailVerified()).isFalse();
        assertThat(res.getLinks()).isEmpty();
        assertThat(res.getStats()).isNull();
    }
}
