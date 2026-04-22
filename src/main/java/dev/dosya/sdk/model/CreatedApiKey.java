package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a newly created API key, including the plain-text key value.
 *
 * <p>The plain-text key is only available at creation time and cannot be retrieved afterwards.</p>
 *
 * @since 0.1.0
 */
public final class CreatedApiKey {

    private String id;
    private String name;
    private String scope;
    private String plainKey;
    private Long expiresAt;
    private long createdAt;

    private CreatedApiKey() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public @Nullable String getScope() { return scope; }
    public @NotNull String getPlainKey() { return plainKey; }
    public @Nullable Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "CreatedApiKey{id='" + id + "', name='" + name + "', plainKey=***}";
    }
}
