package dev.dosya.sdk.webhook;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.exception.DosyaWebhookSignatureException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Webhook signature verification. Standalone: needs no {@code DosyaClient} or API key.
 *
 * <p>Protocol: dosya POSTs the event JSON with
 * {@code X-Dosya-Signature: t=<unix seconds>,v1=<hex HMAC-SHA256>}, where the HMAC key is
 * the UTF-8 bytes of the whole secret (including the {@code whsec_} prefix) and the message
 * is {@code <t>.<raw body>}. {@code t} is the delivery attempt time, re-signed on every retry.
 * The header may carry several {@code v1} values; any match is accepted.
 *
 * <p>Always pass the RAW request body exactly as received. A re-serialized body will not verify.
 *
 * <pre>{@code
 * WebhookEvent event = WebhookSignature.constructEvent(
 *     rawBody, request.getHeader(WebhookHeaders.SIGNATURE), secret);
 * }</pre>
 *
 * @since 0.3.0
 */
public final class WebhookSignature {

    /** Default maximum age (and clock skew) of the signature timestamp, in seconds. */
    public static final long DEFAULT_TOLERANCE_SECONDS = 300;

    private static final Pattern DIGITS = Pattern.compile("^[0-9]+$");
    private static final Pattern HEX64 = Pattern.compile("^[0-9a-fA-F]{64}$");

    private WebhookSignature() {}

    // ── verify ──

    /**
     * Checks a delivery's signature with the default tolerance of 300 seconds and the system clock.
     * Returns {@code true} only when the header is well formed, its timestamp is within tolerance
     * and an HMAC matches. Never throws for a bad signature; use {@link #constructEvent} for the reason.
     *
     * @param payload the raw request body
     * @param header  the {@code X-Dosya-Signature} header value, may be null
     * @param secret  the endpoint's {@code whsec_...} secret
     * @throws IllegalArgumentException if {@code payload} is null or {@code secret} is null or empty
     */
    public static boolean verify(@NotNull byte[] payload, @Nullable String header, @NotNull String secret) {
        return verify(payload, header, secret, DEFAULT_TOLERANCE_SECONDS, nowSeconds());
    }

    /** Same as {@link #verify(byte[], String, String)} for a body read as a string (UTF-8). */
    public static boolean verify(@NotNull String payload, @Nullable String header, @NotNull String secret) {
        return verify(bytes(payload), header, secret, DEFAULT_TOLERANCE_SECONDS, nowSeconds());
    }

    /**
     * Checks a delivery's signature with a custom tolerance.
     *
     * @param toleranceSeconds maximum age and clock skew in seconds; {@link Long#MAX_VALUE} skips the check (not recommended)
     */
    public static boolean verify(@NotNull byte[] payload, @Nullable String header, @NotNull String secret,
                                 long toleranceSeconds) {
        return verify(payload, header, secret, toleranceSeconds, nowSeconds());
    }

    /** String-body variant of {@link #verify(byte[], String, String, long)}. */
    public static boolean verify(@NotNull String payload, @Nullable String header, @NotNull String secret,
                                 long toleranceSeconds) {
        return verify(bytes(payload), header, secret, toleranceSeconds, nowSeconds());
    }

    /**
     * Checks a delivery's signature against an explicit clock (for tests).
     *
     * @param nowSeconds the current time in unix seconds
     */
    public static boolean verify(@NotNull byte[] payload, @Nullable String header, @NotNull String secret,
                                 long toleranceSeconds, long nowSeconds) {
        return problem(payload, header, secret, toleranceSeconds, nowSeconds) == null;
    }

    /** String-body variant of {@link #verify(byte[], String, String, long, long)}. */
    public static boolean verify(@NotNull String payload, @Nullable String header, @NotNull String secret,
                                 long toleranceSeconds, long nowSeconds) {
        return verify(bytes(payload), header, secret, toleranceSeconds, nowSeconds);
    }

    // ── constructEvent ──

    /**
     * Verifies a delivery (default tolerance, system clock) and parses its body.
     *
     * @return the event, snake_case keys untouched
     * @throws DosyaWebhookSignatureException for a missing or malformed header, a timestamp
     *         outside tolerance, or a signature mismatch
     * @throws DosyaException when a validly signed body is not a JSON object
     * @throws IllegalArgumentException if {@code payload} is null or {@code secret} is null or empty
     */
    public static @NotNull WebhookEvent constructEvent(@NotNull byte[] payload, @Nullable String header,
                                                       @NotNull String secret) {
        return constructEvent(payload, header, secret, DEFAULT_TOLERANCE_SECONDS, nowSeconds());
    }

    /** String-body variant of {@link #constructEvent(byte[], String, String)}. */
    public static @NotNull WebhookEvent constructEvent(@NotNull String payload, @Nullable String header,
                                                       @NotNull String secret) {
        return constructEvent(bytes(payload), header, secret, DEFAULT_TOLERANCE_SECONDS, nowSeconds());
    }

    /** {@link #constructEvent(byte[], String, String)} with a custom tolerance in seconds. */
    public static @NotNull WebhookEvent constructEvent(@NotNull byte[] payload, @Nullable String header,
                                                       @NotNull String secret, long toleranceSeconds) {
        return constructEvent(payload, header, secret, toleranceSeconds, nowSeconds());
    }

    /** String-body variant of {@link #constructEvent(byte[], String, String, long)}. */
    public static @NotNull WebhookEvent constructEvent(@NotNull String payload, @Nullable String header,
                                                       @NotNull String secret, long toleranceSeconds) {
        return constructEvent(bytes(payload), header, secret, toleranceSeconds, nowSeconds());
    }

    /** {@link #constructEvent(byte[], String, String)} with a custom tolerance and an explicit clock (unix seconds). */
    public static @NotNull WebhookEvent constructEvent(@NotNull byte[] payload, @Nullable String header,
                                                       @NotNull String secret, long toleranceSeconds,
                                                       long nowSeconds) {
        String problem = problem(payload, header, secret, toleranceSeconds, nowSeconds);
        if (problem != null) throw new DosyaWebhookSignatureException(problem);
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8));
        } catch (JsonParseException e) {
            throw new DosyaException("Webhook payload is not valid JSON", e);
        }
        if (parsed == null || !parsed.isJsonObject()) {
            throw new DosyaException("Webhook payload is not a JSON object");
        }
        return new WebhookEvent(parsed.getAsJsonObject());
    }

    /** String-body variant of {@link #constructEvent(byte[], String, String, long, long)}. */
    public static @NotNull WebhookEvent constructEvent(@NotNull String payload, @Nullable String header,
                                                       @NotNull String secret, long toleranceSeconds,
                                                       long nowSeconds) {
        return constructEvent(bytes(payload), header, secret, toleranceSeconds, nowSeconds);
    }

    // ── internals ──

    /** Null when the signature is valid, otherwise a human-readable reason. */
    private static String problem(byte[] payload, String header, String secret, long tolerance, long now) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("WebhookSignature: secret is required");
        }
        if (payload == null) {
            throw new IllegalArgumentException("WebhookSignature: payload (the raw request body) is required");
        }
        if (header == null || header.trim().isEmpty()) {
            return "Missing X-Dosya-Signature header";
        }

        String timestamp = null;
        List<String> signatures = new ArrayList<>();
        for (String part : header.split(",", -1)) {
            int eq = part.indexOf('=');
            if (eq == -1) continue;
            String key = part.substring(0, eq).trim();
            String value = part.substring(eq + 1).trim();
            if ("t".equals(key)) timestamp = value;
            else if ("v1".equals(key)) signatures.add(value);
        }
        long t;
        if (timestamp == null || !DIGITS.matcher(timestamp).matches()) {
            return "Malformed X-Dosya-Signature header: missing or invalid timestamp";
        }
        try {
            t = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return "Malformed X-Dosya-Signature header: missing or invalid timestamp";
        }
        List<byte[]> valid = new ArrayList<>();
        for (String s : signatures) {
            if (HEX64.matcher(s).matches()) valid.add(hexToBytes(s));
        }
        if (valid.isEmpty()) {
            return "Malformed X-Dosya-Signature header: missing or invalid v1 signature";
        }

        boolean outside;
        try {
            outside = Math.abs(Math.subtractExact(now, t)) > tolerance;
        } catch (ArithmeticException e) {
            outside = true;
        }
        if (outside) {
            return "Webhook timestamp is outside the tolerance of " + tolerance + "s";
        }

        byte[] expected = hmac(secret, timestamp, payload);
        for (byte[] candidate : valid) {
            if (MessageDigest.isEqual(expected, candidate)) return null;
        }
        return "Webhook signature does not match";
    }

    private static byte[] hmac(String secret, String timestamp, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            mac.update(payload);
            return mac.doFinal();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }

    private static byte[] hexToBytes(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    private static byte[] bytes(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("WebhookSignature: payload (the raw request body) is required");
        }
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
