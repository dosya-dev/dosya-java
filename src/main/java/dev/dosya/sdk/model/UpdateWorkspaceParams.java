package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for renaming or re-iconing a workspace. Only non-null fields are sent.
 * The location cannot be changed after creation.
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

    public UpdateWorkspaceParams() {}

    /** 1-80 characters. */
    public @NotNull UpdateWorkspaceParams name(@Nullable String name) { this.name = name; return this; }
    /** Uppercased and truncated to 4 characters by the API. */
    public @NotNull UpdateWorkspaceParams iconInitials(@Nullable String iconInitials) { this.iconInitials = iconInitials; return this; }
    public @NotNull UpdateWorkspaceParams iconColor(@Nullable String iconColor) { this.iconColor = iconColor; return this; }

    public @Nullable String getName() { return name; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }

    /** The request body, with only the fields that were set. @since 0.3.0 */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (name != null) body.put("name", name);
        if (iconInitials != null) body.put("icon_initials", iconInitials);
        if (iconColor != null) body.put("icon_color", iconColor);
        return body;
    }
}
