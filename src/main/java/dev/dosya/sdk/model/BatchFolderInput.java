package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One entry of {@code folders().createBatch()}.
 *
 * @since 0.3.0
 */
public final class BatchFolderInput {

    private final String name;
    private final String parentId;

    /** A folder at the workspace root. {@code name} is a single segment; paths are not split. */
    public BatchFolderInput(@NotNull String name) {
        this(name, null);
    }

    /** A folder under {@code parentId} ({@code null} for the root). {@code name} is a single segment. */
    public BatchFolderInput(@NotNull String name, @Nullable String parentId) {
        this.name = Objects.requireNonNull(name, "name");
        this.parentId = parentId;
    }

    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }

    /** The wire entry: {@code name} and {@code parent_id} (explicit null for the root). */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("parent_id", parentId);
        return body;
    }
}
