package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.*;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspacesResourceTest extends ApiTestSupport {

    private WorkspacesResource ws() {
        return new WorkspacesResource(http());
    }

    private static final String STORAGE = "\"storage\":{\"used\":10,\"total\":1000,\"free\":900,\"cap_bytes\":null,"
            + "\"account_limit_bytes\":2000,\"account_used_bytes\":1100}";

    @Test
    void listParsesStorageAllocationAnd2fa() throws Exception {
        enqueue(ok("\"workspaces\":[{\"id\":\"ws_1\",\"name\":\"A\",\"slug\":\"a\",\"icon_initials\":\"A\","
                + "\"icon_color\":\"#000\",\"icon_image_url\":null,\"owner_id\":\"u1\",\"default_region\":\"ap-southeast-2\","
                + "\"plan\":\"free\",\"created_at\":5,\"storage_used_bytes\":null,\"role_id\":\"role_owner\",\"joined_at\":6,"
                + "\"require_2fa\":1,\"disable_password_login\":0,\"max_total_storage_gb\":25," + STORAGE + "}],"
                + "\"allocation\":{\"plan_gb\":50,\"allocated_gb\":25,\"remaining_gb\":25},"
                + "\"user_email\":\"a@b.c\",\"user_has_2fa\":true,\"user_login_method\":\"password\""));

        WorkspaceListResponse res = ws().list();

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces");
        WorkspaceListItem item = res.getWorkspaces().get(0);
        assertThat(item.isRequire2fa()).isTrue();
        assertThat(item.isDisablePasswordLogin()).isFalse();
        assertThat(item.getDefaultRegion()).isEqualTo("ap-southeast-2");
        assertThat(item.getStorageUsedBytes()).isZero();
        assertThat(item.getMaxTotalStorageGb()).isEqualTo(25.0);
        assertThat(item.getJoinedAt()).isEqualTo(6);
        assertThat(item.getStorage().getFree()).isEqualTo(900);
        assertThat(item.getStorage().getCapBytes()).isNull();
        assertThat(res.getAllocation().getRemainingGb()).isEqualTo(25);
        assertThat(res.isUserHas2fa()).isTrue();
        assertThat(res.getUserLoginMethod()).isEqualTo("password");
    }

    @Test
    void getEncodesIdAndParsesSettingsAndStorage() throws Exception {
        enqueue(ok("\"workspace\":{\"id\":\"ws/1\",\"name\":\"A\",\"slug\":\"a\",\"owner_id\":\"u1\",\"icon_image_url\":\"k\","
                + "\"default_region\":\"eu\",\"storage_used_bytes\":7,\"created_at\":1},"
                + "\"settings\":{\"workspace_id\":\"ws/1\",\"max_file_size_gb\":2,\"require_2fa\":1,\"ip_allowlist\":\"[\\\"1.2.3.4/32\\\"]\","
                + "\"download_rate_limit\":null,\"duplicate_scan_enabled\":1,\"updated_at\":9},"
                + "\"role_id\":\"role_admin\",\"is_owner\":false,\"plan\":\"pro\"," + STORAGE + ","
                + "\"plan_limits\":{\"storage_gb\":600},\"allocation\":{\"plan_gb\":600,\"allocated_gb\":0,\"remaining_gb\":600}"));

        WorkspaceGetResponse res = ws().get("ws/1");

        assertThat(pathOf(take())).isEqualTo("/api/workspaces/ws%2F1");
        assertThat(res.getWorkspace().getIconImageUrl()).isEqualTo("k");
        assertThat(res.getWorkspace().getDefaultRegion()).isEqualTo("eu");
        assertThat(res.getSettings().isRequire2fa()).isTrue();
        assertThat(res.getSettings().getMaxFileSizeGb()).isEqualTo(2.0);
        assertThat(res.getSettings().getIpAllowlist()).isEqualTo("[\"1.2.3.4/32\"]");
        assertThat(res.getSettings().getDownloadRateLimit()).isNull();
        assertThat(res.getSettings().isDuplicateScanEnabled()).isTrue();
        assertThat(res.isOwner()).isFalse();
        assertThat(res.getPlan()).isEqualTo("pro");
        assertThat(res.getPlanLimits().getStorageGb()).isEqualTo(600);
        assertThat(res.getStorage().getAccountUsedBytes()).isEqualTo(1100);
    }

    @Test
    void getAllowsNullSettings() throws Exception {
        enqueue(ok("\"workspace\":{\"id\":\"ws_1\",\"name\":\"A\",\"slug\":\"a\",\"owner_id\":\"u1\"},\"settings\":null,"
                + "\"role_id\":\"role_owner\",\"is_owner\":true"));
        assertThat(ws().get("ws_1").getSettings()).isNull();
    }

    @Test
    void createSendsCapAndRegion() throws Exception {
        enqueue(ok("\"workspace\":{\"id\":\"ws_2\",\"slug\":\"b\",\"name\":\"B\",\"icon_initials\":\"B\",\"icon_color\":\"#fff\"}"));

        CreatedWorkspace created = ws().create(new CreateWorkspaceParams("B")
                .iconInitials("B").defaultRegion("ap-southeast-2").maxTotalStorageGb(10));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        JsonObject body = jsonBody(r);
        assertThat(body.get("name").getAsString()).isEqualTo("B");
        assertThat(body.get("icon_initials").getAsString()).isEqualTo("B");
        assertThat(body.get("default_region").getAsString()).isEqualTo("ap-southeast-2");
        assertThat(body.get("max_total_storage_gb").getAsInt()).isEqualTo(10);
        assertThat(body.has("icon_color")).isFalse();
        assertThat(created.getId()).isEqualTo("ws_2");
        assertThat(created.getIconColor()).isEqualTo("#fff");
    }

    @Test
    void updateNeverSendsDefaultRegion() throws Exception {
        enqueue(ok());
        ws().update("ws_1", new UpdateWorkspaceParams().name("New").iconColor("#111"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws_1");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("name", "icon_color");
    }

    @Test
    void getSettingsReturnsShareDefaultsOrNull() throws Exception {
        enqueue(ok("\"settings\":{\"default_share_expiry_days\":7,\"share_max_expiry_days\":null,"
                + "\"disable_share_links\":0,\"force_share_password\":1}"), ok("\"settings\":null"));

        WorkspaceShareSettings s = ws().getSettings("ws_1");
        assertThat(pathOf(take())).isEqualTo("/api/workspaces/ws_1/settings");
        assertThat(s.getDefaultShareExpiryDays()).isEqualTo(7);
        assertThat(s.getShareMaxExpiryDays()).isNull();
        assertThat(s.isForceSharePassword()).isTrue();
        assertThat(s.isDisableShareLinks()).isFalse();

        assertThat(ws().getSettings("ws_1")).isNull();
    }

    @Test
    void updateSettingsSendsOnlySetFieldsWithRequire2faKeyAndJsonLists() throws Exception {
        enqueue(ok());
        ws().updateSettings("ws_1", new WorkspaceSettingsUpdate()
                .require2fa(true)
                .maxFileSizeGb(null)
                .defaultShareExpiryDays(14)
                .allowedExtensions(".pdf,.docx")
                .ipAllowlist(Arrays.asList("192.168.1.0/24", "10.0.0.1"))
                .countryBlocklist(null)
                .duplicateScanEnabled(false));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws_1/settings");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("require_2fa", "max_file_size_gb",
                "default_share_expiry_days", "allowed_extensions", "ip_allowlist", "country_blocklist",
                "duplicate_scan_enabled");
        assertThat(body.get("require_2fa").getAsBoolean()).isTrue();
        assertThat(body.get("max_file_size_gb").isJsonNull()).isTrue();
        assertThat(body.get("ip_allowlist").getAsString()).isEqualTo("[\"192.168.1.0/24\",\"10.0.0.1\"]");
        assertThat(body.get("country_blocklist").isJsonNull()).isTrue();
        assertThat(body.get("duplicate_scan_enabled").getAsBoolean()).isFalse();
    }

    @Test
    void uploadLimits() throws Exception {
        enqueue(ok("\"allowed_extensions\":\".pdf\",\"blocked_extensions\":null,\"max_file_size_gb\":null,"
                + "\"storage_remaining_bytes\":12345678901,\"max_concurrent_uploads\":0"));
        WorkspaceUploadLimits l = ws().uploadLimits("ws_1");
        assertThat(pathOf(take())).isEqualTo("/api/workspaces/ws_1/upload-limits");
        assertThat(l.getAllowedExtensions()).isEqualTo(".pdf");
        assertThat(l.getMaxFileSizeGb()).isNull();
        assertThat(l.getStorageRemainingBytes()).isEqualTo(12345678901L);
        assertThat(l.getMaxConcurrentUploads()).isZero();
    }

    @Test
    void deletePreview() throws Exception {
        enqueue(ok("\"workspace_id\":\"ws_1\",\"workspace_name\":\"A\",\"file_count\":3,\"total_bytes\":99,"
                + "\"folder_count\":1,\"member_count\":2,\"blockers\":[\"has_members\"]"));
        WorkspaceDeletePreview p = ws().deletePreview("ws_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws_1/delete-preview");
        assertThat(p.getWorkspaceName()).isEqualTo("A");
        assertThat(p.getMemberCount()).isEqualTo(2);
        assertThat(p.getBlockers()).containsExactly("has_members");
    }

    @Test
    void requestDeletion() throws Exception {
        enqueue(ok("\"expires_at\":1700000000,\"sent_to\":\"o@x.y\""));
        WorkspaceDeletionRequest d = ws().requestDeletion("ws_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws_1/delete-request");
        assertThat(d.getExpiresAt()).isEqualTo(1700000000L);
        assertThat(d.getSentTo()).isEqualTo("o@x.y");
    }

    @Test
    void deleteSendsCodeAndConfirmName() throws Exception {
        enqueue(ok());
        DeleteWorkspaceResult res = ws().delete("ws_1", "123456", "My WS");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws_1");
        JsonObject body = jsonBody(r);
        assertThat(body.get("code").getAsString()).isEqualTo("123456");
        assertThat(body.get("confirm_name").getAsString()).isEqualTo("My WS");
        assertThat(res.isPending()).isFalse();
        assertThat(res.getOperationId()).isNull();
    }

    @Test
    void deleteReportsPendingOperation() throws Exception {
        enqueue(ok("\"pending\":true,\"operation_id\":\"op_1\"").setResponseCode(202));
        DeleteWorkspaceResult res = ws().delete("ws_1", "123456", "A");
        assertThat(res.isPending()).isTrue();
        assertThat(res.getOperationId()).isEqualTo("op_1");
    }

    @Test
    void deleteIsNeverRetried() {
        enqueue(fail(503, "down").setHeader("Retry-After", "0"), ok());
        WorkspacesResource retrying = new WorkspacesResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.delete("ws_1", "123456", "A")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void transferSendsUserId() throws Exception {
        enqueue(ok());
        ws().transfer("ws_1", "usr_2");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws_1/transfer");
        assertThat(jsonBody(r).get("user_id").getAsString()).isEqualTo("usr_2");
    }

    @Test
    void leave() throws Exception {
        enqueue(ok());
        ws().leave("ws 1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/workspaces/ws%201/leave");
    }

    @Test
    void rejectsEmptyId() {
        assertThatThrownBy(() -> ws().get("")).isInstanceOf(IllegalArgumentException.class);
    }
}
