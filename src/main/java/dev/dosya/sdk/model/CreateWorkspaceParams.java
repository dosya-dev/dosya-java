package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters for creating a new workspace.
 *
 * <pre>{@code
 * CreateWorkspaceParams params = new CreateWorkspaceParams("My Workspace")
 *     .iconInitials("MW")
 *     .iconColor("#3B82F6")
 *     .defaultRegion("ap-southeast-2")
 *     .maxTotalStorageGb(100);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateWorkspaceParams {

    private final String name;
    private String iconInitials;
    private String iconColor;
    private String defaultRegion;
    private Integer maxTotalStorageGb;

    /** @param name 1-80 characters */
    public CreateWorkspaceParams(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /** Shown as given (keep it to 1-4 characters). */
    public @NotNull CreateWorkspaceParams iconInitials(@Nullable String iconInitials) { this.iconInitials = iconInitials; return this; }
    /** CSS colour, e.g. {@code "#3B82F6"}. */
    public @NotNull CreateWorkspaceParams iconColor(@Nullable String iconColor) { this.iconColor = iconColor; return this; }

    /**
     * Storage location code (see {@code regions().list()}). Fixed for the life of the
     * workspace. When omitted the server picks one near the caller.
     */
    public @NotNull CreateWorkspaceParams defaultRegion(@Nullable String defaultRegion) { this.defaultRegion = defaultRegion; return this; }

    /**
     * Whole-GB cap for this workspace, drawn from the owner's unallocated plan storage
     * (400 when it does not fit). Null leaves it uncapped.
     *
     * @since 0.3.0
     */
    public @NotNull CreateWorkspaceParams maxTotalStorageGb(@Nullable Integer maxTotalStorageGb) { this.maxTotalStorageGb = maxTotalStorageGb; return this; }

    public @NotNull String getName() { return name; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
    public @Nullable String getDefaultRegion() { return defaultRegion; }
    /** @since 0.3.0 */
    public @Nullable Integer getMaxTotalStorageGb() { return maxTotalStorageGb; }

    /** The request body, with only the fields that were set. @since 0.3.0 */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        if (iconInitials != null) body.put("icon_initials", iconInitials);
        if (iconColor != null) body.put("icon_color", iconColor);
        if (defaultRegion != null) body.put("default_region", defaultRegion);
        if (maxTotalStorageGb != null) body.put("max_total_storage_gb", maxTotalStorageGb);
        return body;
    }
}
