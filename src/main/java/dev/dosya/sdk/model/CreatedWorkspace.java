package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The workspace as returned right after {@code workspaces().create()}.
 *
 * @since 0.3.0
 */
public final class CreatedWorkspace {

    private String id;
    private String slug;
    private String name;
    private String iconInitials;
    private String iconColor;

    private CreatedWorkspace() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getSlug() { return slug; }
    public @NotNull String getName() { return name; }
    public @Nullable String getIconInitials() { return iconInitials; }
    public @Nullable String getIconColor() { return iconColor; }
}
