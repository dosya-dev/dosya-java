package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The account that owns the API key, as returned by {@code GET /api/me}.
 *
 * @since 0.1.0
 */
public final class UserProfile {

    private String id;
    private String email;
    private String name;
    private String initials;
    private String avatarUrl;
    private Long deletionScheduledFor;
    private String preferredLanguage;
    private String uiTheme;
    private String uiMode;
    private long createdAt;
    private Long emailVerifiedAt;
    private boolean hasPassword;
    private int workspaceCount;
    private boolean tourCompleted;

    private UserProfile() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getEmail() { return email; }
    public @NotNull String getName() { return name; }
    public @NotNull String getInitials() { return initials; }

    /**
     * Storage key of the avatar image (e.g. {@code avatars/<userId>/avatar.png}), NOT a
     * fetchable URL. {@code null} when the user has no avatar.
     */
    public @Nullable String getAvatarUrl() { return avatarUrl; }

    /**
     * Unix seconds when a pending account deletion will run; {@code null} when none is scheduled.
     *
     * @since 0.3.0
     */
    public @Nullable Long getDeletionScheduledFor() { return deletionScheduledFor; }

    public @Nullable String getPreferredLanguage() { return preferredLanguage; }

    /** @since 0.3.0 */
    public @Nullable String getUiTheme() { return uiTheme; }

    /** @since 0.3.0 */
    public @Nullable String getUiMode() { return uiMode; }

    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }

    /** Unix seconds, or {@code null} when the email is not verified. */
    public @Nullable Long getEmailVerifiedAt() { return emailVerifiedAt; }

    /**
     * {@code false} for OAuth-created accounts that never set a password.
     *
     * @since 0.3.0
     */
    public boolean hasPassword() { return hasPassword; }

    public int getWorkspaceCount() { return workspaceCount; }

    /** @since 0.3.0 */
    public boolean isTourCompleted() { return tourCompleted; }
}
