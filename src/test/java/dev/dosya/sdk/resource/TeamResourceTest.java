package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.*;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamResourceTest extends ApiTestSupport {

    private TeamResource team() {
        return new TeamResource(http());
    }

    @Test
    void listParsesMembersInvitesActivityAndStats() throws Exception {
        enqueue(ok("\"workspace\":{\"name\":\"A\",\"icon_initials\":\"A\",\"icon_color\":\"#000\"},"
                + "\"members\":[{\"membership_id\":\"wm_1\",\"user_id\":\"u1\",\"role_id\":\"role_owner\",\"joined_at\":1,"
                + "\"root_folder_id\":null,\"root_folder_name\":null,\"name\":\"Ana\",\"email\":\"a@x.y\",\"avatar_url\":null,"
                + "\"last_active_at\":5,\"is_you\":true}],"
                + "\"invites\":[{\"id\":\"inv_1\",\"email\":\"b@x.y\",\"role_id\":\"role_member\",\"created_at\":2,\"expires_at\":3,"
                + "\"invited_by_name\":\"Ana\",\"invite_url\":\"https://dosya.dev/invite/t\"}],"
                + "\"activity\":[{\"id\":\"ae_1\",\"action\":\"member_invited\",\"metadata\":\"{\\\"file_name\\\":\\\"x\\\"}\","
                + "\"meta\":{\"file_name\":\"x\"},\"created_at\":4,\"user_name\":\"Ana\",\"user_id\":\"u1\",\"avatar_url\":null}],"
                + "\"stats\":{\"members\":1,\"pending\":1,\"shares_this_week\":7}"));

        TeamListResponse res = team().list("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/team");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        TeamMember m = res.getMembers().get(0);
        assertThat(m.getMembershipId()).isEqualTo("wm_1");
        assertThat(m.isYou()).isTrue();
        assertThat(m.getLastActiveAt()).isEqualTo(5L);
        assertThat(res.getInvites().get(0).getInviteUrl()).endsWith("/invite/t");
        TeamActivityItem a = res.getActivity().get(0);
        assertThat(a.getMeta().getAsJsonObject().get("file_name").getAsString()).isEqualTo("x");
        assertThat(res.getStats().getSharesThisWeek()).isEqualTo(7);
        assertThat(res.getWorkspace().getName()).isEqualTo("A");
    }

    @Test
    void inviteSendsRoleAndReturnsId() throws Exception {
        enqueue(ok("\"invite_id\":\"inv_9\""));
        String id = team().invite(new InviteMemberParams("ws_1", "c@x.y").role("Viewer"));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/team/invite");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("email").getAsString()).isEqualTo("c@x.y");
        assertThat(body.get("role").getAsString()).isEqualTo("Viewer");
        assertThat(id).isEqualTo("inv_9");
    }

    @Test
    void inviteOmitsRoleWhenUnset() throws Exception {
        enqueue(ok("\"invite_id\":\"inv_9\""));
        team().invite(new InviteMemberParams("ws_1", "c@x.y"));
        assertThat(jsonBody(take()).has("role")).isFalse();
    }

    @Test
    void resendAndRevokeInvite() throws Exception {
        enqueue(ok(), ok());
        team().resendInvite("inv/1");
        team().revokeInvite("inv_1");
        RecordedRequest resend = take();
        assertThat(resend.getMethod()).isEqualTo("POST");
        assertThat(pathOf(resend)).isEqualTo("/api/team/invites/inv%2F1/resend");
        assertThat(pathOf(take())).isEqualTo("/api/team/invites/inv_1/revoke");
    }

    @Test
    void updateMemberSendsOnlySetFieldsAndClearsFolderWithNull() throws Exception {
        enqueue(ok("\"root_folder_id\":null,\"role_id\":\"role_viewer\""));
        MemberUpdateResult res = team().updateMember("wm_1", new UpdateMemberParams().roleId("role_viewer").clearRootFolder());
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/team/members/wm_1");
        JsonObject body = jsonBody(r);
        assertThat(body.get("role_id").getAsString()).isEqualTo("role_viewer");
        assertThat(body.get("root_folder_id").isJsonNull()).isTrue();
        assertThat(res.getRoleId()).isEqualTo("role_viewer");
        assertThat(res.getRootFolderId()).isNull();
    }

    @Test
    void updateMemberRoleOnlyLeavesFolderOut() throws Exception {
        enqueue(ok("\"root_folder_id\":\"fld_1\",\"role_id\":\"role_admin\""));
        team().updateMember("wm_1", new UpdateMemberParams().roleId("role_admin"));
        assertThat(jsonBody(take()).has("root_folder_id")).isFalse();
    }

    @Test
    void removeMemberIsNeverRetried() throws Exception {
        enqueue(fail(503, "down").setHeader("Retry-After", "0"), ok());
        TeamResource retrying = new TeamResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.removeMember("wm_1")).isInstanceOf(DosyaApiException.class);
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/team/members/wm_1");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void listInviteLinks() throws Exception {
        enqueue(ok("\"links\":[{\"id\":\"il_1\",\"token\":\"t\",\"role_id\":\"role_member\",\"role_name\":\"Member\","
                + "\"max_uses\":null,\"use_count\":2,\"expires_at\":null,\"is_revoked\":0,\"created_at\":1,"
                + "\"created_by_name\":null,\"url\":\"https://dosya.dev/join/t\"}]"));
        List<InviteLink> links = team().listInviteLinks("ws_1");
        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/team/invite-link");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getMaxUses()).isNull();
        assertThat(links.get(0).isRevoked()).isFalse();
        assertThat(links.get(0).getUseCount()).isEqualTo(2);
    }

    @Test
    void createInviteLink() throws Exception {
        enqueue(ok("\"link\":{\"id\":\"il_2\",\"token\":\"t2\",\"url\":\"u\",\"role_id\":\"role_viewer\","
                + "\"role_name\":\"Viewer\",\"max_uses\":5,\"expires_at\":99}"));
        CreatedInviteLink link = team().createInviteLink(new CreateInviteLinkParams("ws_1").role("Viewer").maxUses(5).expiresInDays(3));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/team/invite-link");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("role").getAsString()).isEqualTo("Viewer");
        assertThat(body.get("max_uses").getAsInt()).isEqualTo(5);
        assertThat(body.get("expires_in_days").getAsInt()).isEqualTo(3);
        assertThat(link.getMaxUses()).isEqualTo(5);
        assertThat(link.getExpiresAt()).isEqualTo(99L);
    }

    @Test
    void revokeInviteLinkSendsIdInQuery() throws Exception {
        enqueue(ok());
        team().revokeInviteLink("il_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/team/invite-link");
        assertThat(r.getRequestUrl().queryParameter("id")).isEqualTo("il_1");
    }

    @Test
    void revokeInviteLinkRejectsEmptyId() {
        assertThatThrownBy(() -> team().revokeInviteLink("")).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }
}
