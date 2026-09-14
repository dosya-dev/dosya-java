package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.model.FavouriteTarget;
import dev.dosya.sdk.model.FavouritesList;
import dev.dosya.sdk.model.LockMode;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavouritesResourceTest extends ApiTestSupport {

    private FavouritesResource favourites() {
        return new FavouritesResource(http());
    }

    @Test
    void listSendsWorkspaceAndParsesFoldersAndFiles() throws Exception {
        enqueue(ok("\"folders\":[{\"id\":\"fav_1\",\"folder_id\":\"fld_1\",\"created_at\":3,\"folder_name\":\"Docs\",\"parent_id\":null}],"
                + "\"files\":[{\"id\":\"fav_2\",\"file_id\":\"file_1\",\"created_at\":4,\"file_name\":\"a.pdf\",\"size_bytes\":99,"
                + "\"mime_type\":\"application/pdf\",\"extension\":\"pdf\",\"lock_mode\":\"view_only\",\"current_version\":2,"
                + "\"folder_id\":\"fld_1\"}]"));

        FavouritesList list = favourites().list("ws_1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/favourites");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");

        FavouritesList.FavouriteFolder folder = list.getFolders().get(0);
        assertThat(folder.getId()).isEqualTo("fav_1");
        assertThat(folder.getFolderId()).isEqualTo("fld_1");
        assertThat(folder.getFolderName()).isEqualTo("Docs");
        assertThat(folder.getParentId()).isNull();
        assertThat(folder.getCreatedAt()).isEqualTo(3);

        FavouritesList.FavouriteFile file = list.getFiles().get(0);
        assertThat(file.getFileId()).isEqualTo("file_1");
        assertThat(file.getFileName()).isEqualTo("a.pdf");
        assertThat(file.getSizeBytes()).isEqualTo(99);
        assertThat(file.getLockMode()).isEqualTo(LockMode.VIEW_ONLY);
        assertThat(file.getCurrentVersion()).isEqualTo(2);
        assertThat(file.getFolderId()).isEqualTo("fld_1");
    }

    @Test
    void listToleratesMissingArrays() {
        enqueue(ok());
        FavouritesList list = favourites().list("ws_1");
        assertThat(list.getFolders()).isEmpty();
        assertThat(list.getFiles()).isEmpty();
    }

    @Test
    void addFolderSendsOnlyFolderId() throws Exception {
        enqueue(new MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("{\"ok\":true,\"id\":\"fav_9\"}"));

        assertThat(favourites().add(FavouriteTarget.folder("ws_1", "fld_1"))).isEqualTo("fav_9");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/favourites");
        JsonObject body = jsonBody(r);
        assertThat(body.get("workspace_id").getAsString()).isEqualTo("ws_1");
        assertThat(body.get("folder_id").getAsString()).isEqualTo("fld_1");
        assertThat(body.has("file_id")).isFalse();
    }

    @Test
    void addFileSendsOnlyFileId() throws Exception {
        enqueue(ok("\"id\":\"fav_9\""));

        favourites().add(FavouriteTarget.file("ws_1", "file_1"));

        JsonObject body = jsonBody(take());
        assertThat(body.get("file_id").getAsString()).isEqualTo("file_1");
        assertThat(body.has("folder_id")).isFalse();
    }

    @Test
    void removeUsesQueryParameters() throws Exception {
        enqueue(ok());

        favourites().remove(FavouriteTarget.file("ws_1", "file_1"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/favourites");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(r.getRequestUrl().queryParameter("file_id")).isEqualTo("file_1");
        assertThat(r.getRequestUrl().queryParameter("folder_id")).isNull();
        assertThat(r.getBodySize()).isZero();
    }

    @Test
    void targetRequiresAnId() {
        assertThatThrownBy(() -> FavouriteTarget.folder("ws_1", null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> FavouriteTarget.file(null, "file_1")).isInstanceOf(NullPointerException.class);
    }
}
