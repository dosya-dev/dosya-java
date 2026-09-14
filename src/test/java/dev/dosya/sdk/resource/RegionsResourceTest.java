package dev.dosya.sdk.resource;

import dev.dosya.sdk.model.RegionsListResponse;
import dev.dosya.sdk.testing.ApiTestSupport;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegionsResourceTest extends ApiTestSupport {

    @Test
    void listParsesRegionsAndSuggestion() throws Exception {
        enqueue(ok("\"regions\":[{\"code\":\"ap-southeast-2\",\"city\":\"Sydney\",\"country\":\"AU\","
                + "\"continent\":\"Oceania\",\"flag\":\"AU\"}],\"suggested\":\"ap-southeast-2\""));

        RegionsListResponse res = new RegionsResource(http()).list();

        RecordedRequest r = take();
        assertThat(r.getMethod()).isEqualTo("GET");
        assertThat(pathOf(r)).isEqualTo("/api/regions");
        assertThat(res.getRegions()).hasSize(1);
        assertThat(res.getRegions().get(0).getCity()).isEqualTo("Sydney");
        assertThat(res.getSuggested()).isEqualTo("ap-southeast-2");
    }
}
