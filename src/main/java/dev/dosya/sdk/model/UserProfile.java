package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the authenticated user's profile information.
 *
 * @since 0.1.0
 */
public final class UserProfile {

    private String id;
    private String email;
    private String name;
    private String initials;
    private String avatarUrl;
    private String preferredLanguage;
    private long createdAt;
    private Long emailVerifiedAt;
    private int workspaceCount;

    private UserProfile() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getEmail() { return email; }
    public @NotNull String getName() { return name; }
    public @NotNull String getInitials() { return initials; }
    public @Nullable String getAvatarUrl() { return avatarUrl; }
    public @Nullable String getPreferredLanguage() { return preferredLanguage; }
    public long getCreatedAt() { return createdAt; }
    public @Nullable Long getEmailVerifiedAt() { return emailVerifiedAt; }
    public int getWorkspaceCount() { return workspaceCount; }
}
