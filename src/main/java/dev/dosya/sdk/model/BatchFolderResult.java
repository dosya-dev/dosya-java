package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One folder resolved by {@code folders().createBatch()}.
 *
 * @since 0.3.0
 */
public final class BatchFolderResult {

    private String name;
    private String parentId;
    private String id;
    private boolean created;

    private BatchFolderResult() {}

    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }
    public @NotNull String getId() { return id; }
    /** False when the folder already existed, or for a repeated entry. */
    public boolean isCreated() { return created; }
}
