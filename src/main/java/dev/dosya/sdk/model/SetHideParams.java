package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Who to hide a file or folder from. Replaces any existing hide rules.
 *
 * <pre>{@code
 * SetHideParams.everyone();
 * SetHideParams.users(Arrays.asList("usr_1", "usr_2"));
 * SetHideParams.none(); // un-hide
 * }</pre>
 *
 * @since 0.3.0
 */
public final class SetHideParams {

    private final HiddenMode mode;
    private final List<String> targets;

    private SetHideParams(HiddenMode mode, List<String> targets) {
        this.mode = mode;
        this.targets = targets;
    }

    public static @NotNull SetHideParams none() { return new SetHideParams(HiddenMode.NONE, Collections.emptyList()); }
    public static @NotNull SetHideParams everyone() { return new SetHideParams(HiddenMode.EVERYONE, Collections.emptyList()); }

    /** Hide from these users. At least one id. */
    public static @NotNull SetHideParams users(@NotNull List<String> userIds) {
        return new SetHideParams(HiddenMode.USERS, nonEmpty(userIds));
    }

    /** Hide from members holding these roles. At least one id. */
    public static @NotNull SetHideParams roles(@NotNull List<String> roleIds) {
        return new SetHideParams(HiddenMode.ROLES, nonEmpty(roleIds));
    }

    public @NotNull HiddenMode getMode() { return mode; }
    public @NotNull List<String> getTargets() { return Collections.unmodifiableList(targets); }

    /** The request body: {@code hidden_mode} and {@code targets}. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("hidden_mode", mode.value());
        body.put("targets", new ArrayList<>(targets));
        return body;
    }

    private static List<String> nonEmpty(List<String> ids) {
        Objects.requireNonNull(ids, "ids");
        if (ids.isEmpty()) throw new IllegalArgumentException("At least one target id is required");
        return new ArrayList<>(ids);
    }
}
