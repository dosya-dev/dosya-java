package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for creating a custom role. Permissions not granted are denied.
 *
 * <pre>{@code
 * new CreateRoleParams("ws_1", "Contractor")
 *     .permission("upload_files", true)
 *     .activeHours(new ActiveHours("Europe/Berlin", Arrays.asList(1, 2, 3, 4, 5), "09:00", "17:00"));
 * }</pre>
 *
 * @since 0.3.0
 */
public final class CreateRoleParams {

    private final String workspaceId;
    private final UpdateRoleParams fields = new UpdateRoleParams();

    /** @param name 1-50 characters */
    public CreateRoleParams(@NotNull String workspaceId, @NotNull String name) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
        fields.name(Objects.requireNonNull(name, "name"));
    }

    /** Grants or denies one permission (snake_case name). Unknown names are ignored by the API. */
    public @NotNull CreateRoleParams permission(@NotNull String permission, boolean granted) { fields.permission(permission, granted); return this; }
    public @NotNull CreateRoleParams permissions(@NotNull Map<String, Boolean> permissions) { fields.permissions(permissions); return this; }
    /** Comma-separated IPs/CIDRs. */
    public @NotNull CreateRoleParams allowedIps(@Nullable String allowedIps) { fields.allowedIps(allowedIps); return this; }
    public @NotNull CreateRoleParams activeHours(@Nullable ActiveHours activeHours) { fields.activeHours(activeHours); return this; }
    /** Positive integer. */
    public @NotNull CreateRoleParams requestsPerMinute(@Nullable Long value) { fields.requestsPerMinute(value); return this; }
    public @NotNull CreateRoleParams egressBytesPerDay(@Nullable Long value) { fields.egressBytesPerDay(value); return this; }
    public @NotNull CreateRoleParams maxFileSizeBytes(@Nullable Long value) { fields.maxFileSizeBytes(value); return this; }
    public @NotNull CreateRoleParams maxConcurrentTransfers(@Nullable Long value) { fields.maxConcurrentTransfers(value); return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }

    /** The request body, with only the fields that were set. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workspace_id", workspaceId);
        body.putAll(fields.toBody());
        return body;
    }
}
