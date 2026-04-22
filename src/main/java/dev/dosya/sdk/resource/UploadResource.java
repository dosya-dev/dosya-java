package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaUploadException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

/**
 * Provides operations for uploading files to Dosya workspaces.
 *
 * <p>Supports single-part and multipart uploads with automatic concurrency,
 * progress tracking, and resumable upload sessions.</p>
 *
 * @since 0.1.0
 */
public final class UploadResource {

    private static final int CONCURRENCY = 3;

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code UploadResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public UploadResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Uploads a file using the provided parameters. Automatically selects single-part
     * or multipart upload based on the server response.
     *
     * @param params the upload parameters including workspace ID, file data, and optional settings
     * @return the upload result containing the uploaded file detail and session ID
     * @throws DosyaUploadException if the upload fails
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull UploadResult file(@NotNull UploadParams params) {
        Consumer<UploadProgress> onProgress = params.getOnProgress();
        if (onProgress != null) {
            onProgress.accept(new UploadProgress(0, params.getFileSize(), 0, "initializing"));
        }

        UploadInitResponse session = init(
                params.getWorkspaceId(), params.getFileName(), params.getFileSize(),
                params.getMimeType(), params.getRegion(), params.getFolderId(), params.getFileId());

        if (session.getResumable() != null) {
            return uploadMultipart(session.getSessionId(), session.getResumable(),
                    params.getBody(), params.getFileSize(), session.getMimeType(),
                    onProgress, Collections.<Integer>emptyList());
        }

        return uploadSingle(session.getSessionId(), session.getUploadUrl(),
                params.getBody(), session.getMimeType(), params.getFileSize(), onProgress);
    }

    /**
     * Resumes a previously started multipart upload session.
     *
     * @param sessionId  the upload session ID to resume
     * @param body       the full file bytes to upload
     * @param onProgress an optional callback for progress updates, or {@code null}
     * @return the upload result containing the uploaded file detail and session ID
     * @throws DosyaUploadException if the session is already complete, not multipart, or the upload fails
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull UploadResult resume(@NotNull String sessionId, @NotNull byte[] body, @Nullable Consumer<UploadProgress> onProgress) {
        UploadStatusResponse st = status(sessionId);

        if ("complete".equals(st.getStatus())) {
            throw new DosyaUploadException("Upload session is already complete", sessionId);
        }

        if (!st.hasMultipart() || st.getPartSize() == null || st.getTotalParts() == null) {
            throw new DosyaUploadException("Cannot resume a non-multipart upload session", sessionId);
        }

        UploadInitResponse.Resumable resumable = new UploadInitResponse.Resumable(
                st.getPartSize(), st.getTotalParts(),
                "/api/upload/" + encode(sessionId) + "/part",
                "/api/upload/" + encode(sessionId) + "/complete",
                "/api/upload/" + encode(sessionId) + "/status");

        return uploadMultipart(sessionId, resumable, body, st.getSizeBytes(),
                "application/octet-stream", onProgress,
                st.getUploadedParts() != null ? st.getUploadedParts() : Collections.<Integer>emptyList());
    }

    /**
     * Initializes an upload session with the Dosya API.
     *
     * @param workspaceId the workspace to upload into
     * @param fileName    the name of the file being uploaded
     * @param fileSize    the total file size in bytes
     * @param mimeType    the MIME type of the file, or {@code null} for auto-detection
     * @param region      the storage region, or {@code null} for the default
     * @param folderId    the target folder ID, or {@code null} for the workspace root
     * @param fileId      an existing file ID for versioned re-upload, or {@code null}
     * @return the initialization response containing session ID and upload instructions
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull UploadInitResponse init(@NotNull String workspaceId, @NotNull String fileName, long fileSize,
                                            @Nullable String mimeType, @Nullable String region,
                                            @Nullable String folderId, @Nullable String fileId) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("workspace_id", workspaceId);
        body.put("file_name", fileName);
        body.put("file_size", fileSize);
        if (mimeType != null) body.put("mime_type", mimeType);
        if (region != null) body.put("region", region);
        if (folderId != null) body.put("folder_id", folderId);
        if (fileId != null) body.put("file_id", fileId);
        return http.requestAs(HttpRequest.post("/api/upload/init").body(body), UploadInitResponse.class);
    }

    /**
     * Retrieves the current status of an upload session.
     *
     * @param sessionId the upload session ID
     * @return the status response including uploaded parts and progress
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull UploadStatusResponse status(@NotNull String sessionId) {
        return http.requestAs(
                HttpRequest.get("/api/upload/" + encode(sessionId) + "/status"),
                UploadStatusResponse.class);
    }

    private UploadResult uploadSingle(String sessionId, String uploadUrl, byte[] body,
                                      String mimeType, long totalBytes,
                                      Consumer<UploadProgress> onProgress) {
        if (onProgress != null) {
            onProgress.accept(new UploadProgress(0, totalBytes, 0, "uploading"));
        }

        JsonObject result = http.request(
                HttpRequest.put(uploadUrl)
                        .rawBody(body)
                        .header("Content-Type", mimeType));

        if (onProgress != null) {
            onProgress.accept(new UploadProgress(totalBytes, totalBytes, 100, "complete"));
        }

        UploadResult.UploadedFile file = http.fromJson(result.get("file"), UploadResult.UploadedFile.class);
        return new UploadResult(file, sessionId);
    }

    private UploadResult uploadMultipart(String sessionId, UploadInitResponse.Resumable resumable,
                                         byte[] allBytes, long totalBytes, String mimeType,
                                         Consumer<UploadProgress> onProgress,
                                         List<Integer> alreadyUploaded) {
        int partSize = resumable.getPartSize();
        int totalParts = resumable.getTotalParts();
        String partUploadUrl = resumable.getPartUploadUrl();
        String completeUrl = resumable.getCompleteUrl();

        Set<Integer> uploadedSet = new HashSet<Integer>(alreadyUploaded);
        long bytesUploaded = (long) uploadedSet.size() * partSize;
        if (bytesUploaded > totalBytes) bytesUploaded = totalBytes;

        if (onProgress != null) {
            onProgress.accept(new UploadProgress(bytesUploaded, totalBytes,
                    (int) Math.round((double) bytesUploaded / totalBytes * 100),
                    uploadedSet.size(), totalParts, "uploading"));
        }

        // Build list of parts to upload
        List<int[]> parts = new ArrayList<int[]>(); // [partNumber, start, end]
        for (int i = 1; i <= totalParts; i++) {
            if (uploadedSet.contains(i)) continue;
            int start = (i - 1) * partSize;
            int end = Math.min(start + partSize, allBytes.length);
            parts.add(new int[]{i, start, end});
        }

        // Upload parts with concurrency pool
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(CONCURRENCY, Math.max(1, parts.size())));
        List<Future<?>> futures = new ArrayList<Future<?>>();
        final AtomicLong sharedBytesUploaded = new AtomicLong(bytesUploaded);
        final long finalTotalBytes = totalBytes;

        for (final int[] part : parts) {
            futures.add(executor.submit(new Runnable() {
                @Override
                public void run() {
                    int partNumber = part[0];
                    byte[] chunk = Arrays.copyOfRange(allBytes, part[1], part[2]);

                    int maxPartRetries = http.getMaxRetries();
                    Exception lastErr = null;
                    for (int attempt = 0; attempt <= maxPartRetries; attempt++) {
                        try {
                            http.request(
                                    HttpRequest.put(partUploadUrl + "/" + partNumber)
                                            .rawBody(chunk)
                                            .header("Content-Type", mimeType));
                            break;
                        } catch (Exception e) {
                            lastErr = e;
                            if (attempt == maxPartRetries) {
                                throw new DosyaUploadException(
                                        "Failed to upload part " + partNumber + " after " + (maxPartRetries + 1) + " attempts",
                                        sessionId, partNumber, lastErr);
                            }
                            long delay = http.getBaseDelay() * (1L << attempt);
                            long jitter = (long) (delay * 0.2 * Math.random());
                            try {
                                Thread.sleep(Math.min(delay + jitter, http.getMaxDelay()));
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new DosyaUploadException("Upload interrupted", sessionId, partNumber);
                            }
                        }
                    }

                    synchronized (uploadedSet) {
                        uploadedSet.add(partNumber);
                        long uploaded = Math.min((long) uploadedSet.size() * partSize, finalTotalBytes);
                        sharedBytesUploaded.set(uploaded);
                        if (onProgress != null) {
                            onProgress.accept(new UploadProgress(uploaded, finalTotalBytes,
                                    (int) Math.round((double) uploaded / finalTotalBytes * 100),
                                    uploadedSet.size(), totalParts, "uploading"));
                        }
                    }
                }
            }));
        }

        // Wait for all parts
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                executor.shutdownNow();
                Throwable cause = e.getCause();
                if (cause instanceof DosyaUploadException) {
                    throw (DosyaUploadException) cause;
                }
                throw new DosyaUploadException("Upload failed: " + cause.getMessage(), sessionId);
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw new DosyaUploadException("Upload interrupted", sessionId);
            }
        }
        executor.shutdown();

        // Complete
        if (onProgress != null) {
            onProgress.accept(new UploadProgress(totalBytes, totalBytes, 99,
                    totalParts, totalParts, "completing"));
        }

        JsonObject result = http.request(HttpRequest.post(completeUrl));

        if (onProgress != null) {
            onProgress.accept(new UploadProgress(totalBytes, totalBytes, 100,
                    totalParts, totalParts, "complete"));
        }

        UploadResult.UploadedFile file = http.fromJson(result.get("file"), UploadResult.UploadedFile.class);
        return new UploadResult(file, sessionId);
    }
}
