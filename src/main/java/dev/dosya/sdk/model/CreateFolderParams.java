package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for creating a folder or a nested folder path.
 *
 * <pre>{@code
 * new CreateFolderParams("ws_123", "reports/2026/q3").parentId("fld_root");
 * }</pre>
 *
 * @since 0.3.0
 */
public final class CreateFolderParams {

    private final String workspaceId;
    private final String name;
    private String parentId;

    /**
     * @param workspaceId the workspace to create the folder in
     * @param name        a folder name, or a path such as {@code "a/b/c"} ({@code /} or {@code \}
     *                    separated) to create nested folders in one call. Find-or-create:
     *                    segments that already exist are reused, so repeating the call is safe.
     */
    public CreateFolderParams(@NotNull String workspaceId, @NotNull String name) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * The parent folder; {@code null} (the default) for the workspace root. A folder-confined
     * member lands in their own folder.
     */
    public @NotNull CreateFolderParams parentId(@Nullable String parentId) { this.parentId = parentId; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getName() { return name; }
    public @Nullable String getParentId() { return parentId; }

    /** The request body: {@code workspace_id}, {@code name} and {@code parent_id} when set. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        if (parentId != null) body.put("parent_id", parentId);
        body.put("name", name);
        return body;
    }
}
