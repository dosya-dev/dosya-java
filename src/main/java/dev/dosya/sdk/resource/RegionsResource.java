package dev.dosya.sdk.resource;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.RegionsListResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Storage locations a workspace can be created in.
 *
 * @since 0.3.0
 */
public final class RegionsResource {

    private final DosyaHttpClient http;

    public RegionsResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /** Every location code plus the one suggested for the caller. {@code read} or {@code full} key. */
    public @NotNull RegionsListResponse list() {
        return http.requestAs(HttpRequest.get("/api/regions"), RegionsListResponse.class);
    }
}
