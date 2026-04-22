package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for creating a new workspace.
 *
 * <pre>{@code
 * CreateWorkspaceParams params = new CreateWorkspaceParams("My Workspace")
 *     .iconInitials("MW")
 *     .iconColor("#3B82F6")
 *     .defaultRegion("eu-west-1");
 * }</pre>
 *
 * @since 0.1.0
 */
public final class CreateWorkspaceParams {

    private final String name;
    private String iconInitials;
    private String iconColor;
    private String defaultRegion;

    public CreateWorkspaceParams(@NotNull String name) {
        this.name = name;
    }

    public CreateWorkspaceParams iconInitials(String iconInitials) { this.iconInitials = iconInitials; return this; }
    public CreateWorkspaceParams iconColor(String iconColor) { this.iconColor = iconColor; return this; }
    public CreateWorkspaceParams defaultRegion(String defaultRegion) { this.defaultRegion = defaultRegion; return this; }

    public @NotNull String getName() { return name; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
    public @Nullable String getDefaultRegion() { return defaultRegion; }
}
