package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A change to a file request. Only the fields whose setter was called are sent.
 *
 * <pre>{@code
 * new UpdateFileRequestParams().title("Q3 receipts").expiresInDays(null).maxFiles(20);
 * }</pre>
 *
 * @since 0.3.0
 */
public final class UpdateFileRequestParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public UpdateFileRequestParams() {}

    /** Null or empty clears. */
    public @NotNull UpdateFileRequestParams title(@Nullable String title) { fields.put("title", title); return this; }
    /** Null or empty clears. */
    public @NotNull UpdateFileRequestParams message(@Nullable String message) { fields.put("message", message); return this; }
    /** Days from now; null or 0 removes the expiry. */
    public @NotNull UpdateFileRequestParams expiresInDays(@Nullable Integer days) { fields.put("expires_in_days", days); return this; }
    /** At least 8 characters; empty removes the password. */
    public @NotNull UpdateFileRequestParams password(@NotNull String password) { fields.put("password", password); return this; }
    /** Removes the password. */
    public @NotNull UpdateFileRequestParams clearPassword() { return password(""); }
    /** Moves the destination; null means the workspace root. 403 {@code folder_locked} for a locked folder. */
    public @NotNull UpdateFileRequestParams folderId(@Nullable String folderId) { fields.put("folder_id", folderId); return this; }
    /** Comma-separated, e.g. {@code ".pdf,.docx"}; null clears. */
    public @NotNull UpdateFileRequestParams allowedExtensions(@Nullable String extensions) { fields.put("allowed_extensions", extensions); return this; }
    /** Null or 0 removes the cap. */
    public @NotNull UpdateFileRequestParams maxFileSizeMb(@Nullable Integer mb) { fields.put("max_file_size_mb", mb); return this; }
    /** Null or 0 removes the cap. */
    public @NotNull UpdateFileRequestParams maxFiles(@Nullable Integer maxFiles) { fields.put("max_files", maxFiles); return this; }

    /** The request body, with only the fields that were set. */
    public @NotNull Map<String, Object> toBody() {
        return new LinkedHashMap<>(fields);
    }
}
