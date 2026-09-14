package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

/**
 * How a file or folder is locked.
 *
 * @since 0.3.0
 */
public enum LockMode {
    /** Not locked. Setting it removes a lock. */
    @SerializedName("none") NONE("none"),
    /** Visible but not downloadable or editable. */
    @SerializedName("view_only") VIEW_ONLY("view_only"),
    /** Requires the lock password to open. */
    @SerializedName("full_lock") FULL_LOCK("full_lock");

    private final String value;

    LockMode(String value) {
        this.value = value;
    }

    /** The API's wire value. */
    public @NotNull String value() {
        return value;
    }
}
