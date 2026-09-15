package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

/**
 * Who can open a share link.
 *
 * @since 0.3.0
 */
public enum ShareAccessMode {
    /** Anyone with the link. */
    @SerializedName("public") PUBLIC("public"),
    /** Only the named recipients, verified by an emailed code. */
    @SerializedName("restricted") RESTRICTED("restricted");

    private final String value;

    ShareAccessMode(String value) {
        this.value = value;
    }

    public @NotNull String value() {
        return value;
    }
}
