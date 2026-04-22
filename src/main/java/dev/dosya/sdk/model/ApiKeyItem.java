package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an API key belonging to the authenticated user.
 *
 * @since 0.1.0
 */
public final class ApiKeyItem {

    private String id;
    private String name;
    private String scope;
    private String keyPrefix;
    private Long lastUsedAt;
    private Long expiresAt;
    private long createdAt;

    private ApiKeyItem() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @Nullable String getScope() { return scope; }
    public @NotNull String getKeyPrefix() { return keyPrefix; }
    public @Nullable Long getLastUsedAt() { return lastUsedAt; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }
}
