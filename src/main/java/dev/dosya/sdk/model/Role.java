package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * A built-in or custom workspace role with its permissions, conditions and usage limits.
 *
 * @since 0.3.0
 */
public final class Role {

    private String id;
    private String name;
    private boolean isBuiltin;
    private boolean isCustom;
    private Map<String, Boolean> permissions;
    private String allowedIps;
    private String activeHours;
    private Long requestsPerMinute;
    private Long egressBytesPerDay;
    private Long maxFileSizeBytes;
    private Long maxConcurrentTransfers;

    private Role() {}

    /** {@code role_owner}, {@code role_admin}, {@code role_member}, {@code role_viewer}, or a custom role id. */
    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
    public boolean isBuiltin() { return isBuiltin; }
    public boolean isCustom() { return isCustom; }
    /** Every permission name (snake_case, e.g. {@code upload_files}) to whether it is granted. */
    public @NotNull Map<String, Boolean> getPermissions() {
        return permissions != null ? Collections.unmodifiableMap(permissions) : Collections.emptyMap();
    }
    /** True when {@code permission} (snake_case) is granted. */
    public boolean has(@NotNull String permission) {
        return permissions != null && Boolean.TRUE.equals(permissions.get(permission));
    }
    /** Comma-separated IPs/CIDRs, or null. Always null on built-in roles. */
    public @Nullable String getAllowedIps() { return allowedIps; }
    /** JSON text of an {@link ActiveHours} object, or null. */
    public @Nullable String getActiveHours() { return activeHours; }
    public @Nullable Long getRequestsPerMinute() { return requestsPerMinute; }
    public @Nullable Long getEgressBytesPerDay() { return egressBytesPerDay; }
    public @Nullable Long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public @Nullable Long getMaxConcurrentTransfers() { return maxConcurrentTransfers; }
}
