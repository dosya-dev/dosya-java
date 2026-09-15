package dev.dosya.sdk.webhook;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.exception.DosyaWebhookSignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookSignatureTest {

    // ── The server's algorithm, reimplemented from apps/api/src/lib/webhooks/sign.ts ──
    // hmacHex(secret, message): HMAC-SHA256 keyed by UTF-8(secret) over UTF-8(message), lowercase hex.
    // signatureHeader(secret, body, t) = "t=" + t + ",v1=" + hmacHex(secret, t + "." + body).
    private static String hmacHex(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sig = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : sig) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static String signatureHeader(String secret, String body, long t) throws Exception {
        return "t=" + t + ",v1=" + hmacHex(secret, t + "." + body);
    }

    private static final String SECRET = "whsec_0123456789abcdefghijklmnopqrstuv";
    private static final long T = 1760000000L;
    // Same shape and serialization as the /test route's payload.
    private static final String BODY = "{\"id\":\"evt_abc\",\"type\":\"file.uploaded\",\"created\":" + T
            + ",\"workspace_id\":\"ws_1\",\"data\":{\"file_id\":\"test_file\",\"name\":\"test.txt\",\"size\":12,"
            + "\"folder_id\":null,\"version\":1,\"is_new_version\":false,\"test\":true}}";

    @Test
    void knownVector() throws Exception {
        // Fixed vector, so the reimplementation above cannot drift together with the SDK.
        assertThat(hmacHex("key", "The quick brown fox jumps over the lazy dog"))
                .isEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
    }

    @Test
    void acceptsHeaderProducedByServerSigningCode() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 300, T)).isTrue();
        assertThat(WebhookSignature.verify(BODY.getBytes(StandardCharsets.UTF_8), header, SECRET, 300, T)).isTrue();
    }

    @Test
    void keyIncludesWhsecPrefix() throws Exception {
        String stripped = hmacHex(SECRET.substring("whsec_".length()), T + "." + BODY);
        assertThat(WebhookSignature.verify(BODY, "t=" + T + ",v1=" + stripped, SECRET, 300, T)).isFalse();
    }

    @Test
    void acceptsNonAsciiBodiesAsStringOrBytes() throws Exception {
        String body = "{\"id\":\"evt_1\",\"type\":\"file.deleted\",\"created\":" + T
                + ",\"workspace_id\":\"ws_1\",\"data\":{\"file_id\":\"f\",\"name\":\"rapor-şubat-日本.pdf\",\"permanent\":false}}";
        String header = signatureHeader(SECRET, body, T);
        assertThat(WebhookSignature.verify(body, header, SECRET, 300, T)).isTrue();
        assertThat(WebhookSignature.verify(body.getBytes(StandardCharsets.UTF_8), header, SECRET, 300, T)).isTrue();
    }

    @Test
    void rejectsTamperedBody() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        assertThat(WebhookSignature.verify(BODY.replace("\"size\":12", "\"size\":13"), header, SECRET, 300, T)).isFalse();
    }

    @Test
    void rejectsReserializedJson() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        String pretty = new com.google.gson.GsonBuilder().setPrettyPrinting().create()
                .toJson(JsonParser.parseString(BODY));
        assertThat(WebhookSignature.verify(pretty, header, SECRET, 300, T)).isFalse();
    }

    @Test
    void rejectsWrongSecret() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        assertThat(WebhookSignature.verify(BODY, header, "whsec_other", 300, T)).isFalse();
    }

    @Test
    void rejectsTimestampSwappedIntoValidSignature() throws Exception {
        String moved = signatureHeader(SECRET, BODY, T).replace("t=" + T, "t=" + (T + 1));
        assertThat(WebhookSignature.verify(BODY, moved, SECRET, 300, T)).isFalse();
    }

    @Test
    void enforcesToleranceInBothDirections() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 300, T + 300)).isTrue();
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 300, T + 301)).isFalse();
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 300, T - 301)).isFalse();
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 1000, T + 1000)).isTrue();
        assertThat(WebhookSignature.verify(BODY, header, SECRET, Long.MAX_VALUE, T + 1_000_000_000L)).isTrue();
    }

    @Test
    void defaultsToSystemClockAnd300sTolerance() throws Exception {
        long now = System.currentTimeMillis() / 1000;
        assertThat(WebhookSignature.verify(BODY, signatureHeader(SECRET, BODY, now), SECRET)).isTrue();
        assertThat(WebhookSignature.verify(BODY.getBytes(StandardCharsets.UTF_8), signatureHeader(SECRET, BODY, now), SECRET)).isTrue();
        assertThat(WebhookSignature.verify(BODY, signatureHeader(SECRET, BODY, now - 3600), SECRET)).isFalse();
        assertThat(WebhookSignature.verify(BODY, signatureHeader(SECRET, BODY, now - 3600), SECRET, 7200)).isTrue();
        assertThat(WebhookSignature.DEFAULT_TOLERANCE_SECONDS).isEqualTo(300);
    }

    @Test
    void toleratesWhitespaceExtraFieldsAndAcceptsAnyMatchingV1() throws Exception {
        String hex = signatureHeader(SECRET, BODY, T).split("v1=")[1];
        String header = " t=" + T + " , v0=zzz, v1=" + "0".repeat(64) + ", v1=" + hex.toUpperCase() + " ";
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 300, T)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "", "   ", "garbage",
            "v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "t=abc,v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "t=-1760000000,v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "t=99999999999999999999999,v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "t=1760000000",
            "t=1760000000,v1=nothex",
            "t=1760000000,v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    })
    void returnsFalseForMalformedHeader(String header) {
        assertThat(WebhookSignature.verify(BODY, header, SECRET, 300, T)).isFalse();
    }

    @Test
    void rejectsMissingSecretOrPayload() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        assertThatThrownBy(() -> WebhookSignature.verify(BODY, header, "", 300, T)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebhookSignature.verify(BODY, header, null, 300, T)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebhookSignature.verify((String) null, header, SECRET, 300, T)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WebhookSignature.verify((byte[]) null, header, SECRET, 300, T)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructEventReturnsEventSnakeCaseUntouched() throws Exception {
        WebhookEvent event = WebhookSignature.constructEvent(BODY, signatureHeader(SECRET, BODY, T), SECRET, 300, T);
        assertThat(event.getId()).isEqualTo("evt_abc");
        assertThat(event.getType()).isEqualTo("file.uploaded");
        assertThat(event.getEventType()).isEqualTo(WebhookEventType.FILE_UPLOADED);
        assertThat(event.getCreated()).isEqualTo(T);
        assertThat(event.getWorkspaceId()).isEqualTo("ws_1");
        JsonObject data = event.getData();
        assertThat(data.get("is_new_version").getAsBoolean()).isFalse();
        assertThat(data.get("size").getAsLong()).isEqualTo(12L);
        assertThat(event.getFileId()).isEqualTo("test_file");
        assertThat(event.getFolderId()).isNull();
        assertThat(event.isTest()).isTrue();
        assertThat(event.getJson().get("workspace_id").getAsString()).isEqualTo("ws_1");
    }

    @Test
    void constructEventParsesBytePayloads() throws Exception {
        WebhookEvent event = WebhookSignature.constructEvent(BODY.getBytes(StandardCharsets.UTF_8),
                signatureHeader(SECRET, BODY, T), SECRET, 300, T);
        assertThat(event.getId()).isEqualTo("evt_abc");
    }

    @Test
    void constructEventThrowsForMissingHeader() {
        assertThatThrownBy(() -> WebhookSignature.constructEvent(BODY, null, SECRET, 300, T))
                .isInstanceOf(DosyaWebhookSignatureException.class)
                .hasMessageContaining("Missing X-Dosya-Signature");
    }

    @Test
    void constructEventThrowsForMalformedHeader() {
        assertThatThrownBy(() -> WebhookSignature.constructEvent(BODY, "t=,v1=", SECRET, 300, T))
                .isInstanceOf(DosyaWebhookSignatureException.class)
                .hasMessageContaining("Malformed")
                .hasMessageContaining("timestamp");
        assertThatThrownBy(() -> WebhookSignature.constructEvent(BODY, "t=" + T + ",v1=", SECRET, 300, T))
                .isInstanceOf(DosyaWebhookSignatureException.class)
                .hasMessageContaining("Malformed")
                .hasMessageContaining("v1 signature");
    }

    @Test
    void constructEventThrowsForStaleTimestamp() throws Exception {
        String header = signatureHeader(SECRET, BODY, T);
        assertThatThrownBy(() -> WebhookSignature.constructEvent(BODY, header, SECRET, 300, T + 3600))
                .isInstanceOf(DosyaWebhookSignatureException.class)
                .hasMessageContaining("outside the tolerance of 300s");
    }

    @Test
    void constructEventThrowsForMismatch() {
        assertThatThrownBy(() -> WebhookSignature.constructEvent(BODY, "t=" + T + ",v1=" + "b".repeat(64), SECRET, 300, T))
                .isInstanceOf(DosyaWebhookSignatureException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void constructEventThrowsPlainDosyaExceptionForSignedNonJson() throws Exception {
        String header = signatureHeader(SECRET, "not json", T);
        assertThatThrownBy(() -> WebhookSignature.constructEvent("not json", header, SECRET, 300, T))
                .isInstanceOf(DosyaException.class)
                .isNotInstanceOf(DosyaWebhookSignatureException.class)
                .hasMessageContaining("not valid JSON");
        String arrayHeader = signatureHeader(SECRET, "[1]", T);
        assertThatThrownBy(() -> WebhookSignature.constructEvent("[1]", arrayHeader, SECRET, 300, T))
                .isInstanceOf(DosyaException.class)
                .isNotInstanceOf(DosyaWebhookSignatureException.class);
    }

    @Test
    void headerConstants() {
        assertThat(WebhookHeaders.SIGNATURE).isEqualTo("X-Dosya-Signature");
        assertThat(WebhookHeaders.EVENT_ID).isEqualTo("X-Dosya-Event-Id");
        assertThat(WebhookHeaders.EVENT_TYPE).isEqualTo("X-Dosya-Event-Type");
        assertThat(WebhookHeaders.DELIVERY_ID).isEqualTo("X-Dosya-Delivery-Id");
    }
}
