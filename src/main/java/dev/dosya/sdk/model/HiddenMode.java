package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

/**
 * Who a hidden file or folder is hidden from.
 *
 * @since 0.3.0
 */
public enum HiddenMode {
    /** Not hidden. Setting it un-hides. */
    @SerializedName("none") NONE("none"),
    @SerializedName("everyone") EVERYONE("everyone"),
    /** Hidden from the users listed as targets. */
    @SerializedName("users") USERS("users"),
    /** Hidden from members holding the roles listed as targets. */
    @SerializedName("roles") ROLES("roles");

    private final String value;

    HiddenMode(String value) {
        this.value = value;
    }

    /** The API's wire value. */
    public @NotNull String value() {
        return value;
    }
}
