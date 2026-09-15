package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A join link as returned right after {@code team().createInviteLink()}.
 *
 * @since 0.3.0
 */
public final class CreatedInviteLink {

    private String id;
    private String token;
    private String url;
    private String roleId;
    private String roleName;
    private Integer maxUses;
    private Long expiresAt;

    private CreatedInviteLink() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    /** Join URL: anyone holding it can join. */
    public @NotNull String getUrl() { return url; }
    public @NotNull String getRoleId() { return roleId; }
    public @NotNull String getRoleName() { return roleName; }
    public @Nullable Integer getMaxUses() { return maxUses; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
}
