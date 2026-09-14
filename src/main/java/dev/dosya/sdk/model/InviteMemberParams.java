package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for {@code team().invite()}.
 *
 * <pre>{@code
 * new InviteMemberParams("ws_1", "ana@example.com").role("role_viewer");
 * }</pre>
 *
 * @since 0.3.0
 */
public final class InviteMemberParams {

    private final String workspaceId;
    private final String email;
    private String role;

    public InviteMemberParams(@NotNull String workspaceId, @NotNull String email) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        this.email = Objects.requireNonNull(email, "email");
    }

    /**
     * A role id ({@code role_admin}, {@code role_member}, {@code role_viewer} or a custom
     * role id) or one of the labels {@code "Admin"}, {@code "Member"}, {@code "Viewer"}.
     * Default {@code role_member}.
     */
    public @NotNull InviteMemberParams role(@Nullable String role) { this.role = role; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @NotNull String getEmail() { return email; }
    public @Nullable String getRole() { return role; }

    /** The request body. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        body.put("email", email);
        if (role != null) body.put("role", role);
        return body;
    }
}
