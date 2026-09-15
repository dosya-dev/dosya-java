package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * A storage location a workspace can be created in.
 *
 * @since 0.3.0
 */
public final class Region {

    private String code;
    private String city;
    private String country;
    private String continent;
    private String flag;

    private Region() {}

    /** Pass to {@code CreateWorkspaceParams.defaultRegion()}, e.g. {@code "ap-southeast-2"}. */
    public @NotNull String getCode() { return code; }
    public @NotNull String getCity() { return city; }
    public @NotNull String getCountry() { return country; }
    public @NotNull String getContinent() { return continent; }
    /** Emoji flag. */
    public @NotNull String getFlag() { return flag; }
}
