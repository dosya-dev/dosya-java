package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response containing the current status of an upload session.
 *
 * @since 0.1.0
 */
public final class UploadStatusResponse {

    private String sessionId;
    private String status;
    private long sizeBytes;
    private Integer partSize;
    private Integer totalParts;
    private long bytesUploaded;
    private List<Integer> uploadedParts;
    private boolean hasMultipart;

    private UploadStatusResponse() {}

    public @NotNull String getSessionId() { return sessionId; }
    public @NotNull String getStatus() { return status; }
    public long getSizeBytes() { return sizeBytes; }
    public @Nullable Integer getPartSize() { return partSize; }
    public @Nullable Integer getTotalParts() { return totalParts; }
    public long getBytesUploaded() { return bytesUploaded; }
    public @NotNull List<Integer> getUploadedParts() { return uploadedParts != null ? Collections.unmodifiableList(uploadedParts) : Collections.<Integer>emptyList(); }
    public boolean hasMultipart() { return hasMultipart; }
}
