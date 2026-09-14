package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A member's role and confinement after {@code team().updateMember()}.
 *
 * @since 0.3.0
 */
public final class MemberUpdateResult {

    private String rootFolderId;
    private String roleId;

    private MemberUpdateResult() {}

    public @Nullable String getRootFolderId() { return rootFolderId; }
    public @NotNull String getRoleId() { return roleId; }
}
