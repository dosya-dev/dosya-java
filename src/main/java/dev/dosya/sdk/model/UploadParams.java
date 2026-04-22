package dev.dosya.sdk.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class UploadParams {

    private final String workspaceId;
    private final String fileName;
    private final long fileSize;
    private final byte[] body;
    private String mimeType;
    private String region;
    private String folderId;
    private String fileId;
    private Consumer<UploadProgress> onProgress;

    private UploadParams(String workspaceId, String fileName, long fileSize, byte[] body) {
        this.workspaceId = workspaceId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.body = body;
    }

    public static UploadParams fromBytes(String workspaceId, String fileName, byte[] data) {
        return new UploadParams(workspaceId, fileName, data.length, data);
    }

    public static UploadParams fromFile(String workspaceId, File file) throws IOException {
        byte[] data = readFile(file);
        return new UploadParams(workspaceId, file.getName(), data.length, data);
    }

    public static UploadParams fromStream(String workspaceId, String fileName, InputStream stream, long fileSize) throws IOException {
        byte[] data = readStream(stream);
        return new UploadParams(workspaceId, fileName, fileSize > 0 ? fileSize : data.length, data);
    }

    public UploadParams mimeType(String mimeType) { this.mimeType = mimeType; return this; }
    public UploadParams region(String region) { this.region = region; return this; }
    public UploadParams folderId(String folderId) { this.folderId = folderId; return this; }
    public UploadParams fileId(String fileId) { this.fileId = fileId; return this; }
    public UploadParams onProgress(Consumer<UploadProgress> onProgress) { this.onProgress = onProgress; return this; }

    public String getWorkspaceId() { return workspaceId; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public byte[] getBody() { return body; }
    public String getMimeType() { return mimeType; }
    public String getRegion() { return region; }
    public String getFolderId() { return folderId; }
    public String getFileId() { return fileId; }
    public Consumer<UploadProgress> getOnProgress() { return onProgress; }

    private static byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return readStream(fis);
        } finally {
            fis.close();
        }
    }

    private static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }
}
