package dev.dosya.sdk.webhook;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Every event type a webhook endpoint can subscribe to.
 *
 * @since 0.3.0
 */
public enum WebhookEventType {
    /** A file was uploaded, or a new version of it was. Also the synthetic event sent by {@code webhooks().test()}. */
    @SerializedName("file.uploaded") FILE_UPLOADED("file.uploaded"),
    /** A file was moved to trash or purged ({@code data.permanent}). */
    @SerializedName("file.deleted") FILE_DELETED("file.deleted"),
    /**
     * A share link was viewed or downloaded. The payload contains the public share
     * {@code token}, which grants access to the link - treat webhook bodies as secrets.
     */
    @SerializedName("share.accessed") SHARE_ACCESSED("share.accessed");

    private final String value;

    WebhookEventType(String value) {
        this.value = value;
    }

    /** The API's wire value, e.g. {@code file.uploaded}. */
    public @NotNull String value() {
        return value;
    }

    /**
     * Looks up an event type by its wire value.
     *
     * @return the matching type, or {@code null} for a value this SDK version does not know
     */
    public static @Nullable WebhookEventType fromValue(@Nullable String value) {
        if (value == null) return null;
        for (WebhookEventType t : values()) {
            if (t.value.equals(value)) return t;
        }
        return null;
    }
}
