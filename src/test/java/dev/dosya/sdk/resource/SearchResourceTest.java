package dev.dosya.sdk.resource;

import dev.dosya.sdk.model.LockMode;
import dev.dosya.sdk.model.SearchParams;
import dev.dosya.sdk.model.SearchResponse;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResourceTest extends ApiTestSupport {

    private static final String REPLY = "\"query\":\"ext:pdf report\",\"ext\":\"pdf\","
            + "\"files\":[{\"id\":\"f1\",\"name\":\"report.pdf\",\"size_bytes\":10,\"mime_type\":\"application/pdf\",\"extension\":\"pdf\","
            + "\"region\":\"apac\",\"folder_id\":null,\"uploaded_by\":\"u1\",\"created_at\":1,\"lock_mode\":\"none\",\"uploader_name\":\"U\"}],"
            + "\"folders\":[{\"id\":\"fld\",\"name\":\"reports\",\"parent_id\":null,\"file_count\":4,\"created_at\":2}],"
            + "\"shared\":[{\"link_id\":\"sl1\",\"token\":\"tok\",\"expires_at\":null,\"view_count\":0,\"download_count\":3,\"is_revoked\":0,"
            + "\"shared_at\":5,\"created_by\":\"u1\",\"folder_id\":null,\"is_bundle\":0,\"file_id\":\"f1\",\"file_name\":\"report.pdf\","
            + "\"size_bytes\":10,\"extension\":\"pdf\",\"region\":\"apac\",\"folder_name\":null,\"sharer_name\":\"U\",\"status\":\"active\"}],"
            + "\"file_requests\":[{\"id\":\"fr1\",\"title\":\"T\",\"message\":null,\"expires_at\":null,\"upload_count\":2,\"is_revoked\":0,"
            + "\"created_at\":3,\"created_by_name\":null}],"
            + "\"pagination\":{\"page\":2,\"per_page\":10,\"total_files\":11,\"total_folders\":0,\"total_shares\":1,\"total_requests\":1,\"has_more\":false}";

    @Test
    void querySendsSnakeCaseParamsAndParsesRealShape() throws Exception {
        enqueue(ok(REPLY));

        SearchResponse res = new SearchResource(http()).query(new SearchParams("ws_1", "ext:pdf report").page(2).perPage(10));

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/search");
        assertThat(r.getRequestUrl().queryParameter("workspace_id")).isEqualTo("ws_1");
        assertThat(r.getRequestUrl().queryParameter("q")).isEqualTo("ext:pdf report");
        assertThat(r.getRequestUrl().queryParameter("page")).isEqualTo("2");
        assertThat(r.getRequestUrl().queryParameter("per_page")).isEqualTo("10");

        assertThat(res.getQuery()).isEqualTo("ext:pdf report");
        assertThat(res.getExt()).isEqualTo("pdf");

        SearchResponse.SearchFile file = res.getFiles().get(0);
        assertThat(file.getFolderId()).isNull();
        assertThat(file.getUploadedBy()).isEqualTo("u1");
        assertThat(file.getUploaderName()).isEqualTo("U");
        assertThat(file.getLockMode()).isEqualTo(LockMode.NONE);
        assertThat(file.getRegion()).isEqualTo("apac");
        assertThat(file.getSizeBytes()).isEqualTo(10);

        SearchResponse.SearchFolder folder = res.getFolders().get(0);
        assertThat(folder.getFileCount()).isEqualTo(4);
        assertThat(folder.getParentId()).isNull();

        SearchResponse.SearchShared share = res.getShared().get(0);
        assertThat(share.getLinkId()).isEqualTo("sl1");
        assertThat(share.getSharedAt()).isEqualTo(5);
        assertThat(share.getStatus()).isEqualTo("active");
        assertThat(share.isBundle()).isFalse();
        assertThat(share.isRevoked()).isFalse();
        assertThat(share.getDownloadCount()).isEqualTo(3);
        assertThat(share.getExpiresAt()).isNull();

        SearchResponse.SearchFileRequest request = res.getFileRequests().get(0);
        assertThat(request.getUploadCount()).isEqualTo(2);
        assertThat(request.getCreatedByName()).isNull();
        assertThat(request.getTitle()).isEqualTo("T");

        assertThat(res.getPagination().getPage()).isEqualTo(2);
        assertThat(res.getPagination().getPerPage()).isEqualTo(10);
        assertThat(res.getPagination().getTotalFiles()).isEqualTo(11);
        assertThat(res.getPagination().getTotalFolders()).isZero();
        assertThat(res.getPagination().getTotalShares()).isEqualTo(1);
        assertThat(res.getPagination().getTotalRequests()).isEqualTo(1);
        assertThat(res.getPagination().hasMore()).isFalse();
    }

    @Test
    void regressionShareHitsHaveNoIdAndFileRequestsNoToken() throws Exception {
        // 0.2.x modelled share hits with `id` and file-request hits with `token`; the API sends neither.
        assertThat(SearchResponse.SearchShared.class.getDeclaredMethods())
                .extracting("name").doesNotContain("getId").contains("getLinkId");
        assertThat(SearchResponse.SearchFileRequest.class.getDeclaredMethods())
                .extracting("name").doesNotContain("getToken");
    }

    @Test
    void shortOverloadOmitsPaging() throws Exception {
        enqueue(ok("\"query\":\"a\",\"ext\":null,\"files\":[],\"folders\":[],\"shared\":[],\"file_requests\":[],"
                + "\"pagination\":{\"page\":1,\"per_page\":50,\"total_files\":0,\"total_folders\":0,\"total_shares\":0,\"total_requests\":0,\"has_more\":false}"));

        SearchResponse res = new SearchResource(http()).query("ws_1", "a");

        assertThat(take().getRequestUrl().queryParameterNames()).containsExactlyInAnyOrder("workspace_id", "q");
        assertThat(res.getExt()).isNull();
        assertThat(res.getFiles()).isEmpty();
    }
}
