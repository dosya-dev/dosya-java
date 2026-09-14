package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for {@code team().createInviteLink()}.
 *
 * @since 0.3.0
 */
public final class CreateInviteLinkParams {

    private final String workspaceId;
    private String role;
    private Integer maxUses;
    private Integer expiresInDays;

    public CreateInviteLinkParams(@NotNull String workspaceId) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
    }

    /** Role id or {@code "Admin"} / {@code "Member"} / {@code "Viewer"}. Default {@code role_member}. */
    public @NotNull CreateInviteLinkParams role(@Nullable String role) { this.role = role; return this; }
    /** Null = unlimited. */
    public @NotNull CreateInviteLinkParams maxUses(@Nullable Integer maxUses) { this.maxUses = maxUses; return this; }
    /** Null or 0 = never expires. */
    public @NotNull CreateInviteLinkParams expiresInDays(@Nullable Integer expiresInDays) { this.expiresInDays = expiresInDays; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable String getRole() { return role; }
    public @Nullable Integer getMaxUses() { return maxUses; }
    public @Nullable Integer getExpiresInDays() { return expiresInDays; }

    /** The request body, with only the fields that were set. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        if (role != null) body.put("role", role);
        if (maxUses != null) body.put("max_uses", maxUses);
        if (expiresInDays != null) body.put("expires_in_days", expiresInDays);
        return body;
    }
}
