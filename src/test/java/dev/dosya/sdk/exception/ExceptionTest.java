package dev.dosya.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    // ---- DosyaException ----

    @Test
    void dosyaExceptionCarriesMessage() {
        DosyaException ex = new DosyaException("something went wrong");
        assertThat(ex.getMessage()).isEqualTo("something went wrong");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void dosyaExceptionCarriesMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        DosyaException ex = new DosyaException("wrapper", cause);
        assertThat(ex.getMessage()).isEqualTo("wrapper");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void dosyaExceptionIsRuntimeException() {
        DosyaException ex = new DosyaException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ---- DosyaApiException ----

    @Test
    void apiExceptionCarriesStatusAndErrorMessage() {
        DosyaApiException ex = new DosyaApiException(403, "Forbidden");
        assertThat(ex.getStatus()).isEqualTo(403);
        assertThat(ex.getErrorMessage()).isEqualTo("Forbidden");
        assertThat(ex.getRaw()).isNull();
        assertThat(ex.getRequestId()).isNull();
    }

    @Test
    void apiExceptionCarriesRawBody() {
        DosyaApiException ex = new DosyaApiException(400, "Bad request", "{\"error\":\"Bad request\"}");
        assertThat(ex.getStatus()).isEqualTo(400);
        assertThat(ex.getErrorMessage()).isEqualTo("Bad request");
        assertThat(ex.getRaw()).isEqualTo("{\"error\":\"Bad request\"}");
        assertThat(ex.getRequestId()).isNull();
    }

    @Test
    void apiExceptionCarriesRequestId() {
        DosyaApiException ex = new DosyaApiException(500, "Internal error", null, "req-abc-123");
        assertThat(ex.getStatus()).isEqualTo(500);
        assertThat(ex.getErrorMessage()).isEqualTo("Internal error");
        assertThat(ex.getRequestId()).isEqualTo("req-abc-123");
    }

    @Test
    void apiExceptionCarriesCause() {
        Exception cause = new Exception("parse error");
        DosyaApiException ex = new DosyaApiException(502, "Bad gateway", "raw body", "req-xyz", cause);
        assertThat(ex.getStatus()).isEqualTo(502);
        assertThat(ex.getErrorMessage()).isEqualTo("Bad gateway");
        assertThat(ex.getRaw()).isEqualTo("raw body");
        assertThat(ex.getRequestId()).isEqualTo("req-xyz");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void apiExceptionMessageIncludesStatusCode() {
        DosyaApiException ex = new DosyaApiException(404, "Not found");
        assertThat(ex.getMessage()).contains("[404]");
        assertThat(ex.getMessage()).contains("Not found");
    }

    @Test
    void apiExceptionMessageIncludesRequestIdWhenPresent() {
        DosyaApiException ex = new DosyaApiException(500, "Server error", null, "req-id-789");
        assertThat(ex.getMessage()).contains("request_id=req-id-789");
    }

    @Test
    void apiExceptionMessageOmitsRequestIdWhenNull() {
        DosyaApiException ex = new DosyaApiException(400, "Bad request", null, null);
        assertThat(ex.getMessage()).doesNotContain("request_id");
    }

    @Test
    void apiExceptionIsDosyaException() {
        DosyaApiException ex = new DosyaApiException(400, "err");
        assertThat(ex).isInstanceOf(DosyaException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ---- DosyaNetworkException ----

    @Test
    void networkExceptionCarriesMessage() {
        DosyaNetworkException ex = new DosyaNetworkException("connection refused");
        assertThat(ex.getMessage()).isEqualTo("connection refused");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void networkExceptionPreservesCause() {
        java.io.IOException cause = new java.io.IOException("timeout");
        DosyaNetworkException ex = new DosyaNetworkException("Request failed", cause);
        assertThat(ex.getMessage()).isEqualTo("Request failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void networkExceptionIsDosyaException() {
        DosyaNetworkException ex = new DosyaNetworkException("err");
        assertThat(ex).isInstanceOf(DosyaException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ---- DosyaUploadException ----

    @Test
    void uploadExceptionCarriesSessionId() {
        DosyaUploadException ex = new DosyaUploadException("upload failed", "session-123");
        assertThat(ex.getSessionId()).isEqualTo("session-123");
        assertThat(ex.getPartNumber()).isNull();
        assertThat(ex.getMessage()).isEqualTo("upload failed");
    }

    @Test
    void uploadExceptionCarriesPartNumber() {
        DosyaUploadException ex = new DosyaUploadException("part failed", "session-456", 3);
        assertThat(ex.getSessionId()).isEqualTo("session-456");
        assertThat(ex.getPartNumber()).isEqualTo(3);
        assertThat(ex.getMessage()).isEqualTo("part failed");
    }

    @Test
    void uploadExceptionCarriesCause() {
        java.io.IOException cause = new java.io.IOException("disk full");
        DosyaUploadException ex = new DosyaUploadException("upload error", "session-789", 5, cause);
        assertThat(ex.getSessionId()).isEqualTo("session-789");
        assertThat(ex.getPartNumber()).isEqualTo(5);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void uploadExceptionIsDosyaException() {
        DosyaUploadException ex = new DosyaUploadException("err", "sid");
        assertThat(ex).isInstanceOf(DosyaException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ---- Cause chain preservation ----

    @Test
    void causeChainIsPreservedAcrossMultipleLevels() {
        java.io.IOException root = new java.io.IOException("socket reset");
        DosyaNetworkException network = new DosyaNetworkException("network layer", root);
        DosyaUploadException upload = new DosyaUploadException("upload layer", "sid", 1, network);

        assertThat(upload.getCause()).isSameAs(network);
        assertThat(upload.getCause().getCause()).isSameAs(root);
    }
}
