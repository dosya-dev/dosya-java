package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import dev.dosya.sdk.exception.DosyaUploadException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static dev.dosya.sdk.internal.DosyaHttpClient.encode;

public class UploadResource {

    private static final int CONCURRENCY = 3;

    private final DosyaHttpClient http;

    public UploadResource(DosyaHttpClient http) {
        this.http = http;
    }

    public UploadResult file(UploadParams params) {
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

    public UploadResult resume(String sessionId, byte[] body, Consumer<UploadProgress> onProgress) {
        UploadStatusResponse st = status(sessionId);

        if ("complete".equals(st.getStatus())) {
            throw new DosyaUploadException("Upload session is already complete", sessionId);
        }

        if (!st.isHasMultipart() || st.getPartSize() == null || st.getTotalParts() == null) {
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

    public UploadInitResponse init(String workspaceId, String fileName, long fileSize,
                                   String mimeType, String region, String folderId, String fileId) {
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

    public UploadStatusResponse status(String sessionId) {
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
        final long[] sharedBytesUploaded = {bytesUploaded};
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
                                        "Failed to upload part " + partNumber + " after " + (maxPartRetries + 1) + " attempts: " + lastErr,
                                        sessionId, partNumber);
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
                        sharedBytesUploaded[0] = Math.min((long) uploadedSet.size() * partSize, finalTotalBytes);
                        if (onProgress != null) {
                            onProgress.accept(new UploadProgress(sharedBytesUploaded[0], finalTotalBytes,
                                    (int) Math.round((double) sharedBytesUploaded[0] / finalTotalBytes * 100),
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
