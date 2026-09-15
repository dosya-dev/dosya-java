package dev.dosya.sdk.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

/**
 * Where a share link stands. {@code EXPIRING} means it expires within 3 days.
 *
 * @since 0.3.0
 */
public enum ShareStatus {
    @SerializedName("active") ACTIVE("active"),
    @SerializedName("expiring") EXPIRING("expiring"),
    @SerializedName("expired") EXPIRED("expired"),
    @SerializedName("revoked") REVOKED("revoked");

    private final String value;

    ShareStatus(String value) {
        this.value = value;
    }

    /** The wire string. */
    public @NotNull String value() {
        return value;
    }
}
