package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * What an uploader is judged against in a workspace.
 *
 * @since 0.3.0
 */
public final class WorkspaceUploadLimits {

    private String allowedExtensions;
    private String blockedExtensions;
    private Double maxFileSizeGb;
    private Long storageRemainingBytes;
    private Integer maxConcurrentUploads;

    private WorkspaceUploadLimits() {}

    /** Comma-separated, lowercase, dot-prefixed. Null = all allowed. */
    public @Nullable String getAllowedExtensions() { return allowedExtensions; }
    public @Nullable String getBlockedExtensions() { return blockedExtensions; }
    /** Per-file cap in GB; null = unlimited. */
    public @Nullable Double getMaxFileSizeGb() { return maxFileSizeGb; }
    /** A hint (cached up to 60 s); the upload gate decides. Null when unknown. */
    public @Nullable Long getStorageRemainingBytes() { return storageRemainingBytes; }
    /** Open upload sessions one member may hold. 0 = no limit. */
    public @Nullable Integer getMaxConcurrentUploads() { return maxConcurrentUploads; }
}
