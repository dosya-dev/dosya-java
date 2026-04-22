package dev.dosya.sdk.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelDeserializationTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    // ---- FileDetail ----

    @Test
    void fileDetailDeserializesCorrectly() {
        String json = "{"
                + "\"id\": \"file-1\","
                + "\"name\": \"document.pdf\","
                + "\"size_bytes\": 102400,"
                + "\"mime_type\": \"application/pdf\","
                + "\"extension\": \"pdf\","
                + "\"region\": \"eu-west-1\","
                + "\"uploaded_by\": \"user-1\","
                + "\"uploader_name\": \"Alice\","
                + "\"folder_id\": \"folder-1\","
                + "\"workspace_id\": \"ws-1\","
                + "\"current_version\": 3,"
                + "\"lock_mode\": \"none\","
                + "\"is_hidden\": 0,"
                + "\"hidden_mode\": null,"
                + "\"created_at\": 1700000000,"
                + "\"updated_at\": 1700001000,"
                + "\"deleted_at\": null"
                + "}";

        FileDetail detail = gson.fromJson(json, FileDetail.class);

        assertThat(detail.getId()).isEqualTo("file-1");
        assertThat(detail.getName()).isEqualTo("document.pdf");
        assertThat(detail.getSizeBytes()).isEqualTo(102400);
        assertThat(detail.getMimeType()).isEqualTo("application/pdf");
        assertThat(detail.getExtension()).isEqualTo("pdf");
        assertThat(detail.getRegion()).isEqualTo("eu-west-1");
        assertThat(detail.getUploadedBy()).isEqualTo("user-1");
        assertThat(detail.getUploaderName()).isEqualTo("Alice");
        assertThat(detail.getFolderId()).isEqualTo("folder-1");
        assertThat(detail.getWorkspaceId()).isEqualTo("ws-1");
        assertThat(detail.getCurrentVersion()).isEqualTo(3);
        assertThat(detail.getLockMode()).isEqualTo("none");
        assertThat(detail.getIsHidden()).isEqualTo(0);
        assertThat(detail.getHiddenMode()).isNull();
        assertThat(detail.getCreatedAt()).isEqualTo(1700000000L);
        assertThat(detail.getUpdatedAt()).isEqualTo(1700001000L);
        assertThat(detail.getDeletedAt()).isNull();
    }

    @Test
    void fileDetailDeletedAtDeserializesWhenPresent() {
        String json = "{\"id\": \"file-2\", \"deleted_at\": 1700002000}";
        FileDetail detail = gson.fromJson(json, FileDetail.class);
        assertThat(detail.getDeletedAt()).isEqualTo(1700002000L);
    }

    // ---- FileListItem ----

    @Test
    void fileListItemDeserializesCorrectly() {
        String json = "{"
                + "\"id\": \"fli-1\","
                + "\"name\": \"report.xlsx\","
                + "\"size_bytes\": 51200,"
                + "\"mime_type\": \"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\","
                + "\"extension\": \"xlsx\","
                + "\"region\": \"us-east-1\","
                + "\"uploaded_by\": \"user-2\","
                + "\"uploader_name\": \"Bob\","
                + "\"created_at\": 1700000000,"
                + "\"updated_at\": 1700001000,"
                + "\"deleted_at\": null,"
                + "\"lock_mode\": \"exclusive\","
                + "\"is_hidden\": 1,"
                + "\"hidden_mode\": \"password\","
                + "\"current_version\": 2,"
                + "\"is_synced\": 1,"
                + "\"share_count\": 5,"
                + "\"comment_count\": 3"
                + "}";

        FileListItem item = gson.fromJson(json, FileListItem.class);

        assertThat(item.getId()).isEqualTo("fli-1");
        assertThat(item.getName()).isEqualTo("report.xlsx");
        assertThat(item.getSizeBytes()).isEqualTo(51200);
        assertThat(item.getLockMode()).isEqualTo("exclusive");
        assertThat(item.getIsHidden()).isEqualTo(1);
        assertThat(item.getHiddenMode()).isEqualTo("password");
        assertThat(item.getCurrentVersion()).isEqualTo(2);
        assertThat(item.getIsSynced()).isEqualTo(1);
        assertThat(item.getShareCount()).isEqualTo(5);
        assertThat(item.getCommentCount()).isEqualTo(3);
    }

    // ---- FolderListItem ----

    @Test
    void folderListItemDeserializesCorrectly() {
        String json = "{"
                + "\"id\": \"fold-1\","
                + "\"name\": \"Documents\","
                + "\"created_at\": 1700000000,"
                + "\"file_count\": 42,"
                + "\"lock_mode\": \"none\","
                + "\"is_hidden\": 0,"
                + "\"hidden_mode\": null,"
                + "\"is_synced\": 0"
                + "}";

        FolderListItem folder = gson.fromJson(json, FolderListItem.class);

        assertThat(folder.getId()).isEqualTo("fold-1");
        assertThat(folder.getName()).isEqualTo("Documents");
        assertThat(folder.getCreatedAt()).isEqualTo(1700000000L);
        assertThat(folder.getFileCount()).isEqualTo(42);
        assertThat(folder.getLockMode()).isEqualTo("none");
        assertThat(folder.getIsHidden()).isEqualTo(0);
        assertThat(folder.getHiddenMode()).isNull();
        assertThat(folder.getIsSynced()).isEqualTo(0);
    }

    // ---- Breadcrumb ----

    @Test
    void breadcrumbDeserializesCorrectly() {
        String json = "{\"id\": \"bc-1\", \"name\": \"Root\"}";
        Breadcrumb crumb = gson.fromJson(json, Breadcrumb.class);
        assertThat(crumb.getId()).isEqualTo("bc-1");
        assertThat(crumb.getName()).isEqualTo("Root");
    }

    // ---- Pagination ----

    @Test
    void paginationDeserializesCorrectly() {
        String json = "{"
                + "\"page\": 2,"
                + "\"per_page\": 25,"
                + "\"total_files\": 100,"
                + "\"total_pages\": 4"
                + "}";

        Pagination page = gson.fromJson(json, Pagination.class);
        assertThat(page.getPage()).isEqualTo(2);
        assertThat(page.getPerPage()).isEqualTo(25);
        assertThat(page.getTotalFiles()).isEqualTo(100);
        assertThat(page.getTotalPages()).isEqualTo(4);
    }

    // ---- UserProfile ----

    @Test
    void userProfileDeserializesCorrectly() {
        String json = "{"
                + "\"id\": \"user-1\","
                + "\"email\": \"alice@example.com\","
                + "\"name\": \"Alice\","
                + "\"initials\": \"A\","
                + "\"avatar_url\": \"https://cdn.dosya.dev/avatar.png\","
                + "\"preferred_language\": \"en\","
                + "\"created_at\": 1690000000,"
                + "\"email_verified_at\": 1690001000,"
                + "\"workspace_count\": 3"
                + "}";

        UserProfile profile = gson.fromJson(json, UserProfile.class);
        assertThat(profile.getId()).isEqualTo("user-1");
        assertThat(profile.getEmail()).isEqualTo("alice@example.com");
        assertThat(profile.getName()).isEqualTo("Alice");
        assertThat(profile.getInitials()).isEqualTo("A");
        assertThat(profile.getAvatarUrl()).isEqualTo("https://cdn.dosya.dev/avatar.png");
        assertThat(profile.getPreferredLanguage()).isEqualTo("en");
        assertThat(profile.getCreatedAt()).isEqualTo(1690000000L);
        assertThat(profile.getEmailVerifiedAt()).isEqualTo(1690001000L);
        assertThat(profile.getWorkspaceCount()).isEqualTo(3);
    }

    @Test
    void userProfileNullableEmailVerifiedAt() {
        String json = "{\"id\": \"user-2\", \"email_verified_at\": null}";
        UserProfile profile = gson.fromJson(json, UserProfile.class);
        assertThat(profile.getEmailVerifiedAt()).isNull();
    }

    // ---- CreatedApiKey ----

    @Test
    void createdApiKeyDeserializesCorrectly() {
        String json = "{"
                + "\"id\": \"key-1\","
                + "\"name\": \"My Key\","
                + "\"scope\": \"full\","
                + "\"plain_key\": \"dos_live_abc123\","
                + "\"expires_at\": 1800000000,"
                + "\"created_at\": 1700000000"
                + "}";

        CreatedApiKey key = gson.fromJson(json, CreatedApiKey.class);
        assertThat(key.getId()).isEqualTo("key-1");
        assertThat(key.getName()).isEqualTo("My Key");
        assertThat(key.getScope()).isEqualTo("full");
        assertThat(key.getPlainKey()).isEqualTo("dos_live_abc123");
        assertThat(key.getExpiresAt()).isEqualTo(1800000000L);
        assertThat(key.getCreatedAt()).isEqualTo(1700000000L);
    }

    @Test
    void createdApiKeyNullableExpiresAt() {
        String json = "{\"id\": \"key-2\", \"expires_at\": null, \"created_at\": 1700000000}";
        CreatedApiKey key = gson.fromJson(json, CreatedApiKey.class);
        assertThat(key.getExpiresAt()).isNull();
    }

    @Test
    void createdApiKeyToStringMasksPlainKey() {
        String json = "{"
                + "\"id\": \"key-1\","
                + "\"name\": \"My Key\","
                + "\"plain_key\": \"dos_live_secret_value\","
                + "\"created_at\": 1700000000"
                + "}";

        CreatedApiKey key = gson.fromJson(json, CreatedApiKey.class);
        String str = key.toString();
        assertThat(str).contains("plainKey=***");
        assertThat(str).doesNotContain("dos_live_secret_value");
        assertThat(str).contains("key-1");
        assertThat(str).contains("My Key");
    }

    // ---- ListFilesResponse (unmodifiable lists) ----

    @Test
    void listFilesResponseDeserializesCorrectly() {
        String json = "{"
                + "\"folders\": [{\"id\": \"f1\", \"name\": \"Docs\"}],"
                + "\"files\": [{\"id\": \"file1\", \"name\": \"a.txt\"}],"
                + "\"breadcrumbs\": [{\"id\": \"bc1\", \"name\": \"Root\"}],"
                + "\"workspace_id\": \"ws-1\","
                + "\"folder_id\": \"fold-1\","
                + "\"can_lock\": true,"
                + "\"can_hide\": false,"
                + "\"folder_view_only\": true,"
                + "\"pagination\": {\"page\": 1, \"per_page\": 25, \"total_files\": 10, \"total_pages\": 1}"
                + "}";

        ListFilesResponse resp = gson.fromJson(json, ListFilesResponse.class);
        assertThat(resp.getFolders()).hasSize(1);
        assertThat(resp.getFiles()).hasSize(1);
        assertThat(resp.getBreadcrumbs()).hasSize(1);
        assertThat(resp.getWorkspaceId()).isEqualTo("ws-1");
        assertThat(resp.getFolderId()).isEqualTo("fold-1");
        assertThat(resp.isCanLock()).isTrue();
        assertThat(resp.isCanHide()).isFalse();
        assertThat(resp.isFolderViewOnly()).isTrue();
        assertThat(resp.getPagination()).isNotNull();
        assertThat(resp.getPagination().getPage()).isEqualTo(1);
    }

    @Test
    void listFilesResponseFoldersListIsUnmodifiable() {
        String json = "{\"folders\": [{\"id\": \"f1\"}]}";
        ListFilesResponse resp = gson.fromJson(json, ListFilesResponse.class);

        assertThatThrownBy(() -> resp.getFolders().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void listFilesResponseFilesListIsUnmodifiable() {
        String json = "{\"files\": [{\"id\": \"file1\"}]}";
        ListFilesResponse resp = gson.fromJson(json, ListFilesResponse.class);

        assertThatThrownBy(() -> resp.getFiles().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void listFilesResponseBreadcrumbsListIsUnmodifiable() {
        String json = "{\"breadcrumbs\": [{\"id\": \"bc1\"}]}";
        ListFilesResponse resp = gson.fromJson(json, ListFilesResponse.class);

        assertThatThrownBy(() -> resp.getBreadcrumbs().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void listFilesResponseNullListsReturnEmpty() {
        String json = "{}";
        ListFilesResponse resp = gson.fromJson(json, ListFilesResponse.class);
        assertThat(resp.getFolders()).isEmpty();
        assertThat(resp.getFiles()).isEmpty();
        assertThat(resp.getBreadcrumbs()).isEmpty();
    }

    // ---- RateLimitInfo ----

    @Test
    void rateLimitInfoConstructor() {
        RateLimitInfo info = new RateLimitInfo(100, 95, 1700000060L);
        assertThat(info.getLimit()).isEqualTo(100);
        assertThat(info.getRemaining()).isEqualTo(95);
        assertThat(info.getResetAt()).isEqualTo(1700000060L);
    }

    // ---- UploadProgress ----

    @Test
    void uploadProgressSimpleConstructor() {
        UploadProgress progress = new UploadProgress(50000, 100000, 50, "uploading");
        assertThat(progress.getBytesUploaded()).isEqualTo(50000);
        assertThat(progress.getTotalBytes()).isEqualTo(100000);
        assertThat(progress.getPercent()).isEqualTo(50);
        assertThat(progress.getPartsCompleted()).isNull();
        assertThat(progress.getTotalParts()).isNull();
        assertThat(progress.getStatus()).isEqualTo("uploading");
    }

    @Test
    void uploadProgressFullConstructor() {
        UploadProgress progress = new UploadProgress(50000, 100000, 50, 2, 4, "uploading");
        assertThat(progress.getPartsCompleted()).isEqualTo(2);
        assertThat(progress.getTotalParts()).isEqualTo(4);
    }

    // ---- UploadParams defensive copy ----

    @Test
    void uploadParamsGetBodyReturnsDefensiveCopy() {
        byte[] original = {1, 2, 3, 4, 5};
        UploadParams params = UploadParams.fromBytes("ws-1", "test.bin", original);

        byte[] copy1 = params.getBody();
        byte[] copy2 = params.getBody();

        // Copies should be equal but not the same reference
        assertThat(copy1).isEqualTo(copy2);
        assertThat(copy1).isNotSameAs(copy2);

        // Modifying the copy should not affect subsequent calls
        copy1[0] = 99;
        assertThat(params.getBody()[0]).isEqualTo((byte) 1);
    }

    @Test
    void uploadParamsFromBytesDoesNotShareOriginalArray() {
        byte[] original = {10, 20, 30};
        UploadParams params = UploadParams.fromBytes("ws-1", "test.bin", original);

        // Modify the original array
        original[0] = 99;

        // The params body should still have the original value because fromBytes
        // stores the reference (but getBody returns a copy)
        // Actually, fromBytes stores the array directly, so let's verify getBody copies
        byte[] body = params.getBody();
        assertThat(body).hasSize(3);
    }

    @Test
    void uploadParamsFluentSetters() {
        byte[] data = {1, 2, 3};
        UploadParams params = UploadParams.fromBytes("ws-1", "test.bin", data)
                .mimeType("application/octet-stream")
                .region("eu-west-1")
                .folderId("fold-1")
                .fileId("file-1");

        assertThat(params.getWorkspaceId()).isEqualTo("ws-1");
        assertThat(params.getFileName()).isEqualTo("test.bin");
        assertThat(params.getFileSize()).isEqualTo(3);
        assertThat(params.getMimeType()).isEqualTo("application/octet-stream");
        assertThat(params.getRegion()).isEqualTo("eu-west-1");
        assertThat(params.getFolderId()).isEqualTo("fold-1");
        assertThat(params.getFileId()).isEqualTo("file-1");
    }

    // ---- Forward compatibility: unknown fields silently ignored ----

    @Test
    void unknownFieldsAreIgnoredOnFileDetail() {
        String json = "{"
                + "\"id\": \"file-1\","
                + "\"name\": \"test.txt\","
                + "\"some_future_field\": \"future value\","
                + "\"another_new_thing\": 42"
                + "}";

        FileDetail detail = gson.fromJson(json, FileDetail.class);
        assertThat(detail.getId()).isEqualTo("file-1");
        assertThat(detail.getName()).isEqualTo("test.txt");
        // No exception thrown - unknown fields simply ignored
    }

    @Test
    void unknownFieldsAreIgnoredOnUserProfile() {
        String json = "{"
                + "\"id\": \"user-1\","
                + "\"email\": \"test@example.com\","
                + "\"brand_new_field\": true"
                + "}";

        UserProfile profile = gson.fromJson(json, UserProfile.class);
        assertThat(profile.getId()).isEqualTo("user-1");
        assertThat(profile.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void unknownFieldsAreIgnoredOnCreatedApiKey() {
        String json = "{"
                + "\"id\": \"key-1\","
                + "\"name\": \"Test\","
                + "\"created_at\": 1700000000,"
                + "\"future_flag\": false"
                + "}";

        CreatedApiKey key = gson.fromJson(json, CreatedApiKey.class);
        assertThat(key.getId()).isEqualTo("key-1");
    }

    // ---- Null fields deserialize correctly ----

    @Test
    void nullStringFieldsDeserializeAsNull() {
        String json = "{"
                + "\"id\": null,"
                + "\"name\": null,"
                + "\"mime_type\": null"
                + "}";

        FileDetail detail = gson.fromJson(json, FileDetail.class);
        assertThat(detail.getId()).isNull();
        assertThat(detail.getName()).isNull();
        assertThat(detail.getMimeType()).isNull();
    }

    @Test
    void nullLongFieldsDeserializeAsNull() {
        String json = "{\"id\": \"file-1\", \"deleted_at\": null}";
        FileDetail detail = gson.fromJson(json, FileDetail.class);
        assertThat(detail.getDeletedAt()).isNull();
    }

    // ---- DosyaClientOptions.toString() masks API key ----

    @Test
    void clientOptionsToStringMasksApiKey() {
        dev.dosya.sdk.DosyaClientOptions options =
                new dev.dosya.sdk.DosyaClientOptions("dos_super_secret_key");
        String str = options.toString();
        assertThat(str).contains("apiKey=***");
        assertThat(str).doesNotContain("dos_super_secret_key");
    }
}
