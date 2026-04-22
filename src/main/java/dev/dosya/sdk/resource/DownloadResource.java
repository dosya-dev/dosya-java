package dev.dosya.sdk.resource;

import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class DownloadResource {

    private final DosyaHttpClient http;

    public DownloadResource(DosyaHttpClient http) {
        this.http = http;
    }

    public String getUrl(String fileId, Integer version, String unlockToken) {
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

    public String getUrl(String fileId) {
        return getUrl(fileId, null, null);
    }

    public byte[] downloadBytes(String fileId, Integer version, String unlockToken) {
        String url = getUrl(fileId, version, unlockToken);
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() >= 400) {
                throw new DosyaException("Download failed with status " + conn.getResponseCode());
            }
            return readBytes(conn.getInputStream());
        } catch (IOException e) {
            throw new DosyaException("Download failed: " + e.getMessage(), e);
        }
    }

    public byte[] downloadBytes(String fileId) {
        return downloadBytes(fileId, null, null);
    }

    public InputStream downloadStream(String fileId, Integer version, String unlockToken) {
        String url = getUrl(fileId, version, unlockToken);
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() >= 400) {
                throw new DosyaException("Download failed with status " + conn.getResponseCode());
            }
            return conn.getInputStream();
        } catch (IOException e) {
            throw new DosyaException("Download failed: " + e.getMessage(), e);
        }
    }

    public InputStream downloadStream(String fileId) {
        return downloadStream(fileId, null, null);
    }

    private byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        is.close();
        return baos.toByteArray();
    }
}
