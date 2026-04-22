package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for creating a new API key.
 *
 * <pre>{@code
 * CreateApiKeyParams params = new CreateApiKeyParams("CI/CD Key")
 *     .scope("read")
 *     .expiresInDays(90);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateApiKeyParams {

    private final String name;
    private String scope;
    private Integer expiresInDays;

    public CreateApiKeyParams(@NotNull String name) {
        this.name = name;
    }

    public CreateApiKeyParams scope(String scope) { this.scope = scope; return this; }
    public CreateApiKeyParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }

    public @NotNull String getName() { return name; }
    public @Nullable String getScope() { return scope; }
    public @Nullable Integer getExpiresInDays() { return expiresInDays; }
}
