package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Parameters for creating a share bundle containing multiple files.
 *
 * <pre>{@code
 * CreateShareBundleParams params = new CreateShareBundleParams(Arrays.asList("file_1", "file_2"))
 *     .expiresInDays(30)
 *     .password("secret");
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateShareBundleParams {

    private final List<String> fileIds;
    private Integer expiresInDays;
    private String password;

    public CreateShareBundleParams(@NotNull List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public CreateShareBundleParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    public CreateShareBundleParams password(String password) { this.password = password; return this; }

    public @NotNull List<String> getFileIds() { return fileIds; }
    public @Nullable Integer getExpiresInDays() { return expiresInDays; }
    public @Nullable String getPassword() { return password; }
}
