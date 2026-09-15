package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A change to a member's role and/or folder confinement. Fields never set are left alone.
 *
 * <pre>{@code
 * new UpdateMemberParams().roleId("role_viewer").rootFolderId("fld_1");
 * new UpdateMemberParams().clearRootFolder();
 * }</pre>
 *
 * @since 0.3.0
 */
public final class UpdateMemberParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public UpdateMemberParams() {}

    /** New role id. {@code role_owner} is refused; use {@code workspaces().transfer()}. */
    public @NotNull UpdateMemberParams roleId(@NotNull String roleId) {
        fields.put("role_id", roleId);
        return this;
    }

    /** Confines the member to this folder; null removes the confinement. */
    public @NotNull UpdateMemberParams rootFolderId(@Nullable String rootFolderId) {
        fields.put("root_folder_id", rootFolderId);
        return this;
    }

    /** Removes the member's folder confinement. */
    public @NotNull UpdateMemberParams clearRootFolder() {
        return rootFolderId(null);
    }

    /** The request body: only the fields that were set (a cleared folder is sent as null). */
    public @NotNull Map<String, Object> toBody() {
        return new LinkedHashMap<>(fields);
    }
}
