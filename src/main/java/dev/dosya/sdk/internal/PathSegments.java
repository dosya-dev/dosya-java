package dev.dosya.sdk.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Encodes caller-supplied ids as single opaque path segments.
 */
public final class PathSegments {

    private PathSegments() {}

    /**
     * Encodes one id so it can never add a segment, a query or a fragment to a
     * path, and rejects the dot segments a URL resolver would collapse.
     *
     * @throws IllegalArgumentException for a null, empty, "." or ".." id
     */
    public static String seg(String id) {
        if (id == null || id.isEmpty() || ".".equals(id) || "..".equals(id)) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        try {
            // URLEncoder is form encoding: fix the three characters it treats differently.
            return URLEncoder.encode(id, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is always supported", e);
        }
    }
}
