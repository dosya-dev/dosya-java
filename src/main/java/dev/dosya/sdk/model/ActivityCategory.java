package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

/**
 * Activity log categories accepted by the {@code category} filter.
 *
 * @since 0.3.0
 */
public enum ActivityCategory {
    @SerializedName("files") FILES("files"),
    @SerializedName("folders") FOLDERS("folders"),
    @SerializedName("sharing") SHARING("sharing"),
    @SerializedName("members") MEMBERS("members"),
    @SerializedName("workspace") WORKSPACE("workspace"),
    @SerializedName("comments") COMMENTS("comments");

    private final String value;

    ActivityCategory(String value) {
        this.value = value;
    }

    /** The API's wire value. */
    public @NotNull String value() {
        return value;
    }
}
