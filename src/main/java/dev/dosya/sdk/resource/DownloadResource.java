package dev.dosya.sdk.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.ArchiveListing;
import dev.dosya.sdk.model.DownloadLink;
import dev.dosya.sdk.model.DownloadOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Downloads: presigned links, bytes, inline raw reads, thumbnails and ZIP archives.
 *
 * <p>Presigned storage URLs are fetched without the API key, which must never reach storage.
 *
 * @since 0.1.0
 */
public final class DownloadResource {

    private static final int DEFAULT_TTL = 300;
    private static final int MAX_TTL = 3600;

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code DownloadResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public DownloadResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    // ── Links ──

    /**
     * Returns a presigned download link for the current version.
     *
     * @see #getUrl(String, DownloadOptions)
     * @since 0.3.0
     */
    public @NotNull DownloadLink getUrl(@NotNull String fileId) {
        return getUrl(fileId, new DownloadOptions());
    }

    /**
     * Returns a presigned download link plus the file's size, name and region
     * ({@code GET /api/files/{id}/download-url}). Reads {@code version}, {@code unlockToken}
     * and {@code ttl}.
     *
     * <p>Needs the role's download permission; view-only files are refused (403) and
     * {@code full_lock} files need an unlock token. Egress is metered when the link is
     * issued (429 when over the cap). Hidden or folder-locked files answer 404.
     *
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API refuses
     * @since 0.3.0
     */
    public @NotNull DownloadLink getUrl(@NotNull String fileId, @NotNull DownloadOptions options) {
        long issuedAt = System.currentTimeMillis() / 1000;
        JsonObject res = http.request(HttpRequest.get("/api/files/" + seg(fileId) + "/download-url")
                .query("version", options.getVersion())
                .query("ut", options.getUnlockToken())
                .query("ttl", options.getTtl())
                .envelope(false));
        String url = string(res, "url");
        if (url == null || url.isEmpty()) {
            throw new DosyaException("The download-url response did not contain a url");
        }
        JsonElement size = res.get("size");
        return new DownloadLink(url,
                size != null && size.isJsonPrimitive() ? size.getAsLong() : 0L,
                nonNull(string(res, "name")),
                nonNull(string(res, "region")),
                issuedAt + clampTtl(options.getTtl()));
    }

    /**
     * Returns the presigned URL only.
     *
     * @deprecated use {@link #getUrl(String, DownloadOptions)}, which also returns size, name,
     * region and expiry. Kept from 0.2 with the same arguments.
     */
    @Deprecated
    public @NotNull String getUrl(@NotNull String fileId, @Nullable Integer version, @Nullable String unlockToken) {
        return getUrl(fileId, new DownloadOptions().version(version).unlockToken(unlockToken)).getUrl();
    }

    // ── Bytes through the presigned URL ──

    /** Downloads the current version into memory. Same gates as {@link #getUrl(String, DownloadOptions)}. */
    public byte @NotNull [] downloadBytes(@NotNull String fileId) {
        return downloadBytes(fileId, new DownloadOptions());
    }

    /**
     * Downloads the file into memory through its presigned URL. Reads {@code version},
     * {@code unlockToken} and {@code range}. Same gates as {@link #getUrl(String, DownloadOptions)};
     * a storage refusal throws {@link dev.dosya.sdk.exception.DosyaApiException} with its status.
     *
     * @since 0.3.0
     */
    public byte @NotNull [] downloadBytes(@NotNull String fileId, @NotNull DownloadOptions options) {
        try (InputStream in = downloadStream(fileId, options)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new DosyaException("Download failed: " + e.getMessage(), e);
        }
    }

    /** @deprecated use {@link #downloadBytes(String, DownloadOptions)}. */
    @Deprecated
    public byte @NotNull [] downloadBytes(@NotNull String fileId, @Nullable Integer version, @Nullable String unlockToken) {
        return downloadBytes(fileId, new DownloadOptions().version(version).unlockToken(unlockToken));
    }

    /** Streams the current version. The caller closes the stream. */
    public @NotNull InputStream downloadStream(@NotNull String fileId) {
        return downloadStream(fileId, new DownloadOptions());
    }

    /**
     * Streams the file from storage through its presigned URL. Reads {@code version},
     * {@code unlockToken} and {@code range}. The caller closes the stream.
     *
     * @since 0.3.0
     */
    public @NotNull InputStream downloadStream(@NotNull String fileId, @NotNull DownloadOptions options) {
        return fetchPresigned(fileId, options).body();
    }

    /** @deprecated use {@link #downloadStream(String, DownloadOptions)}. */
    @Deprecated
    public @NotNull InputStream downloadStream(@NotNull String fileId, @Nullable Integer version, @Nullable String unlockToken) {
        return downloadStream(fileId, new DownloadOptions().version(version).unlockToken(unlockToken));
    }

    /**
     * Downloads the current version to {@code target}, replacing it.
     *
     * @return bytes written
     * @since 0.3.0
     */
    public long downloadTo(@NotNull String fileId, @NotNull Path target) {
        return downloadTo(fileId, target, new DownloadOptions());
    }

    /**
     * Downloads the file to {@code target}, replacing it. The bytes go to a temporary file
     * next to the target first, so a failed download leaves no partial target. Reads
     * {@code version}, {@code unlockToken} and {@code range}.
     *
     * @return bytes written
     * @since 0.3.0
     */
    public long downloadTo(@NotNull String fileId, @NotNull Path target, @NotNull DownloadOptions options) {
        Path absolute = target.toAbsolutePath();
        Path dir = absolute.getParent();
        Path tmp = null;
        try (InputStream in = downloadStream(fileId, options)) {
            // Not Files.createTempFile: it creates owner-only (0600) files, and the move would
            // keep that mode. A plain create gets the process's normal permissions.
            tmp = dir.resolve(".dosya-download-" + java.util.UUID.randomUUID() + ".part");
            long written;
            try (java.io.OutputStream out = Files.newOutputStream(tmp, java.nio.file.StandardOpenOption.CREATE_NEW,
                    java.nio.file.StandardOpenOption.WRITE)) {
                written = in.transferTo(out);
            }
            Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            tmp = null;
            return written;
        } catch (IOException e) {
            throw new DosyaException("Download to " + target + " failed: " + e.getMessage(), e);
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                    // Best effort.
                }
            }
        }
    }

    // ── Through the API ──

    /**
     * The current version's bytes served inline.
     * @see #raw(String, DownloadOptions)
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> raw(@NotNull String fileId) {
        return raw(fileId, new DownloadOptions());
    }

    /**
     * The file's bytes served inline through the API ({@code GET /api/files/{id}/raw}), with
     * {@code Content-Type} by extension and an {@code ETag}. Reads {@code version},
     * {@code unlockToken}, {@code range} (206, or 416 thrown) and {@code ifNoneMatch} (304).
     * Viewer semantics: works on view-only files and needs no download permission.
     * The caller closes the body.
     *
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> raw(@NotNull String fileId, @NotNull DownloadOptions options) {
        return http.requestRaw(HttpRequest.get("/api/files/" + seg(fileId) + "/raw")
                .query("version", options.getVersion())
                .query("ut", options.getUnlockToken())
                .header("Range", options.rangeHeader())
                .header("If-None-Match", options.getIfNoneMatch()));
    }

    /**
     * A thumbnail of the current version.
     * @see #thumbnail(String, int, DownloadOptions)
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> thumbnail(@NotNull String fileId, int width) {
        return thumbnail(fileId, width, new DownloadOptions());
    }

    /**
     * A thumbnail, WebP or an embedded JPEG ({@code GET /api/files/{id}/thumb}). Width must be
     * 128, 256, 512 or 1600. Formats that need no thumbnail, and sources too large to render,
     * redirect to {@code /raw} and are followed. 415 when the type has no thumbnail. Reads
     * {@code version} and {@code unlockToken}. The caller closes the body.
     *
     * @throws IllegalArgumentException for any other width, before any request
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> thumbnail(@NotNull String fileId, int width, @NotNull DownloadOptions options) {
        if (width != 128 && width != 256 && width != 512 && width != 1600) {
            throw new IllegalArgumentException("width must be one of 128, 256, 512, 1600, got " + width);
        }
        return http.requestRaw(HttpRequest.get("/api/files/" + seg(fileId) + "/thumb")
                .query("w", width)
                .query("version", options.getVersion())
                .query("ut", options.getUnlockToken()));
    }

    /**
     * A streamed ZIP of files (at the archive root) and folders (recursively)
     * ({@code POST /api/files/download-archive}). Read scope is enough. Locked, hidden and
     * view-only items are left out; limits are 10 000 entries and 5 GiB. The timeout covers
     * the response headers only. The caller closes the body.
     *
     * @param fileIds   files to include, or null
     * @param folderIds folders to include, or null
     * @throws IllegalArgumentException when both lists are empty, before any request
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> archive(@Nullable List<String> fileIds, @Nullable List<String> folderIds) {
        List<String> files = fileIds != null ? fileIds : Collections.<String>emptyList();
        List<String> folders = folderIds != null ? folderIds : Collections.<String>emptyList();
        if (files.isEmpty() && folders.isEmpty()) {
            throw new IllegalArgumentException("archive() needs at least one id in fileIds or folderIds");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (!files.isEmpty()) body.put("file_ids", new ArrayList<>(files));
        if (!folders.isEmpty()) body.put("folder_ids", new ArrayList<>(folders));
        return http.requestRaw(HttpRequest.post("/api/files/download-archive").body(body));
    }

    /**
     * Lists a zip file's entries.
     * @see #archiveEntries(String, DownloadOptions)
     * @since 0.3.0
     */
    public @NotNull ArchiveListing archiveEntries(@NotNull String fileId) {
        return archiveEntries(fileId, new DownloadOptions());
    }

    /**
     * Lists the entries of a {@code .zip} file without downloading it
     * ({@code GET /api/files/{id}/archive}). 415 when the file is not a zip, 413 when its
     * directory is too large to list, 422 for a malformed zip. Reads {@code version} and
     * {@code unlockToken}.
     *
     * @since 0.3.0
     */
    public @NotNull ArchiveListing archiveEntries(@NotNull String fileId, @NotNull DownloadOptions options) {
        return http.requestAs(HttpRequest.get("/api/files/" + seg(fileId) + "/archive")
                .query("version", options.getVersion())
                .query("ut", options.getUnlockToken())
                .envelope(false), ArchiveListing.class);
    }

    /**
     * Streams one zip entry inline.
     * @see #archiveEntry(String, int, DownloadOptions)
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> archiveEntry(@NotNull String fileId, int index) {
        return archiveEntry(fileId, index, new DownloadOptions());
    }

    /**
     * Streams one entry of a {@code .zip} file by its index from {@link #archiveEntries}
     * ({@code GET /api/files/{id}/archive/entry}). Only stored and deflated, unencrypted
     * entries. Reads {@code version}, {@code unlockToken} and {@code attachment} (serve as an
     * attachment, which applies the download gates). The caller closes the body.
     *
     * @throws IllegalArgumentException for a negative index, before any request
     * @since 0.3.0
     */
    public @NotNull HttpResponse<InputStream> archiveEntry(@NotNull String fileId, int index, @NotNull DownloadOptions options) {
        if (index < 0) throw new IllegalArgumentException("index must be a non-negative integer, got " + index);
        return http.requestRaw(HttpRequest.get("/api/files/" + seg(fileId) + "/archive/entry")
                .query("i", index)
                .query("dl", options.isAttachment() ? 1 : null)
                .query("version", options.getVersion())
                .query("ut", options.getUnlockToken()));
    }

    // ── Private ──

    private HttpResponse<InputStream> fetchPresigned(String fileId, DownloadOptions options) {
        String range = options.rangeHeader();
        DownloadLink link = getUrl(fileId, new DownloadOptions()
                .version(options.getVersion())
                .unlockToken(options.getUnlockToken()));
        Map<String, String> headers = new HashMap<>();
        if (range != null) headers.put("Range", range);
        HttpResponse<InputStream> res = http.fetchExternal(URI.create(link.getUrl()), headers);
        int status = res.statusCode();
        if (status < 200 || status >= 300) {
            throw http.errorFrom(res, "GET", "/api/files/" + seg(fileId) + "/download-url");
        }
        return res;
    }

    private static int clampTtl(@Nullable Integer ttl) {
        if (ttl == null || ttl <= 0) return DEFAULT_TTL;
        return Math.min(ttl, MAX_TTL);
    }

    private static @Nullable String string(JsonObject json, String key) {
        JsonElement e = json.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    }

    private static String nonNull(@Nullable String value) {
        return value != null ? value : "";
    }
}
