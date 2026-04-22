package dev.dosya.sdk.model;

import java.util.List;

public class UploadStatusResponse {

    private String sessionId;
    private String status;
    private long sizeBytes;
    private Integer partSize;
    private Integer totalParts;
    private long bytesUploaded;
    private List<Integer> uploadedParts;
    private boolean hasMultipart;

    private UploadStatusResponse() {}

    public String getSessionId() { return sessionId; }
    public String getStatus() { return status; }
    public long getSizeBytes() { return sizeBytes; }
    public Integer getPartSize() { return partSize; }
    public Integer getTotalParts() { return totalParts; }
    public long getBytesUploaded() { return bytesUploaded; }
    public List<Integer> getUploadedParts() { return uploadedParts; }
    public boolean isHasMultipart() { return hasMultipart; }
}
