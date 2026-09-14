package dev.dosya.sdk.resource;

import dev.dosya.sdk.model.ActivityCategory;
import dev.dosya.sdk.model.ActivityEntry;
import dev.dosya.sdk.model.ActivityListResponse;
import dev.dosya.sdk.model.ListActivityParams;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityResourceTest extends ApiTestSupport {

    private static final String PAGE = "\"activities\":[{\"id\":\"ae1\",\"action\":\"folder_renamed\",\"entity_type\":\"folder\",\"entity_id\":\"fld_1\","
            + "\"metadata\":\"{\\\"old_name\\\":\\\"a\\\",\\\"new_name\\\":\\\"b\\\"}\",\"created_at\":10,\"actor_id\":\"u1\",\"source_ip\":null,"
            + "\"user_agent\":null,\"outcome\":\"success\",\"source\":\"api\",\"action_group\":\"files\",\"on_behalf_of\":null,\"resource_name\":\"b\","
            + "\"request_id\":null,\"session_id\":null,\"actor_type\":\"user\",\"obo_name\":null,\"obo_email\":null,\"user_name\":\"U\",\"user_id\":\"u1\","
            + "\"user_email\":\"u@x.y\",\"user_avatar\":null,\"geo\":{\"country\":\"AU\"},"
            + "\"meta\":{\"old_name\":\"a\",\"new_name\":\"b\",\"files_restored\":3,\"ratio\":0.5,\"nested\":{\"keep_key\":true},\"list\":[1,\"x\"]}}],"
            + "\"members\":[{\"id\":\"u1\",\"name\":\"U\",\"email\":\"u@x.y\",\"avatar_url\":null}],"
            + "\"pagination\":{\"page\":1,\"per_page\":30,\"total\":1,\"total_pages\":1}";

    @Test
    @SuppressWarnings("unchecked")
    void listSendsFiltersAndParsesRealEntryShape() throws Exception {
        enqueue(ok(PAGE));

        ActivityListResponse res = new ActivityResource(http()).list(new ListActivityParams("ws_1")
                .page(1).perPage(30).category(ActivityCategory.FOLDERS).action("folder_renamed").userId("u1"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/activity");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(r.getRequestUrl().queryParameter("page")).isEqualTo("1");
        assertThat(r.getRequestUrl().queryParameter("per_page")).isEqualTo("30");
        assertThat(r.getRequestUrl().queryParameter("category")).isEqualTo("folders");
        assertThat(r.getRequestUrl().queryParameter("action")).isEqualTo("folder_renamed");
        assertThat(r.getRequestUrl().queryParameter("user_id")).isEqualTo("u1");

        ActivityEntry a = res.getActivities().get(0);
        assertThat(a.getEntityType()).isEqualTo("folder");
        assertThat(a.getEntityId()).isEqualTo("fld_1");
        assertThat(a.getUserEmail()).isEqualTo("u@x.y");
        assertThat(a.getOutcome()).isEqualTo("success");
        assertThat(a.getActorType()).isEqualTo("user");
        assertThat(a.getActionGroup()).isEqualTo("files");
        assertThat(a.getSourceIp()).isNull();
        assertThat(a.getGeo().get("country").getAsString()).isEqualTo("AU");
        assertThat(a.getMetadata()).isEqualTo("{\"old_name\":\"a\",\"new_name\":\"b\"}");

        // meta keeps its snake_case keys, both as JsonObject and as a Map
        assertThat(a.getMeta().get("old_name").getAsString()).isEqualTo("a");
        Map<String, Object> meta = a.getMetaMap();
        assertThat(meta).containsKeys("old_name", "new_name", "files_restored");
        assertThat(meta.get("files_restored")).isEqualTo(3L);
        assertThat(meta.get("ratio")).isEqualTo(0.5);
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) meta.get("nested");
        assertThat(nested).containsEntry("keep_key", true);
        assertThat((List<Object>) meta.get("list")).containsExactly(1L, "x");

        assertThat(res.getMembers().get(0).getAvatarUrl()).isNull();
        assertThat(res.getPagination().getPage()).isEqualTo(1);
        assertThat(res.getPagination().getPerPage()).isEqualTo(30);
        assertThat(res.getPagination().getTotal()).isEqualTo(1);
        assertThat(res.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void joinsListFiltersWithCommas() throws Exception {
        enqueue(ok(PAGE));

        new ActivityResource(http()).list(new ListActivityParams("ws_1")
                .category(ActivityCategory.FILES, ActivityCategory.SHARING)
                .action(Arrays.asList("file_uploaded", "file_deleted"))
                .userId("u1", "u2"));

        RecordedRequest r = take();
        assertThat(r.getRequestUrl().queryParameterNames())
                .containsExactlyInAnyOrder("workspace_id", "category", "action", "user_id");
        assertThat(r.getRequestUrl().queryParameter("category")).isEqualTo("files,sharing");
        assertThat(r.getRequestUrl().queryParameter("action")).isEqualTo("file_uploaded,file_deleted");
        assertThat(r.getRequestUrl().queryParameter("user_id")).isEqualTo("u1,u2");
    }

    @Test
    void nullMetaAndGeoStayNull() throws Exception {
        enqueue(ok("\"activities\":[{\"id\":\"ae2\",\"action\":\"x\",\"entity_type\":\"file\",\"entity_id\":null,\"created_at\":1,"
                + "\"geo\":null,\"meta\":null,\"metadata\":null}],\"members\":[],\"pagination\":{\"page\":1,\"per_page\":30,\"total\":0,\"total_pages\":1}"));

        ActivityEntry a = new ActivityResource(http()).list("ws_1").getActivities().get(0);

        assertThat(take().getRequestUrl().queryParameterNames()).containsExactly("workspace_id");
        assertThat(a.getMeta()).isNull();
        assertThat(a.getMetaMap()).isNull();
        assertThat(a.getGeo()).isNull();
        assertThat(a.getEntityId()).isNull();
    }
}
