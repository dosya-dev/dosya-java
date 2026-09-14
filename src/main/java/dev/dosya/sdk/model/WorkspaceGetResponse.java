package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Response of {@code workspaces().get()}: the workspace, its settings row, the caller's
 * role and the storage picture.
 *
 * @since 0.1.0
 */
public final class WorkspaceGetResponse {

    private WorkspaceDetail workspace;
    private WorkspaceSettings settings;
    private String roleId;
    private boolean isOwner;
    private String plan;
    private WorkspaceStorage storage;
    private PlanLimits planLimits;
    private CapAllocation allocation;

    private WorkspaceGetResponse() {}

    public @NotNull WorkspaceDetail getWorkspace() { return workspace; }
    /** The settings row, or null when the workspace has none. */
    public @Nullable WorkspaceSettings getSettings() { return settings; }
    /** The caller's role. */
    public @NotNull String getRoleId() { return roleId; }
    public boolean isOwner() { return isOwner; }
    /** The owner's effective plan id. @since 0.3.0 */
    public @Nullable String getPlan() { return plan; }
    /** @since 0.3.0 */
    public @Nullable WorkspaceStorage getStorage() { return storage; }
    /** @since 0.3.0 */
    public @Nullable PlanLimits getPlanLimits() { return planLimits; }
    /** The owner's cap allocation. @since 0.3.0 */
    public @Nullable CapAllocation getAllocation() { return allocation; }

    /**
     * The owner's plan limits.
     *
     * @since 0.3.0
     */
    public static final class PlanLimits {
        private long storageGb;

        private PlanLimits() {}

        public long getStorageGb() { return storageGb; }
    }
}
