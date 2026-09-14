package dev.dosya.sdk.internal;

import dev.dosya.sdk.model.UpdateFileRequestParams;
import dev.dosya.sdk.testing.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LenientDecodingTest extends ApiTestSupport {

    static final class Row {
        private int maxConcurrentUploads;
        private Integer shareMaxExpiryDays;
        private long sizeBytes;
        private Long expiresAt;
        private boolean isHidden;
    }

    @Test
    void truncatesStoredDecimalsInsteadOfFailingTheResponse() {
        enqueue(ok("\"row\":{\"max_concurrent_uploads\":3.0,\"share_max_expiry_days\":7.5,\"size_bytes\":\"42\",\"expires_at\":null,\"is_hidden\":1}"));
        DosyaHttpClient http = http();
        Row row = http.fromJson(http.request(HttpRequest.get("/api/x")).get("row"), Row.class);
        assertThat(row.maxConcurrentUploads).isEqualTo(3);
        assertThat(row.shareMaxExpiryDays).isEqualTo(7);
        assertThat(row.sizeBytes).isEqualTo(42L);
        assertThat(row.expiresAt).isNull();
        assertThat(row.isHidden).isTrue();
    }

    @Test
    void refusesABlankFileRequestPasswordInsteadOfRemovingIt() {
        assertThatThrownBy(() -> new UpdateFileRequestParams().password("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UpdateFileRequestParams().password(null)).isInstanceOf(NullPointerException.class);
        assertThat(new UpdateFileRequestParams().clearPassword().toBody()).containsEntry("password", "");
    }
}
