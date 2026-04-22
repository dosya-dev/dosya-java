package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Parameters for uploading a file to a Dosya workspace.
 *
 * <p>Use the static factory methods to create instances from byte arrays, files, or streams.</p>
 *
 * <pre>{@code
 * UploadParams params = UploadParams.fromFile("ws_123", new File("report.pdf"))
 *     .mimeType("application/pdf")
 *     .folderId("folder_456")
 *     .onProgress(p -> System.out.println(p.getPercent() + "%"));
 * }</pre>
 *
 * @since 0.1.0
 */
public final class UploadParams {

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

    public static @NotNull UploadParams fromBytes(@NotNull String workspaceId, @NotNull String fileName, byte @NotNull [] data) {
        return new UploadParams(workspaceId, fileName, data.length, data);
    }

    public static @NotNull UploadParams fromFile(@NotNull String workspaceId, @NotNull File file) throws IOException {
        byte[] data = readFile(file);
        return new UploadParams(workspaceId, file.getName(), data.length, data);
    }

    public static @NotNull UploadParams fromStream(@NotNull String workspaceId, @NotNull String fileName, @NotNull InputStream stream, long fileSize) throws IOException {
        byte[] data = readStream(stream);
        return new UploadParams(workspaceId, fileName, fileSize > 0 ? fileSize : data.length, data);
    }

    public UploadParams mimeType(String mimeType) { this.mimeType = mimeType; return this; }
    public UploadParams region(String region) { this.region = region; return this; }
    public UploadParams folderId(String folderId) { this.folderId = folderId; return this; }
    public UploadParams fileId(String fileId) { this.fileId = fileId; return this; }
    public UploadParams onProgress(Consumer<UploadProgress> onProgress) { this.onProgress = onProgress; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public byte @NotNull [] getBody() { return Arrays.copyOf(body, body.length); }
    public @Nullable String getMimeType() { return mimeType; }
    public @Nullable String getRegion() { return region; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFileId() { return fileId; }
    public @Nullable Consumer<UploadProgress> getOnProgress() { return onProgress; }

    private static byte[] readFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readStream(fis);
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
