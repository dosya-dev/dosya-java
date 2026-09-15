package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * The caller's role and effective permissions in one workspace ({@code GET /api/me/permissions}).
 *
 * @since 0.3.0
 */
public final class MyWorkspacePermissions {

    private String userId;
    private String roleId;
    private String roleName;
    private boolean isBuiltin;
    private String rootFolderId;
    private String rootFolderName;
    private Map<String, Boolean> permissions;

    private MyWorkspacePermissions() {}

    public @NotNull String getUserId() { return userId; }
    public @NotNull String getRoleId() { return roleId; }
    public @Nullable String getRoleName() { return roleName; }
    public boolean isBuiltin() { return isBuiltin; }

    /** Set when the caller is a folder-confined member: the folder they are anchored at. */
    public @Nullable String getRootFolderId() { return rootFolderId; }

    /** Name of the anchor folder; {@code null} when not confined or the folder was trashed. */
    public @Nullable String getRootFolderName() { return rootFolderName; }

    /**
     * Every permission key the API knows, {@code true} or {@code false}. Keys stay snake_case
     * as the API sends them (e.g. {@code upload_files}, {@code manage_settings}).
     */
    public @NotNull Map<String, Boolean> getPermissions() {
        return permissions == null ? Collections.emptyMap() : Collections.unmodifiableMap(permissions);
    }

    /** Whether the permission (snake_case key) is granted; {@code false} for unknown keys. */
    public boolean has(@NotNull String permission) {
        return permissions != null && Boolean.TRUE.equals(permissions.get(permission));
    }
}
