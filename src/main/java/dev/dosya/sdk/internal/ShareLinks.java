package dev.dosya.sdk.internal;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.model.CreateShareLinkParams;
import dev.dosya.sdk.model.CreatedShareLink;
import dev.dosya.sdk.model.ItemShareLink;
import dev.dosya.sdk.model.ShareByEmailParams;
import dev.dosya.sdk.model.ShareByEmailResult;

import java.lang.reflect.Type;
import java.util.List;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Share-link calls that exist identically for files and folders
 * ({@code /api/files/:id/share*} and {@code /api/folders/:id/share*}). The two create
 * routes refuse each other's recipient fields with a 400, so each params class
 * builds its body from an explicit allowlist.
 */
public final class ShareLinks {

    /** {@code "files"} or {@code "folders"}. */
    public enum Kind { FILES, FOLDERS }

    private static final Type LINK_LIST = new TypeToken<List<ItemShareLink>>() {}.getType();

    private ShareLinks() {}

    private static String base(Kind kind, String id) {
        return "/api/" + (kind == Kind.FILES ? "files" : "folders") + "/" + seg(id);
    }

    public static List<ItemShareLink> list(DosyaHttpClient http, Kind kind, String id) {
        JsonObject res = http.request(HttpRequest.get(base(kind, id) + "/share"));
        return http.fromJson(res.get("links"), LINK_LIST);
    }

    public static CreatedShareLink create(DosyaHttpClient http, Kind kind, String id, CreateShareLinkParams params) {
        CreateShareLinkParams p = params != null ? params : new CreateShareLinkParams();
        JsonObject res = http.request(HttpRequest.post(base(kind, id) + "/share").body(p.toBody()));
        return http.fromJson(res.get("link"), CreatedShareLink.class);
    }

    public static ShareByEmailResult email(DosyaHttpClient http, Kind kind, String id, ShareByEmailParams params) {
        // Never repeated automatically: a failure can arrive after the link was created and mailed.
        return http.requestAs(HttpRequest.post(base(kind, id) + "/share-email")
                .body(params.toBody()).retry(HttpRequest.Retry.NEVER), ShareByEmailResult.class);
    }
}
