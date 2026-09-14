package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code roles().list()}.
 *
 * @since 0.3.0
 */
public final class RolesListResponse {

    private List<Role> roles;
    private List<String> allPermissions;

    private RolesListResponse() {}

    /** Built-in roles first, then custom roles by name. */
    public @NotNull List<Role> getRoles() {
        return roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
    }
    /** Every permission name the API knows. */
    public @NotNull List<String> getAllPermissions() {
        return allPermissions != null ? Collections.unmodifiableList(allPermissions) : Collections.emptyList();
    }
}
