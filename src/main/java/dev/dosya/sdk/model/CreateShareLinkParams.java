package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Optional parameters for creating a share link on a file.
 *
 * <pre>{@code
 * CreateShareLinkParams params = new CreateShareLinkParams()
 *     .expiresInDays(7)
 *     .password("secret");
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateShareLinkParams {

    private Integer expiresInDays;
    private String password;
    private String lockMode;

    public CreateShareLinkParams() {}

    public CreateShareLinkParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    public CreateShareLinkParams password(String password) { this.password = password; return this; }
    public CreateShareLinkParams lockMode(String lockMode) { this.lockMode = lockMode; return this; }

    public @Nullable Integer getExpiresInDays() { return expiresInDays; }
    public @Nullable String getPassword() { return password; }
    public @Nullable String getLockMode() { return lockMode; }
}
