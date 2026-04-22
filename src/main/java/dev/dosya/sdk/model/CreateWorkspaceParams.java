package dev.dosya.sdk.model;

public class CreateWorkspaceParams {

    private final String name;
    private String iconInitials;
    private String iconColor;
    private String defaultRegion;

    public CreateWorkspaceParams(String name) {
        this.name = name;
    }

    public CreateWorkspaceParams iconInitials(String iconInitials) { this.iconInitials = iconInitials; return this; }
    public CreateWorkspaceParams iconColor(String iconColor) { this.iconColor = iconColor; return this; }
    public CreateWorkspaceParams defaultRegion(String defaultRegion) { this.defaultRegion = defaultRegion; return this; }

    public String getName() { return name; }
    public String getIconInitials() { return iconInitials; }
    public String getIconColor() { return iconColor; }
    public String getDefaultRegion() { return defaultRegion; }
}
