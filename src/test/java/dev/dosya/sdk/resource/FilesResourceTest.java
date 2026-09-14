package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.BatchDeleteParams;
import dev.dosya.sdk.model.BatchDeleteResult;
import dev.dosya.sdk.model.CopiedFile;
import dev.dosya.sdk.model.CreateShareBundleParams;
import dev.dosya.sdk.model.CreateShareLinkParams;
import dev.dosya.sdk.model.CreatedShareBundle;
import dev.dosya.sdk.model.CreatedShareLink;
import dev.dosya.sdk.model.DuplicatesResponse;
import dev.dosya.sdk.model.FileDetail;
import dev.dosya.sdk.model.FileListItem;
import dev.dosya.sdk.model.FileSort;
import dev.dosya.sdk.model.FileVersionsResponse;
import dev.dosya.sdk.model.FolderListItem;
import dev.dosya.sdk.model.HiddenMode;
import dev.dosya.sdk.model.HideInfo;
import dev.dosya.sdk.model.ItemShareLink;
import dev.dosya.sdk.model.ListFilesParams;
import dev.dosya.sdk.model.ListFilesResponse;
import dev.dosya.sdk.model.LockInfo;
import dev.dosya.sdk.model.LockMode;
import dev.dosya.sdk.model.SetHideParams;
import dev.dosya.sdk.model.ShareAccessMode;
import dev.dosya.sdk.model.ShareByEmailParams;
import dev.dosya.sdk.model.ShareByEmailResult;
import dev.dosya.sdk.model.UnlockGrant;
import dev.dosya.sdk.model.VersionRestoreResponse;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilesResourceTest extends ApiTestSupport {

    private FilesResource files() {
        return new FilesResource(http());
    }

    /** A client that would retry a 503 twice under Retry.AUTO. */
    private FilesResource retryingFiles() {
        return new FilesResource(new DosyaHttpClient(options().maxRetries(2)));
    }

    private static MockResponse unavailable() {
        return fail(503, "Service unavailable");
    }

    // ---- list ----

    @Test
    void listSendsEveryQueryParameterWithWireNames() throws Exception {
        enqueue(ok("\"folders\":[],\"files\":[],\"breadcrumbs\":[],\"workspace_id\":\"ws_1\",\"folder_id\":null,"
                + "\"can_lock\":true,\"can_hide\":false,\"folder_view_only\":false,"
                + "\"pagination\":{\"page\":2,\"per_page\":50,\"total_files\":120,\"total_pages\":3}"));

        files().list(new ListFilesParams("ws_1")
                .folderId("fld_1").filter("images").sort(FileSort.TAKEN).dir("asc").q("cat")
                .hidden(true).unlockToken("ut_1").groupId("grp_1").page(2).perPage(50));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(r.getRequestUrl().queryParameter("folder_id")).isEqualTo("fld_1");
        assertThat(r.getRequestUrl().queryParameter("filter")).isEqualTo("images");
        assertThat(r.getRequestUrl().queryParameter("sort")).isEqualTo("taken");
        assertThat(r.getRequestUrl().queryParameter("dir")).isEqualTo("asc");
        assertThat(r.getRequestUrl().queryParameter("q")).isEqualTo("cat");
        assertThat(r.getRequestUrl().queryParameter("hidden")).isEqualTo("1");
        assertThat(r.getRequestUrl().queryParameter("ut")).isEqualTo("ut_1");
        assertThat(r.getRequestUrl().queryParameter("group_id")).isEqualTo("grp_1");
        assertThat(r.getRequestUrl().queryParameter("page")).isEqualTo("2");
        assertThat(r.getRequestUrl().queryParameter("per_page")).isEqualTo("50");
        assertThat(r.getRequestUrl().queryParameter("deleted")).isNull();
    }

    @Test
    void listTrashSendsDeletedAsOneAndTheTrashedFolder() throws Exception {
        // Regression: the handler checks deleted === "1"; "true" silently listed live items.
        enqueue(ok("\"folders\":[],\"files\":[]"));

        files().list(new ListFilesParams("ws_1").deleted(true).folder("fld_trashed"));

        RecordedRequest r = take();
        assertThat(r.getRequestUrl().queryParameter("deleted")).isEqualTo("1");
        assertThat(r.getRequestUrl().queryParameter("folder")).isEqualTo("fld_trashed");
        assertThat(r.getRequestUrl().queryParameter("hidden")).isNull();
    }

    @Test
    void listDropsFolderOutsideTheTrashAndFalseFlags() throws Exception {
        enqueue(ok("\"folders\":[],\"files\":[]"));

        files().list(new ListFilesParams("ws_1").deleted(false).hidden(false).folder("fld_x"));

        RecordedRequest r = take();
        assertThat(r.getRequestUrl().queryParameter("deleted")).isNull();
        assertThat(r.getRequestUrl().queryParameter("hidden")).isNull();
        assertThat(r.getRequestUrl().queryParameter("folder")).isNull();
    }

    @Test
    void listParsesFoldersFilesBreadcrumbsAndPagination() {
        enqueue(ok("\"folders\":[{\"id\":\"fld_1\",\"name\":\"Photos\",\"created_at\":10,\"updated_at\":20,"
                + "\"lock_mode\":\"view_only\",\"is_hidden\":0,\"hidden_mode\":\"none\",\"is_synced\":1,\"origin\":null,"
                + "\"file_count\":3,\"uploader_name\":\"Ada\",\"share_count\":1,\"comment_count\":2,"
                + "\"total_size_bytes\":5000000000,\"content_updated_at\":30,\"region\":\"multi\"}],"
                + "\"files\":[{\"id\":\"file_1\",\"name\":\"a.jpg\",\"size_bytes\":42,\"mime_type\":\"image/jpeg\","
                + "\"extension\":null,\"region\":\"eu\",\"uploaded_by\":\"usr_1\",\"created_at\":1,\"updated_at\":2,"
                + "\"deleted_at\":null,\"lock_mode\":\"full_lock\",\"is_hidden\":1,\"hidden_mode\":\"roles\","
                + "\"current_version\":4,\"is_synced\":0,\"import_source\":\"gdrive\",\"import_account_email\":\"a@b.c\","
                + "\"origin\":\"desktop\",\"content_hash\":\"h1\",\"etag\":\"e1\",\"captured_at\":\"2026-01-02T03:04:05\","
                + "\"uploader_name\":null,\"share_count\":7,\"comment_count\":8}],"
                + "\"breadcrumbs\":[{\"id\":\"fld_0\",\"name\":\"Root\"}],\"workspace_id\":\"ws_1\",\"folder_id\":\"fld_0\","
                + "\"can_lock\":true,\"can_hide\":true,\"folder_view_only\":true,"
                + "\"pagination\":{\"page\":1,\"per_page\":100,\"total_files\":1,\"total_pages\":1}"));

        ListFilesResponse res = files().list(new ListFilesParams("ws_1"));

        assertThat(res.getWorkspaceId()).isEqualTo("ws_1");
        assertThat(res.getFolderId()).isEqualTo("fld_0");
        assertThat(res.isCanLock()).isTrue();
        assertThat(res.isFolderViewOnly()).isTrue();
        assertThat(res.getBreadcrumbs()).hasSize(1);
        assertThat(res.getPagination().getTotalFiles()).isEqualTo(1);

        FolderListItem folder = res.getFolders().get(0);
        assertThat(folder.getLockModeValue()).isEqualTo(LockMode.VIEW_ONLY);
        assertThat(folder.getHiddenModeValue()).isEqualTo(HiddenMode.NONE);
        assertThat(folder.getUpdatedAt()).isEqualTo(20);
        assertThat(folder.getUploaderName()).isEqualTo("Ada");
        assertThat(folder.getShareCount()).isEqualTo(1);
        assertThat(folder.getCommentCount()).isEqualTo(2);
        assertThat(folder.getTotalSizeBytes()).isEqualTo(5_000_000_000L);
        assertThat(folder.getContentUpdatedAt()).isEqualTo(30L);
        assertThat(folder.getRegion()).isEqualTo("multi");
        assertThat(folder.getIsTrashRoot()).isNull();
        assertThat(folder.getDeletedAt()).isNull();

        FileListItem file = res.getFiles().get(0);
        assertThat(file.getLockModeValue()).isEqualTo(LockMode.FULL_LOCK);
        assertThat(file.getHiddenModeValue()).isEqualTo(HiddenMode.ROLES);
        assertThat(file.getExtension()).isNull();
        assertThat(file.getUploaderName()).isNull();
        assertThat(file.getImportSource()).isEqualTo("gdrive");
        assertThat(file.getImportAccountEmail()).isEqualTo("a@b.c");
        assertThat(file.getOrigin()).isEqualTo("desktop");
        assertThat(file.getContentHash()).isEqualTo("h1");
        assertThat(file.getEtag()).isEqualTo("e1");
        assertThat(file.getCapturedAt()).isEqualTo("2026-01-02T03:04:05");
        assertThat(file.getShareCount()).isEqualTo(7);
    }

    @Test
    void listParsesTrashOnlyFolderFields() {
        enqueue(ok("\"folders\":[{\"id\":\"fld_1\",\"name\":\"Old\",\"deleted_at\":99,\"is_trash_root\":true,"
                + "\"trashed_size_bytes\":123,\"region\":null}],\"files\":[]"));

        FolderListItem folder = files().list(new ListFilesParams("ws_1").deleted(true)).getFolders().get(0);

        assertThat(folder.getDeletedAt()).isEqualTo(99L);
        assertThat(folder.getIsTrashRoot()).isTrue();
        assertThat(folder.getTrashedSizeBytes()).isEqualTo(123L);
        assertThat(folder.getRegion()).isNull();
    }

    @Test
    void fileSortHelpersBuildSuffixedValues() {
        assertThat(FileSort.asc(FileSort.NAME)).isEqualTo("name_asc");
        assertThat(FileSort.desc(FileSort.COMMENTS)).isEqualTo("comments_desc");
    }

    @Test
    void lockedFolderListingSurfacesTheApiError() {
        enqueue(new MockResponse().setResponseCode(403).setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":false,\"error\":\"folder_locked\",\"folder_id\":\"fld_1\",\"lock_mode\":\"full_lock\"}"));

        assertThatThrownBy(() -> files().list(new ListFilesParams("ws_1").folderId("fld_1")))
                .isInstanceOf(DosyaApiException.class)
                .satisfies(e -> assertThat(((DosyaApiException) e).getStatus()).isEqualTo(403));
    }

    // ---- get / delete / restore / rename ----

    @Test
    void getEncodesTheIdAndParsesTheFile() throws Exception {
        enqueue(ok("\"file\":{\"id\":\"a/b\",\"name\":\"x.pdf\",\"workspace_id\":\"ws_1\",\"folder_id\":null,"
                + "\"lock_mode\":\"none\",\"hidden_mode\":\"everyone\",\"content_hash\":\"h\",\"etag\":\"e\","
                + "\"share_count\":2,\"comment_count\":3}"));

        FileDetail file = files().get("a/b");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files/a%2Fb");
        assertThat(file.getName()).isEqualTo("x.pdf");
        assertThat(file.getFolderId()).isNull();
        assertThat(file.getHiddenModeValue()).isEqualTo(HiddenMode.EVERYONE);
        assertThat(file.getLockModeValue()).isEqualTo(LockMode.NONE);
        assertThat(file.getContentHash()).isEqualTo("h");
        assertThat(file.getEtag()).isEqualTo("e");
        assertThat(file.getShareCount()).isEqualTo(2);
        assertThat(file.getCommentCount()).isEqualTo(3);
    }

    @Test
    void getRejectsDotSegmentIds() {
        assertThatThrownBy(() -> files().get("..")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteReportsTrashAndPurge() throws Exception {
        enqueue(ok("\"permanent\":false"), ok("\"permanent\":true"));

        assertThat(files().delete("file_1")).isFalse();
        assertThat(files().delete("file_1")).isTrue();

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1");
    }

    @Test
    void deleteIsNeverRetried() {
        // A replayed trash would purge the file.
        enqueue(unavailable(), ok("\"permanent\":true"));

        assertThatThrownBy(() -> retryingFiles().delete("file_1")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void restoreIsAPutAndNeverRetried() throws Exception {
        enqueue(ok());
        files().restore("file_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1");

        enqueue(unavailable(), ok());
        assertThatThrownBy(() -> retryingFiles().restore("file_1")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    void renameSendsNameAndReturnsStoredName() throws Exception {
        enqueue(ok("\"name\":\"new.txt\""));

        assertThat(files().rename("file_1", " new.txt ")).isEqualTo("new.txt");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/rename");
        assertThat(jsonBody(r).get("name").getAsString()).isEqualTo(" new.txt ");
    }

    // ---- move / copy ----

    @Test
    void moveWithRenameSendsFolderAndNameAndReturnsName() throws Exception {
        enqueue(ok("\"name\":\"renamed.txt\""));

        assertThat(files().move("file_1", "fld_2", "renamed.txt")).isEqualTo("renamed.txt");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/move");
        JsonObject body = jsonBody(r);
        assertThat(body.get("folder_id").getAsString()).isEqualTo("fld_2");
        assertThat(body.get("name").getAsString()).isEqualTo("renamed.txt");
    }

    @Test
    void moveToRootSendsExplicitNullFolderAndNoName() throws Exception {
        enqueue(ok("\"name\":\"a.txt\""));

        assertThat(files().move("file_1", null)).isEqualTo("a.txt");

        JsonObject body = jsonBody(take());
        assertThat(body.has("folder_id")).isTrue();
        assertThat(body.get("folder_id").isJsonNull()).isTrue();
        assertThat(body.has("name")).isFalse();
    }

    @Test
    void moveIsNeverRetried() {
        enqueue(unavailable(), ok("\"name\":\"a\""));
        assertThatThrownBy(() -> retryingFiles().move("file_1", "fld_1")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void copyReturnsNewIdAndName() throws Exception {
        // Regression: the handler answers {file_id, name}, not {file}, and ignores new_name.
        enqueue(new MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true,\"file_id\":\"file_2\",\"name\":\"Copy of a.txt\"}"));

        CopiedFile copy = files().copy("file_1", "fld_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/copy");
        JsonObject body = jsonBody(r);
        assertThat(body.get("folder_id").getAsString()).isEqualTo("fld_1");
        assertThat(body.has("new_name")).isFalse();
        assertThat(copy.getFileId()).isEqualTo("file_2");
        assertThat(copy.getName()).isEqualTo("Copy of a.txt");
    }

    @Test
    void copyWithoutFolderTargetsTheRoot() throws Exception {
        enqueue(ok("\"file_id\":\"file_2\",\"name\":\"a.txt\""));

        files().copy("file_1");

        JsonObject body = jsonBody(take());
        assertThat(body.has("folder_id")).isTrue();
        assertThat(body.get("folder_id").isJsonNull()).isTrue();
    }

    // ---- lock / unlock ----

    @Test
    void getLockParsesLockInfo() throws Exception {
        enqueue(ok("\"lock_mode\":\"full_lock\",\"locked_by\":\"usr_1\",\"locked_by_name\":\"Ada\",\"locked_at\":5"));

        LockInfo info = files().getLock("file_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/lock");
        assertThat(info.getLockMode()).isEqualTo(LockMode.FULL_LOCK);
        assertThat(info.getLockedByName()).isEqualTo("Ada");
        assertThat(info.getLockedAt()).isEqualTo(5L);
    }

    @Test
    void lockSendsModeAndPasswordAndReturnsMode() throws Exception {
        enqueue(ok("\"lock_mode\":\"full_lock\""));

        assertThat(files().lock("file_1", LockMode.FULL_LOCK, "secret")).isEqualTo(LockMode.FULL_LOCK);

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/lock");
        JsonObject body = jsonBody(r);
        assertThat(body.get("lock_mode").getAsString()).isEqualTo("full_lock");
        assertThat(body.get("password").getAsString()).isEqualTo("secret");
    }

    @Test
    void lockNoneRemovesTheLockWithoutPassword() throws Exception {
        enqueue(ok("\"lock_mode\":\"none\""));

        assertThat(files().lock("file_1", LockMode.NONE)).isEqualTo(LockMode.NONE);

        JsonObject body = jsonBody(take());
        assertThat(body.get("lock_mode").getAsString()).isEqualTo("none");
        assertThat(body.has("password")).isFalse();
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedStringLockStillSendsTheWireValue() throws Exception {
        enqueue(ok("\"lock_mode\":\"view_only\""));

        files().lock("file_1", "view_only");

        assertThat(jsonBody(take()).get("lock_mode").getAsString()).isEqualTo("view_only");
    }

    @Test
    void unlockSendsPasswordAndReturnsGrant() throws Exception {
        // Regression: unlock used to send no body (always 400) and was documented as removing the lock.
        enqueue(ok("\"unlock_token\":\"tok_1\",\"expires_at\":1700003600"));

        UnlockGrant grant = files().unlock("file_1", "secret");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/unlock");
        assertThat(jsonBody(r).get("password").getAsString()).isEqualTo("secret");
        assertThat(grant.getUnlockToken()).isEqualTo("tok_1");
        assertThat(grant.getExpiresAt()).isEqualTo(1700003600L);
    }

    // ---- hide ----

    @Test
    void getHideParsesRules() throws Exception {
        enqueue(ok("\"is_hidden\":1,\"hidden_mode\":\"users\",\"rules\":[{\"target_type\":\"user\",\"target_id\":\"usr_1\"}]"));

        HideInfo info = files().getHide("file_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/hide");
        assertThat(info.isHidden()).isTrue();
        assertThat(info.getHiddenMode()).isEqualTo(HiddenMode.USERS);
        assertThat(info.getRules().get(0).getTargetId()).isEqualTo("usr_1");
    }

    @Test
    void hideSendsTargetsNotTargetIds() throws Exception {
        // Regression: target_ids is ignored by the handler, so user/role hiding always failed.
        enqueue(ok("\"hidden_mode\":\"roles\""));

        HiddenMode mode = files().hide("file_1", SetHideParams.roles(Arrays.asList("role_1", "role_2")));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/hide");
        JsonObject body = jsonBody(r);
        assertThat(body.get("hidden_mode").getAsString()).isEqualTo("roles");
        assertThat(body.getAsJsonArray("targets")).hasSize(2);
        assertThat(body.has("target_ids")).isFalse();
        assertThat(mode).isEqualTo(HiddenMode.ROLES);
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedHideMapsToTargets() throws Exception {
        enqueue(ok("\"hidden_mode\":\"users\""));

        files().hide("file_1", "users", Collections.singletonList("usr_1"));

        JsonObject body = jsonBody(take());
        assertThat(body.get("hidden_mode").getAsString()).isEqualTo("users");
        assertThat(body.getAsJsonArray("targets").get(0).getAsString()).isEqualTo("usr_1");
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedHideRefusesAMissingModeInsteadOfUnhiding() {
        assertThatThrownBy(() -> files().hide("file_1", null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    // ---- versions ----

    @Test
    void listVersionsParsesCurrentFileFields() throws Exception {
        enqueue(ok("\"file_name\":\"a.txt\",\"file_size\":6000000000,\"file_mime\":\"text/plain\",\"file_created\":7,"
                + "\"current_version\":1,\"versions\":[{\"id\":\"fver_implicit_file_1\",\"version_number\":1,"
                + "\"size_bytes\":6000000000,\"mime_type\":\"text/plain\",\"extension\":null,\"uploaded_by\":\"usr_1\","
                + "\"created_at\":7,\"uploader_name\":null}]"));

        FileVersionsResponse res = files().listVersions("file_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/versions");
        assertThat(res.getFileName()).isEqualTo("a.txt");
        assertThat(res.getFileSize()).isEqualTo(6_000_000_000L);
        assertThat(res.getFileMime()).isEqualTo("text/plain");
        assertThat(res.getFileCreated()).isEqualTo(7L);
        assertThat(res.getVersions().get(0).getId()).isEqualTo("fver_implicit_file_1");
        assertThat(res.getVersions().get(0).getSizeBytes()).isEqualTo(6_000_000_000L);
    }

    @Test
    void restoreVersionSendsVersionNumber() throws Exception {
        enqueue(ok("\"version\":5,\"restored_from\":2"));

        VersionRestoreResponse res = files().restoreVersion("file_1", 2);

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/versions/restore");
        assertThat(jsonBody(r).get("version_number").getAsInt()).isEqualTo(2);
        assertThat(res.getVersion()).isEqualTo(5);
        assertThat(res.getRestoredFrom()).isEqualTo(2);
    }

    // ---- sharing ----

    @Test
    void getShareLinksParsesItemShareLinks() throws Exception {
        enqueue(ok("\"links\":[{\"id\":\"sl_1\",\"token\":\"t\",\"url\":\"https://dosya.dev/s/t\",\"is_password_protected\":1,"
                + "\"expires_at\":null,\"view_count\":3,\"download_count\":4,\"is_revoked\":0,\"lock_mode\":\"view_only\","
                + "\"created_at\":9}]"));

        List<ItemShareLink> links = files().getShareLinks("file_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/share");
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getUrl()).isEqualTo("https://dosya.dev/s/t");
        assertThat(links.get(0).isPasswordProtected()).isTrue();
        assertThat(links.get(0).getViewCount()).isEqualTo(3);
        assertThat(links.get(0).getLockMode()).isEqualTo(LockMode.VIEW_ONLY);
    }

    @Test
    void createShareLinkSendsRestrictedOptionsAndParsesLink() throws Exception {
        enqueue(new MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true,\"link\":{\"id\":\"sl_1\",\"token\":\"t\",\"url\":\"u\",\"lock_mode\":\"none\","
                        + "\"access_mode\":\"restricted\",\"expires_at\":100,\"created_at\":9}}"));

        CreatedShareLink link = files().createShareLink("file_1", new CreateShareLinkParams()
                .accessMode(ShareAccessMode.RESTRICTED)
                .recipientEmails(Collections.singletonList("a@example.com"))
                .maxDownloads(10));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/share");
        JsonObject body = jsonBody(r);
        assertThat(body.get("access_mode").getAsString()).isEqualTo("restricted");
        assertThat(body.getAsJsonArray("recipient_emails").get(0).getAsString()).isEqualTo("a@example.com");
        assertThat(body.get("max_downloads").getAsInt()).isEqualTo(10);
        assertThat(link.getAccessMode()).isEqualTo(ShareAccessMode.RESTRICTED);
        assertThat(link.getExpiresAt()).isEqualTo(100L);
    }

    @Test
    void createShareLinkWithoutParamsSendsEmptyBody() throws Exception {
        enqueue(ok("\"link\":{\"id\":\"sl_1\",\"token\":\"t\",\"url\":\"u\",\"created_at\":1}"));

        files().createShareLink("file_1");

        assertThat(jsonBody(take()).size()).isZero();
    }

    @Test
    void shareByEmailReturnsDeliveryOutcomeAndIsNeverRetried() throws Exception {
        enqueue(ok("\"share_url\":\"https://dosya.dev/s/t\",\"access_mode\":\"restricted\",\"sent\":1,\"failed\":[\"b@x.y\"]"));

        ShareByEmailResult res = files().shareByEmail("file_1",
                new ShareByEmailParams(Arrays.asList("a@x.y", "b@x.y")).message("hi").restrictToRecipients(true));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/file_1/share-email");
        JsonObject body = jsonBody(r);
        assertThat(body.getAsJsonArray("emails")).hasSize(2);
        assertThat(body.get("restrict_to_recipients").getAsBoolean()).isTrue();
        assertThat(res.getSent()).isEqualTo(1);
        assertThat(res.getFailed()).containsExactly("b@x.y");

        enqueue(unavailable(), ok("\"share_url\":\"u\",\"sent\":1,\"failed\":[]"));
        assertThatThrownBy(() -> retryingFiles().shareByEmail("file_1",
                new ShareByEmailParams(Collections.singletonList("a@x.y")))).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedShareByEmailStillSendsEmailsAndMessage() throws Exception {
        enqueue(ok("\"share_url\":\"u\",\"sent\":1,\"failed\":[]"));

        files().shareByEmail("file_1", Collections.singletonList("a@x.y"), "hello");

        JsonObject body = jsonBody(take());
        assertThat(body.getAsJsonArray("emails").get(0).getAsString()).isEqualTo("a@x.y");
        assertThat(body.get("message").getAsString()).isEqualTo("hello");
    }

    @Test
    void createShareBundleSendsAllOptionsAndParsesSentAndFailed() throws Exception {
        enqueue(ok("\"sent\":2,\"failed\":[],\"link\":{\"id\":\"sl_1\",\"token\":\"t\",\"url\":\"u\",\"lock_mode\":\"none\","
                + "\"access_mode\":\"public\",\"file_count\":2,\"expires_at\":null,\"created_at\":1}"));

        CreatedShareBundle bundle = files().createShareBundle(new CreateShareBundleParams(Arrays.asList("file_1", "file_2"))
                .expiresAt(500).notify(true).message("see").recipientEmails(Arrays.asList("a@x.y", "b@x.y")));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/share-bundle");
        JsonObject body = jsonBody(r);
        assertThat(body.getAsJsonArray("file_ids")).hasSize(2);
        assertThat(body.get("expires_at").getAsLong()).isEqualTo(500);
        assertThat(body.get("notify").getAsBoolean()).isTrue();
        assertThat(body.get("message").getAsString()).isEqualTo("see");
        assertThat(bundle.getSent()).isEqualTo(2);
        assertThat(bundle.getFailed()).isEmpty();
        assertThat(bundle.getLink().getFileCount()).isEqualTo(2);
    }

    // ---- batch delete / duplicates ----

    @Test
    void batchDeleteSendsWorkspaceAndIds() throws Exception {
        enqueue(ok("\"deleted\":3,\"folders_deleted\":1"));

        BatchDeleteResult res = files().batchDelete(new BatchDeleteParams("ws_1")
                .fileIds(Arrays.asList("file_1", "file_2", "file_3")).folderIds(Collections.singletonList("fld_1")));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/files/batch-delete");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.getAsJsonArray("file_ids")).hasSize(3);
        assertThat(body.getAsJsonArray("folder_ids")).hasSize(1);
        assertThat(res.getDeleted()).isEqualTo(3);
        assertThat(res.getFoldersDeleted()).isEqualTo(1);
    }

    @Test
    void batchDeleteOmitsUnsetIdLists() throws Exception {
        enqueue(ok("\"deleted\":0,\"folders_deleted\":1"));

        files().batchDelete(new BatchDeleteParams("ws_1").folderIds(Collections.singletonList("fld_1")));

        assertThat(jsonBody(take()).has("file_ids")).isFalse();
    }

    @Test
    void duplicatesParsesGroups() throws Exception {
        enqueue(ok("\"scan_enabled\":true,\"groups\":[{\"content_hash\":\"h1\",\"size_bytes\":10,\"count\":3,\"wasted_bytes\":20,"
                + "\"files\":[{\"id\":\"file_1\",\"name\":\"a.txt\",\"folder_id\":null,\"created_at\":5,\"uploaded_by\":\"usr_1\","
                + "\"uploader_name\":\"Ada\",\"mime_type\":\"text/plain\",\"extension\":\"txt\",\"content_hash\":\"h1\","
                + "\"size_bytes\":10,\"folder_path\":null}]}],\"total_groups\":1,\"total_wasted_bytes\":20,"
                + "\"scanning\":{\"pending\":4}"));

        DuplicatesResponse res = files().duplicates("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/duplicates");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(res.isScanEnabled()).isTrue();
        assertThat(res.getTotalGroups()).isEqualTo(1);
        assertThat(res.getTotalWastedBytes()).isEqualTo(20);
        assertThat(res.getPendingScans()).isEqualTo(4);
        DuplicatesResponse.Group group = res.getGroups().get(0);
        assertThat(group.getContentHash()).isEqualTo("h1");
        assertThat(group.getCount()).isEqualTo(3);
        assertThat(group.getWastedBytes()).isEqualTo(20);
        assertThat(group.getFiles().get(0).getFolderPath()).isNull();
        assertThat(group.getFiles().get(0).getUploaderName()).isEqualTo("Ada");
    }

    @Test
    void duplicatesWithScanDisabled() {
        enqueue(ok("\"scan_enabled\":false,\"groups\":[],\"total_groups\":0,\"total_wasted_bytes\":0,\"scanning\":{\"pending\":0}"));

        DuplicatesResponse res = files().duplicates("ws_1");

        assertThat(res.isScanEnabled()).isFalse();
        assertThat(res.getGroups()).isEmpty();
    }
}
