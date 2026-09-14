package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code workspaces().list()}: the caller's workspaces plus their allocation
 * as an owner.
 *
 * @since 0.3.0
 */
public final class WorkspaceListResponse {

    private List<WorkspaceListItem> workspaces;
    private CapAllocation allocation;
    private String userEmail;
    @SerializedName("user_has_2fa")
    private boolean userHas2fa;
    private String userLoginMethod;

    private WorkspaceListResponse() {}

    /** Oldest first. A workspace-pinned key sees only its own workspace. */
    public @NotNull List<WorkspaceListItem> getWorkspaces() {
        return workspaces != null ? Collections.unmodifiableList(workspaces) : Collections.emptyList();
    }
    /** The caller's cap allocation as an owner. */
    public @Nullable CapAllocation getAllocation() { return allocation; }
    public @Nullable String getUserEmail() { return userEmail; }
    public boolean isUserHas2fa() { return userHas2fa; }
    public @Nullable String getUserLoginMethod() { return userLoginMethod; }
}
