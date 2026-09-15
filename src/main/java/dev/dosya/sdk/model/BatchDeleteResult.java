package dev.dosya.sdk.model;

/**
 * Outcome of a batch delete. Skipped items are not reported individually.
 *
 * @since 0.3.0
 */
public final class BatchDeleteResult {

    private int deleted;
    private int foldersDeleted;

    private BatchDeleteResult() {}

    /** Files actually moved to the trash. */
    public int getDeleted() { return deleted; }
    /** Folders actually moved to the trash. */
    public int getFoldersDeleted() { return foldersDeleted; }
}
