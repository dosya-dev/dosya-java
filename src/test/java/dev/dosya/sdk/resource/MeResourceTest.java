package dev.dosya.sdk.resource;

import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.MyWorkspacePermissions;
import dev.dosya.sdk.model.UserProfile;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeResourceTest extends ApiTestSupport {

    @Test
    void profileReadsEveryField() throws Exception {
        enqueue(ok("\"user\":{\"id\":\"usr_1\",\"email\":\"a@b.co\",\"name\":\"Ada Lovelace\",\"initials\":\"AL\","
                + "\"avatar_url\":\"avatars/usr_1/avatar.png\",\"deletion_scheduled_for\":1800000000,"
                + "\"preferred_language\":\"en\",\"ui_theme\":\"dark\",\"ui_mode\":\"system\","
                + "\"created_at\":1700000000,\"email_verified_at\":1700000001,\"has_password\":false,"
                + "\"workspace_count\":2,\"tour_completed\":true}"));

        UserProfile user = new MeResource(http()).profile();

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/me");
        assertThat(user.getId()).isEqualTo("usr_1");
        assertThat(user.getEmail()).isEqualTo("a@b.co");
        assertThat(user.getName()).isEqualTo("Ada Lovelace");
        assertThat(user.getInitials()).isEqualTo("AL");
        assertThat(user.getAvatarUrl()).isEqualTo("avatars/usr_1/avatar.png");
        assertThat(user.getDeletionScheduledFor()).isEqualTo(1800000000L);
        assertThat(user.getPreferredLanguage()).isEqualTo("en");
        assertThat(user.getUiTheme()).isEqualTo("dark");
        assertThat(user.getUiMode()).isEqualTo("system");
        assertThat(user.getCreatedAt()).isEqualTo(1700000000L);
        assertThat(user.getEmailVerifiedAt()).isEqualTo(1700000001L);
        assertThat(user.hasPassword()).isFalse();
        assertThat(user.getWorkspaceCount()).isEqualTo(2);
        assertThat(user.isTourCompleted()).isTrue();
    }

    @Test
    void profileNullableFields() {
        enqueue(ok("\"user\":{\"id\":\"usr_1\",\"avatar_url\":null,\"deletion_scheduled_for\":null,"
                + "\"email_verified_at\":null,\"has_password\":true}"));
        UserProfile user = new MeResource(http()).profile();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getDeletionScheduledFor()).isNull();
        assertThat(user.getEmailVerifiedAt()).isNull();
        assertThat(user.hasPassword()).isTrue();
    }

    @Test
    void permissionsSendsWorkspaceIdAndKeepsSnakeCaseKeys() throws Exception {
        enqueue(ok("\"user_id\":\"usr_1\",\"role_id\":\"role_admin\",\"role_name\":\"Admin\",\"is_builtin\":true,"
                + "\"root_folder_id\":\"fld_1\",\"root_folder_name\":null,"
                + "\"permissions\":{\"upload_files\":true,\"manage_settings\":false}"));

        MyWorkspacePermissions res = new MeResource(http()).permissions("ws 1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/me/permissions");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws 1");
        assertThat(res.getUserId()).isEqualTo("usr_1");
        assertThat(res.getRoleId()).isEqualTo("role_admin");
        assertThat(res.getRoleName()).isEqualTo("Admin");
        assertThat(res.isBuiltin()).isTrue();
        assertThat(res.getRootFolderId()).isEqualTo("fld_1");
        assertThat(res.getRootFolderName()).isNull();
        assertThat(res.getPermissions()).containsEntry("upload_files", true).containsEntry("manage_settings", false);
        assertThat(res.has("upload_files")).isTrue();
        assertThat(res.has("manage_settings")).isFalse();
        assertThat(res.has("nope")).isFalse();
    }

    @Test
    void permissionsNotAMember() {
        enqueue(fail(403, "Not a member"));
        assertThatThrownBy(() -> new MeResource(http()).permissions("ws_1"))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(e -> assertThat(((DosyaApiException) e).getStatus()).isEqualTo(403));
    }

    @Test
    void updateNamePutsNameAndReturnsStoredName() throws Exception {
        enqueue(ok("\"name\":\"Ada\""));
        String stored = new MeResource(http()).updateName(" Ada ");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/me/name");
        com.google.gson.JsonObject body = jsonBody(r);
        assertThat(body.get("name").getAsString()).isEqualTo(" Ada ");
        assertThat(body.size()).isEqualTo(1);
        assertThat(stored).isEqualTo("Ada");
    }

    @Test
    void updateNameSurfacesValidationError() {
        enqueue(fail(400, "Name is required"));
        assertThatThrownBy(() -> new MeResource(http()).updateName(" "))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(e -> {
                    assertThat(((DosyaApiException) e).getStatus()).isEqualTo(400);
                    assertThat(((DosyaApiException) e).getErrorMessage()).isEqualTo("Name is required");
                });
    }

    @Test
    void revokeCurrentKeyDeletesCurrent() throws Exception {
        enqueue(ok());
        new MeResource(http()).revokeCurrentKey();
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/me/api-keys/current");
    }

    @Test
    void revokeCurrentKeyIsNeverRetried() {
        enqueue(fail(503, "Service temporarily unavailable"), ok());
        DosyaHttpClient retrying = new DosyaHttpClient(options().maxRetries(3));
        assertThatThrownBy(() -> new MeResource(retrying).revokeCurrentKey()).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void sessionOnlyApiKeyMethodsAreGone() {
        assertThat(Arrays.stream(MeResource.class.getMethods()).map(Method::getName))
                .doesNotContain("listApiKeys", "createApiKey", "deleteApiKey");
    }
}
