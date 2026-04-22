package dev.dosya.sdk.resource;

import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for downloading files from Dosya.
 *
 * <p>Supports retrieving download URLs, downloading file content as byte arrays,
 * and streaming file content via {@link InputStream}.</p>
 *
 * @since 0.1.0
 */
public final class DownloadResource {

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code DownloadResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public DownloadResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Retrieves a pre-signed download URL for a file.
     *
     * @param fileId      the unique identifier of the file
     * @param version     the file version number, or {@code null} for the latest version
     * @param unlockToken the unlock token for locked files, or {@code null} if not locked
     * @return the pre-signed download URL
     * @throws DosyaException if the download URL cannot be extracted from the response
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String getUrl(@NotNull String fileId, @Nullable Integer version, @Nullable String unlockToken) {
        DosyaHttpClient.HttpResult result = http.doFetch(
                HttpRequest.get("/api/files/" + encode(fileId) + "/download")
                        .query("version", version)
                        .query("ut", unlockToken)
                        .rawResponse(true));

        if (result.body != null && !result.body.isEmpty()) {
            return result.body;
        }

        throw new DosyaException("Could not extract download URL from response");
    }

    /**
     * Retrieves a pre-signed download URL for the latest version of a file.
     *
     * @param fileId the unique identifier of the file
     * @return the pre-signed download URL
     * @throws DosyaException if the download URL cannot be extracted from the response
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull String getUrl(@NotNull String fileId) {
        return getUrl(fileId, null, null);
    }

    /**
     * Downloads the file content as a byte array.
     *
     * @param fileId      the unique identifier of the file
     * @param version     the file version number, or {@code null} for the latest version
     * @param unlockToken the unlock token for locked files, or {@code null} if not locked
     * @return the file content as a byte array
     * @throws DosyaException if the download fails
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public byte @NotNull [] downloadBytes(@NotNull String fileId, @Nullable Integer version, @Nullable String unlockToken) {
        String url = getUrl(fileId, version, unlockToken);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() >= 400) {
                throw new DosyaException("Download failed with status " + conn.getResponseCode());
            }
            try (InputStream is = conn.getInputStream()) {
                return readBytes(is);
            }
        } catch (IOException e) {
            throw new DosyaException("Download failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Downloads the latest version of a file as a byte array.
     *
     * @param fileId the unique identifier of the file
     * @return the file content as a byte array
     * @throws DosyaException if the download fails
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public byte @NotNull [] downloadBytes(@NotNull String fileId) {
        return downloadBytes(fileId, null, null);
    }

    /**
     * Downloads the file content as a streaming {@link InputStream}.
     *
     * <p>The caller is responsible for closing the returned stream, which will also
     * disconnect the underlying HTTP connection.</p>
     *
     * @param fileId      the unique identifier of the file
     * @param version     the file version number, or {@code null} for the latest version
     * @param unlockToken the unlock token for locked files, or {@code null} if not locked
     * @return an input stream of the file content
     * @throws DosyaException if the download fails
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull InputStream downloadStream(@NotNull String fileId, @Nullable Integer version, @Nullable String unlockToken) {
        String url = getUrl(fileId, version, unlockToken);
        try {
            final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() >= 400) {
                conn.disconnect();
                throw new DosyaException("Download failed with status " + conn.getResponseCode());
            }
            return new FilterInputStream(conn.getInputStream()) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        conn.disconnect();
                    }
                }
            };
        } catch (IOException e) {
            throw new DosyaException("Download failed: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads the latest version of a file as a streaming {@link InputStream}.
     *
     * <p>The caller is responsible for closing the returned stream.</p>
     *
     * @param fileId the unique identifier of the file
     * @return an input stream of the file content
     * @throws DosyaException if the download fails
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull InputStream downloadStream(@NotNull String fileId) {
        return downloadStream(fileId, null, null);
    }

    private byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }
}
