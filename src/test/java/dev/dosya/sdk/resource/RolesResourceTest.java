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

class RolesResourceTest extends ApiTestSupport {

    private RolesResource roles() {
        return new RolesResource(http());
    }

    @Test
    void listKeepsSnakeCasePermissionKeys() throws Exception {
        enqueue(ok("\"roles\":[{\"id\":\"role_admin\",\"name\":\"Admin\",\"is_builtin\":true,\"is_custom\":false,"
                + "\"permissions\":{\"upload_files\":true,\"manage_roles\":false},\"allowed_ips\":null,"
                + "\"active_hours\":\"{\\\"tz\\\":\\\"UTC\\\"}\",\"requests_per_minute\":60,\"egress_bytes_per_day\":null,"
                + "\"max_file_size_bytes\":null,\"max_concurrent_transfers\":null}],"
                + "\"all_permissions\":[\"upload_files\",\"manage_roles\"]"));

        RolesListResponse res = roles().list("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/roles");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        Role role = res.getRoles().get(0);
        assertThat(role.isBuiltin()).isTrue();
        assertThat(role.getPermissions()).containsEntry("upload_files", true).containsEntry("manage_roles", false);
        assertThat(role.has("upload_files")).isTrue();
        assertThat(role.has("unknown")).isFalse();
        assertThat(role.getActiveHours()).isEqualTo("{\"tz\":\"UTC\"}");
        assertThat(role.getRequestsPerMinute()).isEqualTo(60L);
        assertThat(res.getAllPermissions()).containsExactly("upload_files", "manage_roles");
    }

    @Test
    void createSendsFieldsAndReturnsId() throws Exception {
        enqueue(ok("\"role_id\":\"role_x\""));
        String id = roles().create(new CreateRoleParams("ws_1", "Contractor")
                .permission("upload_files", true)
                .allowedIps("10.0.0.0/8")
                .activeHours(new ActiveHours("Europe/Berlin", Arrays.asList(1, 2), "09:00", "17:00"))
                .requestsPerMinute(30L));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/roles");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("name").getAsString()).isEqualTo("Contractor");
        assertThat(body.getAsJsonObject("permissions").get("upload_files").getAsBoolean()).isTrue();
        assertThat(body.get("allowed_ips").getAsString()).isEqualTo("10.0.0.0/8");
        JsonObject hours = body.getAsJsonObject("active_hours");
        assertThat(hours.get("tz").getAsString()).isEqualTo("Europe/Berlin");
        assertThat(hours.getAsJsonArray("days").get(1).getAsInt()).isEqualTo(2);
        assertThat(hours.get("from").getAsString()).isEqualTo("09:00");
        assertThat(body.get("requests_per_minute").getAsLong()).isEqualTo(30L);
        assertThat(body.has("egress_bytes_per_day")).isFalse();
        assertThat(id).isEqualTo("role_x");
    }

    @Test
    void updateSendsOnlySetFieldsAndNullClears() throws Exception {
        enqueue(ok());
        roles().update("role/x", new UpdateRoleParams().permission("manage_roles", false).activeHours(null).maxFileSizeBytes(null));
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/roles/role%2Fx");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("permissions", "active_hours", "max_file_size_bytes");
        assertThat(body.get("active_hours").isJsonNull()).isTrue();
        assertThat(body.getAsJsonObject("permissions").get("manage_roles").getAsBoolean()).isFalse();
    }

    @Test
    void deleteIsNeverRetried() throws Exception {
        enqueue(fail(503, "down").setHeader("Retry-After", "0"), ok());
        RolesResource retrying = new RolesResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.delete("role_x")).isInstanceOf(DosyaApiException.class);
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/roles/role_x");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }
}
