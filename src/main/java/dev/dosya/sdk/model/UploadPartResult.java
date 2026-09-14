package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The server's answer to one multipart part.
 *
 * @since 0.3.0
 */
public final class UploadPartResult {

    private int partNumber;
    private String etag;
    private Integer totalParts;
    private Boolean alreadyUploaded;

    private UploadPartResult() {}

    public int getPartNumber() { return partNumber; }
    public @NotNull String getEtag() { return etag; }
    /** Present on a freshly stored part. */
    public @Nullable Integer getTotalParts() { return totalParts; }
    /** True when the server already had this part (the request is idempotent). */
    public boolean isAlreadyUploaded() { return alreadyUploaded != null && alreadyUploaded; }
}
