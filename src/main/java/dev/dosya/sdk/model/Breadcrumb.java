package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a breadcrumb item in the folder navigation path.
 *
 * @since 0.1.0
 */
public final class Breadcrumb {

    private String id;
    private String name;

    private Breadcrumb() {}

    public @NotNull String getId() { return id; }
    public @NotNull String getName() { return name; }
}
