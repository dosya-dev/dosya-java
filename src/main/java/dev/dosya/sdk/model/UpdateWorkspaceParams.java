package dev.dosya.sdk.model;

import org.jetbrains.annotations.Nullable;

/**
 * Parameters for updating an existing workspace. Only non-null fields are applied.
 *
 * <pre>{@code
 * UpdateWorkspaceParams params = new UpdateWorkspaceParams()
 *     .name("Renamed Workspace")
 *     .iconColor("#EF4444");
 * }</pre>
 *
 * @since 0.1.0
 */
public final class UpdateWorkspaceParams {

    private String name;
    private String iconInitials;
    private String iconColor;
    private String defaultRegion;

    public UpdateWorkspaceParams() {}

    public UpdateWorkspaceParams name(String name) { this.name = name; return this; }
    public UpdateWorkspaceParams iconInitials(String iconInitials) { this.iconInitials = iconInitials; return this; }
    public UpdateWorkspaceParams iconColor(String iconColor) { this.iconColor = iconColor; return this; }
    public UpdateWorkspaceParams defaultRegion(String defaultRegion) { this.defaultRegion = defaultRegion; return this; }

    public @Nullable String getName() { return name; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
    public @Nullable String getDefaultRegion() { return defaultRegion; }
}
