package dev.dosya.sdk.model;

public class UserProfile {

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

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getInitials() { return initials; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public long getCreatedAt() { return createdAt; }
    public Long getEmailVerifiedAt() { return emailVerifiedAt; }
    public int getWorkspaceCount() { return workspaceCount; }
}
