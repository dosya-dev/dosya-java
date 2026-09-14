package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.BatchFolderInput;
import dev.dosya.sdk.model.BatchFolderResult;
import dev.dosya.sdk.model.CreateFolderParams;
import dev.dosya.sdk.model.CreateFolderResponse;
import dev.dosya.sdk.model.CreateShareLinkParams;
import dev.dosya.sdk.model.CreatedShareLink;
import dev.dosya.sdk.model.DeleteFolderResult;
import dev.dosya.sdk.model.FolderChild;
import dev.dosya.sdk.model.FolderDetail;
import dev.dosya.sdk.model.FolderSearchResult;
import dev.dosya.sdk.model.FolderTreeItem;
import dev.dosya.sdk.model.HiddenMode;
import dev.dosya.sdk.model.HideInfo;
import dev.dosya.sdk.model.ItemShareLink;
import dev.dosya.sdk.model.LockInfo;
import dev.dosya.sdk.model.LockMode;
import dev.dosya.sdk.model.PurgeFolderOptions;
import dev.dosya.sdk.model.PurgeFolderSummary;
import dev.dosya.sdk.model.RestoreFolderResult;
import dev.dosya.sdk.model.SetHideParams;
import dev.dosya.sdk.model.ShareByEmailParams;
import dev.dosya.sdk.model.ShareByEmailResult;
import dev.dosya.sdk.model.UnlockGrant;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoldersResourceTest extends ApiTestSupport {

    private FoldersResource folders() {
        return new FoldersResource(http());
    }

    private static MockResponse status(MockResponse r, int code) {
        return r.setResponseCode(code);
    }

    private static String folderJson(int isDeleted) {
        return "\"folder\":{\"id\":\"fld_1\",\"name\":\"a\",\"workspace_id\":\"ws\",\"parent_id\":null,"
                + "\"is_synced\":0,\"is_deleted\":" + isDeleted + ",\"trash_root_id\":" + (isDeleted == 1 ? "\"fld_1\"" : "null")
                + ",\"created_at\":1,\"updated_at\":1}";
    }

    // ── create ──

    @Test
    void createPostsSnakeCaseBodyAndParsesRealResponse() throws Exception {
        enqueue(status(ok("\"folder\":{\"id\":\"fld_c\",\"name\":\"c\",\"parent_id\":\"fld_b\",\"workspace_id\":\"ws_1\"},"
                + "\"created_count\":2,\"created_folders\":[{\"id\":\"fld_b\",\"name\":\"b\",\"parent_id\":\"fld_a\"},"
                + "{\"id\":\"fld_c\",\"name\":\"c\",\"parent_id\":\"fld_b\"}]"), 201));

        CreateFolderResponse res = folders().create(new CreateFolderParams("ws_1", "b/c").parentId("fld_a"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/folders");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("workspace_id", "parent_id", "name");
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("parent_id").getAsString()).isEqualTo("fld_a");
        assertThat(body.get("name").getAsString()).isEqualTo("b/c");

        assertThat(res.getFolder().getId()).isEqualTo("fld_c");
        assertThat(res.getFolder().getParentId()).isEqualTo("fld_b");
        assertThat(res.getFolder().getWorkspaceId()).isEqualTo("ws_1");
        assertThat(res.getCreatedCount()).isEqualTo(2);
        assertThat(res.getCreatedFolders()).hasSize(2);
        assertThat(res.getCreatedFolders().get(1).getId()).isEqualTo("fld_c");
        assertThat(res.getCreatedFolders().get(1).getParentId()).isEqualTo("fld_b");
    }

    @Test
    void createAtRootOmitsParentAndReportsIdempotentNoop() throws Exception {
        enqueue(status(ok("\"folder\":{\"id\":\"fld_a\",\"name\":\"a\",\"parent_id\":null,\"workspace_id\":\"ws_1\"},"
                + "\"created_count\":0,\"created_folders\":[]"), 201));

        CreateFolderResponse res = folders().create("ws_1", "a");

        assertThat(jsonBody(take()).has("parent_id")).isFalse();
        assertThat(res.getCreatedCount()).isZero();
        assertThat(res.getCreatedFolders()).isEmpty();
        assertThat(res.getFolder().getParentId()).isNull();
    }

    @Test
    void createBatchSendsExplicitNullParent() throws Exception {
        enqueue(ok("\"folders\":[{\"name\":\"x\",\"parent_id\":null,\"id\":\"fld_x\",\"created\":true}]"));

        List<BatchFolderResult> res = folders().createBatch("ws_1",
                Arrays.asList(new BatchFolderInput("x"), new BatchFolderInput("y", "fld_p")));

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/folders/batch");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        JsonObject first = body.getAsJsonArray("folders").get(0).getAsJsonObject();
        assertThat(first.has("parent_id")).isTrue();
        assertThat(first.get("parent_id").isJsonNull()).isTrue();
        assertThat(body.getAsJsonArray("folders").get(1).getAsJsonObject().get("parent_id").getAsString()).isEqualTo("fld_p");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo("fld_x");
        assertThat(res.get(0).getParentId()).isNull();
        assertThat(res.get(0).isCreated()).isTrue();
    }

    // ── get ──

    @Test
    void getEncodesIdAndReturnsFullDetail() throws Exception {
        enqueue(ok("\"folder\":{\"id\":\"fld/1\",\"name\":\"Docs\",\"workspace_id\":\"ws_1\",\"parent_id\":null,"
                + "\"is_synced\":1,\"is_deleted\":1,\"trash_root_id\":\"fld/1\",\"created_at\":1,\"updated_at\":2}"));

        FolderDetail f = folders().get("fld/1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld%2F1");
        assertThat(f.getId()).isEqualTo("fld/1");
        assertThat(f.getName()).isEqualTo("Docs");
        assertThat(f.getParentId()).isNull();
        assertThat(f.isSynced()).isTrue();
        assertThat(f.isDeleted()).isTrue();
        assertThat(f.getTrashRootId()).isEqualTo("fld/1");
        assertThat(f.getCreatedAt()).isEqualTo(1);
        assertThat(f.getUpdatedAt()).isEqualTo(2);
    }

    @Test
    void rejectsDotSegmentIds() {
        assertThatThrownBy(() -> folders().get("..")).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    // ── rename / move / restore ──

    @Test
    void regressionRenamePutsRenameRouteNotRestore() throws Exception {
        enqueue(ok("\"name\":\"New\""));

        String name = folders().rename("fld_1", "New");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/rename");
        assertThat(jsonBody(r).get("name").getAsString()).isEqualTo("New");
        assertThat(name).isEqualTo("New");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void moveToRootSendsExplicitNull() throws Exception {
        enqueue(ok());

        folders().move("fld_1", null);

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/move");
        JsonObject body = jsonBody(r);
        assertThat(body.has("parent_id")).isTrue();
        assertThat(body.get("parent_id").isJsonNull()).isTrue();
    }

    @Test
    void moveSendsParentAndIsNeverRetried() {
        enqueue(fail(500, "boom"), ok());
        FoldersResource retrying = new FoldersResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.move("fld_1", "fld_2")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void restorePutsFolderRouteWithoutBody() throws Exception {
        enqueue(ok("\"folder_id\":\"fld_1\",\"name\":\"Docs (1)\",\"restored_to_root\":true,\"files_restored\":3,\"folders_restored\":2"));

        RestoreFolderResult res = folders().restore("fld_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1");
        assertThat(r.getBodySize()).isZero();
        assertThat(res.getFolderId()).isEqualTo("fld_1");
        assertThat(res.getName()).isEqualTo("Docs (1)");
        assertThat(res.isRestoredToRoot()).isTrue();
        assertThat(res.getFilesRestored()).isEqualTo(3);
        assertThat(res.getFoldersRestored()).isEqualTo(2);
    }

    @Test
    void restoreIsNeverRetried() {
        enqueue(fail(503, "busy"), ok());
        FoldersResource retrying = new FoldersResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.restore("fld_1")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    // ── delete / purge ──

    @Test
    void deleteReturnsTrashResult() throws Exception {
        enqueue(ok("\"permanent\":false,\"files_affected\":4,\"folders_removed\":2"));

        DeleteFolderResult res = folders().delete("fld_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(r.getRequestUrl().queryParameterNames()).isEmpty();
        assertThat(res.isPermanent()).isFalse();
        assertThat(res.isComplete()).isTrue();
        assertThat(res.getFilesAffected()).isEqualTo(4);
        assertThat(res.getFoldersRemoved()).isEqualTo(2);
        assertThat(res.getRemaining()).isZero();
    }

    @Test
    void deleteReturnsPartialPurgeAndSendsBudget() throws Exception {
        enqueue(status(ok("\"permanent\":true,\"complete\":false,\"remaining\":10,\"files_affected\":5"), 202));

        DeleteFolderResult res = folders().delete("fld_1", 20);

        assertThat(take().getRequestUrl().queryParameter("max_r2_calls")).isEqualTo("20");
        assertThat(res.isPermanent()).isTrue();
        assertThat(res.isComplete()).isFalse();
        assertThat(res.getRemaining()).isEqualTo(10);
        assertThat(res.getFilesAffected()).isEqualTo(5);
        assertThat(res.getFoldersRemoved()).isNull();
    }

    @Test
    void deleteIsNeverRetried() {
        enqueue(fail(500, "boom"), ok("\"permanent\":true,\"complete\":true,\"remaining\":0,\"files_affected\":1"));
        FoldersResource retrying = new FoldersResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.delete("fld_1")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void purgeRefusesLiveFolderWithoutDeleting() throws Exception {
        enqueue(ok(folderJson(0)));

        assertThatThrownBy(() -> folders().purge("fld_1"))
                .isInstanceOf(DosyaException.class)
                .hasMessageContaining("not in the trash");
        assertThat(take().getMethod()).isEqualTo("GET");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void purgeLoopsUntilComplete() throws Exception {
        enqueue(ok(folderJson(1)),
                status(ok("\"permanent\":true,\"complete\":false,\"remaining\":3,\"files_affected\":5"), 202),
                ok("\"permanent\":true,\"complete\":true,\"remaining\":0,\"files_affected\":3"));

        PurgeFolderSummary res = folders().purge("fld_1", new PurgeFolderOptions().maxR2Calls(7));

        assertThat(res.getFilesAffected()).isEqualTo(8);
        assertThat(res.getPasses()).isEqualTo(2);
        assertThat(take().getMethod()).isEqualTo("GET");
        RecordedRequest d1 = take();
        assertThat(d1.getMethod()).isEqualTo("DELETE");
        assertThat(d1.getRequestUrl().queryParameter("max_r2_calls")).isEqualTo("7");
        assertThat(take().getMethod()).isEqualTo("DELETE");
    }

    @Test
    void purgeStopsWhenPassTrashedInstead() {
        enqueue(ok(folderJson(1)), ok("\"permanent\":false,\"files_affected\":0,\"folders_removed\":1"));
        assertThatThrownBy(() -> folders().purge("fld_1"))
                .isInstanceOf(DosyaException.class)
                .hasMessageContaining("not performed");
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void purgeGivesUpAfterMaxPasses() {
        String partial = "\"permanent\":true,\"complete\":false,\"remaining\":3,\"files_affected\":1";
        enqueue(ok(folderJson(1)), status(ok(partial), 202), status(ok(partial), 202));
        assertThatThrownBy(() -> folders().purge("fld_1", new PurgeFolderOptions().maxPasses(2)))
                .isInstanceOf(DosyaException.class)
                .hasMessageContaining("2 passes");
        assertThat(server.getRequestCount()).isEqualTo(3);
    }

    // ── listings ──

    @Test
    void treeSendsWorkspaceId() throws Exception {
        enqueue(ok("\"folders\":[{\"id\":\"f\",\"name\":\"n\",\"parent_id\":null,\"file_count\":2}]"));

        List<FolderTreeItem> res = folders().tree("ws_1");

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/folders/tree");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(res.get(0).getFileCount()).isEqualTo(2);
        assertThat(res.get(0).getParentId()).isNull();
    }

    @Test
    void childrenSendsParentOnlyWhenGiven() throws Exception {
        String reply = "\"folders\":[{\"id\":\"f\",\"name\":\"n\",\"parent_id\":\"p\",\"file_count\":0,\"has_children\":1}]";
        enqueue(ok(reply), ok(reply));

        folders().children("ws_1");
        List<FolderChild> res = folders().children("ws_1", "p");

        RecordedRequest root = take();
        assertThat(pathOf(root)).isEqualTo("/api/folders/children");
        assertThat(root.getRequestUrl().queryParameterNames()).containsExactly("workspace_id");
        RecordedRequest sub = take();
        assertThat(sub.getRequestUrl().queryParameter("parent_id")).isEqualTo("p");
        assertThat(res.get(0).hasChildren()).isTrue();
        assertThat(res.get(0).getParentId()).isEqualTo("p");
    }

    @Test
    void searchSendsQAndReturnsPath() throws Exception {
        enqueue(ok("\"folders\":[{\"id\":\"f\",\"name\":\"inv\",\"file_count\":1,\"path\":\"A / B\"}]"));

        List<FolderSearchResult> res = folders().search("ws_1", "inv");

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/folders/search");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(r.getRequestUrl().queryParameter("q")).isEqualTo("inv");
        assertThat(res.get(0).getPath()).isEqualTo("A / B");
        assertThat(res.get(0).getFileCount()).isEqualTo(1);
    }

    // ── lock ──

    @Test
    void getLockParses() throws Exception {
        enqueue(ok("\"lock_mode\":\"view_only\",\"locked_by\":\"u\",\"locked_by_name\":\"U\",\"locked_at\":5"));

        LockInfo info = folders().getLock("fld_1");

        assertThat(pathOf(take())).isEqualTo("/api/folders/fld_1/lock");
        assertThat(info.getLockMode()).isEqualTo(LockMode.VIEW_ONLY);
        assertThat(info.getLockedByName()).isEqualTo("U");
        assertThat(info.getLockedAt()).isEqualTo(5L);
    }

    @Test
    void lockNoneSendsNoPasswordAndReturnsMode() throws Exception {
        enqueue(ok("\"lock_mode\":\"none\""));

        LockMode mode = folders().lock("fld_1", LockMode.NONE);

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactly("lock_mode");
        assertThat(body.get("lock_mode").getAsString()).isEqualTo("none");
        assertThat(mode).isEqualTo(LockMode.NONE);
    }

    @Test
    void lockFullSendsPassword() throws Exception {
        enqueue(ok("\"lock_mode\":\"full_lock\""));

        LockMode mode = folders().lock("fld_1", LockMode.FULL_LOCK, "secret");

        JsonObject body = jsonBody(take());
        assertThat(body.get("lock_mode").getAsString()).isEqualTo("full_lock");
        assertThat(body.get("password").getAsString()).isEqualTo("secret");
        assertThat(mode).isEqualTo(LockMode.FULL_LOCK);
    }

    @Test
    void regressionUnlockSendsPasswordAndReturnsGrant() throws Exception {
        enqueue(ok("\"unlock_token\":\"ut_abc\",\"expires_at\":99"));

        UnlockGrant grant = folders().unlock("fld_1", "secret");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/unlock");
        assertThat(jsonBody(r).get("password").getAsString()).isEqualTo("secret");
        assertThat(grant.getUnlockToken()).isEqualTo("ut_abc");
        assertThat(grant.getExpiresAt()).isEqualTo(99);
    }

    // ── hide ──

    @Test
    void getHideParsesRules() throws Exception {
        enqueue(ok("\"is_hidden\":true,\"hidden_mode\":\"users\",\"rules\":[{\"target_type\":\"user\",\"target_id\":\"u1\"}]"));

        HideInfo info = folders().getHide("fld_1");

        assertThat(pathOf(take())).isEqualTo("/api/folders/fld_1/hide");
        assertThat(info.isHidden()).isTrue();
        assertThat(info.getHiddenMode()).isEqualTo(HiddenMode.USERS);
        assertThat(info.getRules()).hasSize(1);
    }

    @Test
    void hideSendsTargetsNotTargetIds() throws Exception {
        enqueue(ok("\"hidden_mode\":\"roles\""));

        HiddenMode mode = folders().hide("fld_1", SetHideParams.roles(Collections.singletonList("role_a")));

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/hide");
        JsonObject body = jsonBody(r);
        assertThat(body.get("hidden_mode").getAsString()).isEqualTo("roles");
        assertThat(body.getAsJsonArray("targets").get(0).getAsString()).isEqualTo("role_a");
        assertThat(body.has("target_ids")).isFalse();
        assertThat(mode).isEqualTo(HiddenMode.ROLES);
    }

    // ── share links ──

    @Test
    void getShareLinksUsesFolderRoute() throws Exception {
        enqueue(ok("\"links\":[{\"id\":\"l\",\"token\":\"t\",\"url\":\"u\",\"lock_mode\":\"none\",\"created_at\":1}],\"excluded_count\":1"));

        List<ItemShareLink> links = folders().getShareLinks("fld_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/share");
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getToken()).isEqualTo("t");
    }

    @Test
    void createShareLinkPostsToFolderRoute() throws Exception {
        enqueue(status(ok("\"link\":{\"id\":\"l\",\"token\":\"t\",\"url\":\"u\",\"lock_mode\":\"none\",\"access_mode\":\"public\",\"expires_at\":null,\"created_at\":1}"), 201));

        CreatedShareLink link = folders().createShareLink("fld_1", new CreateShareLinkParams().expiresInDays(7));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/share");
        assertThat(jsonBody(r).get("expires_in_days").getAsInt()).isEqualTo(7);
        assertThat(link.getToken()).isEqualTo("t");
    }

    @Test
    void shareByEmailPostsToShareEmail() throws Exception {
        enqueue(status(ok("\"share_url\":\"u\",\"access_mode\":\"public\",\"sent\":1,\"failed\":[]"), 201));

        ShareByEmailResult res = folders().shareByEmail("fld_1", new ShareByEmailParams(Collections.singletonList("a@b.c")));

        RecordedRequest r = take();
        assertThat(pathOf(r)).isEqualTo("/api/folders/fld_1/share-email");
        assertThat(jsonBody(r).getAsJsonArray("emails").get(0).getAsString()).isEqualTo("a@b.c");
        assertThat(res.getSent()).isEqualTo(1);
    }
}
