package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A workspace member, as listed by {@code team().list()}.
 *
 * @since 0.3.0
 */
public final class TeamMember {

    private String membershipId;
    private String userId;
    private String roleId;
    private long joinedAt;
    private String rootFolderId;
    private String rootFolderName;
    private String name;
    private String email;
    private String avatarUrl;
    private Long lastActiveAt;
    private boolean isYou;

    private TeamMember() {}

    /** Use with {@code team().updateMember()} / {@code team().removeMember()}. */
    public @NotNull String getMembershipId() { return membershipId; }
    /** Use with {@code workspaces().transfer()}. */
    public @NotNull String getUserId() { return userId; }
    public @NotNull String getRoleId() { return roleId; }
    public long getJoinedAt() { return joinedAt; }
    /** Folder the member is confined to, or null for the whole workspace. */
    public @Nullable String getRootFolderId() { return rootFolderId; }
    /** Null when unconfined or the folder is in the trash. */
    public @Nullable String getRootFolderName() { return rootFolderName; }
    public @Nullable String getName() { return name; }
    public @NotNull String getEmail() { return email; }
    public @Nullable String getAvatarUrl() { return avatarUrl; }
    public @Nullable Long getLastActiveAt() { return lastActiveAt; }
    /** True for the caller's own membership. */
    public boolean isYou() { return isYou; }
}
