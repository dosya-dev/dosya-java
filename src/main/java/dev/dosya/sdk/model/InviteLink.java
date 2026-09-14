package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An active workspace join link.
 *
 * @since 0.3.0
 */
public final class InviteLink {

    private String id;
    private String token;
    private String roleId;
    private String roleName;
    private Integer maxUses;
    private int useCount;
    private Long expiresAt;
    private boolean isRevoked;
    private long createdAt;
    private String createdByName;
    private String url;

    private InviteLink() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getToken() { return token; }
    public @NotNull String getRoleId() { return roleId; }
    /** The built-in role's label; for a custom role the API currently returns its id. */
    public @NotNull String getRoleName() { return roleName; }
    /** Null = unlimited. */
    public @Nullable Integer getMaxUses() { return maxUses; }
    public int getUseCount() { return useCount; }
    /** Unix seconds, or null for never. */
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return isRevoked; }
    public long getCreatedAt() { return createdAt; }
    public @Nullable String getCreatedByName() { return createdByName; }
    /** Join URL: anyone holding it can join with {@link #getRoleId()}. */
    public @NotNull String getUrl() { return url; }
}
