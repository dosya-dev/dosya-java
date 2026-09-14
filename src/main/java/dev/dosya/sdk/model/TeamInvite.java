package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A pending, unexpired email invite.
 *
 * @since 0.3.0
 */
public final class TeamInvite {

    private String id;
    private String email;
    private String roleId;
    private long createdAt;
    private long expiresAt;
    private String invitedByName;
    private String inviteUrl;

    private TeamInvite() {}

    /** Use with {@code team().resendInvite()} / {@code team().revokeInvite()}. */
    public @NotNull String getId() { return id; }
    public @NotNull String getEmail() { return email; }
    public @NotNull String getRoleId() { return roleId; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public @Nullable String getInvitedByName() { return invitedByName; }
    /** Accept link, for sharing the invite manually. */
    public @NotNull String getInviteUrl() { return inviteUrl; }
}
