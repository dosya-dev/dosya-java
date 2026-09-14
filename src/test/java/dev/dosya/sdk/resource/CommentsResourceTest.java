package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.model.CommentDetail;
import dev.dosya.sdk.model.CommentEditResult;
import dev.dosya.sdk.model.CreateCommentParams;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentsResourceTest extends ApiTestSupport {

    private static final String ROW = "{\"id\":\"cmt_1\",\"file_id\":\"f1\",\"folder_id\":null,\"workspace_id\":\"ws_1\",\"user_id\":\"u1\","
            + "\"parent_id\":%s,\"body\":\"hi\",\"is_edited\":1,\"created_at\":1,\"updated_at\":2,\"user_name\":\"U\","
            + "\"user_email\":\"u@x.y\",\"user_avatar\":null}";

    private CommentsResource comments() {
        return new CommentsResource(http());
    }

    @Test
    void listForFileSendsFileIdAndParsesRows() throws Exception {
        enqueue(ok("\"comments\":[" + String.format(ROW, "null") + "]"));

        List<CommentDetail> res = comments().listForFile("ws_1", "f1");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/comments");
        assertThat(r.getRequestUrl().queryParameterNames()).containsExactlyInAnyOrder("workspace_id", "file_id");
        assertThat(r.getRequestUrl().queryParameter("file_id")).isEqualTo("f1");
        CommentDetail c = res.get(0);
        assertThat(c.getId()).isEqualTo("cmt_1");
        assertThat(c.getFileId()).isEqualTo("f1");
        assertThat(c.getFolderId()).isNull();
        assertThat(c.getWorkspaceId()).isEqualTo("ws_1");
        assertThat(c.getParentId()).isNull();
        assertThat(c.isEdited()).isTrue();
        assertThat(c.getUpdatedAt()).isEqualTo(2);
        assertThat(c.getUserEmail()).isEqualTo("u@x.y");
        assertThat(c.getUserAvatar()).isNull();
    }

    @Test
    void listForFolderSendsFolderId() throws Exception {
        enqueue(ok("\"comments\":[]"));

        assertThat(comments().listForFolder("ws_1", "fld_1")).isEmpty();

        RecordedRequest r = take();
        assertThat(r.getRequestUrl().queryParameterNames()).containsExactlyInAnyOrder("workspace_id", "folder_id");
        assertThat(r.getRequestUrl().queryParameter("folder_id")).isEqualTo("fld_1");
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedListRequiresExactlyOneTarget() {
        assertThatThrownBy(() -> comments().list("ws_1", null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> comments().list("ws_1", "f", "d")).isInstanceOf(IllegalArgumentException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void createPostsOnlyAllowedKeys() throws Exception {
        enqueue(ok("\"comment\":" + String.format(ROW, "\"cmt_0\"")).setResponseCode(201));

        CommentDetail c = comments().create(CreateCommentParams.onFile("ws_1", "f1", "hi").parentId("cmt_0"));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("POST");
        assertThat(pathOf(r)).isEqualTo("/api/comments");
        JsonObject body = jsonBody(r);
        assertThat(body.keySet()).containsExactlyInAnyOrder("workspace_id", "file_id", "parent_id", "body");
        assertThat(body.get("file_id").getAsString()).isEqualTo("f1");
        assertThat(body.get("parent_id").getAsString()).isEqualTo("cmt_0");
        assertThat(body.get("body").getAsString()).isEqualTo("hi");
        assertThat(c.getParentId()).isEqualTo("cmt_0");
    }

    @Test
    void createOnFolderSendsFolderId() throws Exception {
        enqueue(ok("\"comment\":" + String.format(ROW, "null")));

        comments().create(CreateCommentParams.onFolder("ws_1", "fld_1", "hi"));

        JsonObject body = jsonBody(take());
        assertThat(body.keySet()).containsExactlyInAnyOrder("workspace_id", "folder_id", "body");
    }

    @Test
    @SuppressWarnings("deprecation")
    void createRefusesMissingOrDoubleTarget() {
        assertThatThrownBy(() -> comments().create(new CreateCommentParams("ws_1", "hi")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> comments().create(CreateCommentParams.onFile("ws_1", "f", "hi").folderId("d")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void editPutsBodyToEncodedId() throws Exception {
        enqueue(ok("\"body\":\"new\",\"updated_at\":7"));

        CommentEditResult res = comments().edit("cmt 1", "new");

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("PUT");
        assertThat(pathOf(r)).isEqualTo("/api/comments/cmt%201");
        assertThat(jsonBody(r).get("body").getAsString()).isEqualTo("new");
        assertThat(res.getBody()).isEqualTo("new");
        assertThat(res.getUpdatedAt()).isEqualTo(7);
    }

    @Test
    void deleteSendsDeleteAndIsNeverRetried() throws Exception {
        enqueue(ok());
        comments().delete("cmt_1");
        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("DELETE");
        assertThat(pathOf(r)).isEqualTo("/api/comments/cmt_1");

        enqueue(fail(500, "boom"), ok());
        CommentsResource retrying = new CommentsResource(new DosyaHttpClient(options().maxRetries(3)));
        assertThatThrownBy(() -> retrying.delete("cmt_1")).isInstanceOf(DosyaApiException.class);
        assertThat(server.getRequestCount()).isEqualTo(2);
    }
}
