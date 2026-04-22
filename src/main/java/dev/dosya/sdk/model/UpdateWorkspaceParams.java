package dev.dosya.sdk.model;

public class UpdateWorkspaceParams {

    private String name;
    private String iconInitials;
    private String iconColor;
    private String defaultRegion;

    public UpdateWorkspaceParams() {}

    public UpdateWorkspaceParams name(String name) { this.name = name; return this; }
    public UpdateWorkspaceParams iconInitials(String iconInitials) { this.iconInitials = iconInitials; return this; }
    public UpdateWorkspaceParams iconColor(String iconColor) { this.iconColor = iconColor; return this; }
    public UpdateWorkspaceParams defaultRegion(String defaultRegion) { this.defaultRegion = defaultRegion; return this; }

    public String getName() { return name; }
    public String getIconInitials() { return iconInitials; }
    public String getIconColor() { return iconColor; }
    public String getDefaultRegion() { return defaultRegion; }
}
