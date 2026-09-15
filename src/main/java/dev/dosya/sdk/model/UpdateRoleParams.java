package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A change to a custom role. Only the fields whose setter was called are sent; permissions
 * are merged per key. A null condition or limit clears it.
 *
 * <pre>{@code
 * new UpdateRoleParams()
 *     .permission("upload_files", true)
 *     .requestsPerMinute(null);
 * }</pre>
 *
 * @since 0.3.0
 */
public final class UpdateRoleParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();
    private Map<String, Boolean> permissions;

    public UpdateRoleParams() {}

    /** 1-50 characters. */
    public @NotNull UpdateRoleParams name(@NotNull String name) { fields.put("name", name); return this; }

    /** Grants or revokes one permission (snake_case name). Unknown names are ignored by the API. */
    public @NotNull UpdateRoleParams permission(@NotNull String permission, boolean granted) {
        if (permissions == null) permissions = new LinkedHashMap<>();
        permissions.put(permission, granted);
        return this;
    }

    /** Grants or revokes several permissions (snake_case names). */
    public @NotNull UpdateRoleParams permissions(@NotNull Map<String, Boolean> permissions) {
        for (Map.Entry<String, Boolean> e : permissions.entrySet()) {
            permission(e.getKey(), Boolean.TRUE.equals(e.getValue()));
        }
        return this;
    }

    /** Comma-separated IPs/CIDRs; null clears. */
    public @NotNull UpdateRoleParams allowedIps(@Nullable String allowedIps) { fields.put("allowed_ips", allowedIps); return this; }
    /** When the role may be used; null clears. */
    public @NotNull UpdateRoleParams activeHours(@Nullable ActiveHours activeHours) {
        fields.put("active_hours", activeHours == null ? null : activeHours.toBody());
        return this;
    }
    /** Positive integer; null clears. */
    public @NotNull UpdateRoleParams requestsPerMinute(@Nullable Long value) { fields.put("requests_per_minute", value); return this; }
    public @NotNull UpdateRoleParams egressBytesPerDay(@Nullable Long value) { fields.put("egress_bytes_per_day", value); return this; }
    public @NotNull UpdateRoleParams maxFileSizeBytes(@Nullable Long value) { fields.put("max_file_size_bytes", value); return this; }
    public @NotNull UpdateRoleParams maxConcurrentTransfers(@Nullable Long value) { fields.put("max_concurrent_transfers", value); return this; }

    /** The request body, with only the fields that were set. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>(fields);
        if (permissions != null) body.put("permissions", new LinkedHashMap<>(permissions));
        return body;
    }
}
