package dev.dosya.sdk.model;

public class UploadProgress {

    private final long bytesUploaded;
    private final long totalBytes;
    private final int percent;
    private final Integer partsCompleted;
    private final Integer totalParts;
    private final String status;

    public UploadProgress(long bytesUploaded, long totalBytes, int percent, String status) {
        this(bytesUploaded, totalBytes, percent, null, null, status);
    }

    public UploadProgress(long bytesUploaded, long totalBytes, int percent,
                          Integer partsCompleted, Integer totalParts, String status) {
        this.bytesUploaded = bytesUploaded;
        this.totalBytes = totalBytes;
        this.percent = percent;
        this.partsCompleted = partsCompleted;
        this.totalParts = totalParts;
        this.status = status;
    }

    public long getBytesUploaded() { return bytesUploaded; }
    public long getTotalBytes() { return totalBytes; }
    public int getPercent() { return percent; }
    public Integer getPartsCompleted() { return partsCompleted; }
    public Integer getTotalParts() { return totalParts; }
    public String getStatus() { return status; }
}
