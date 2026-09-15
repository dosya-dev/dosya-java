package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * The hidden state of a file or folder.
 *
 * @since 0.3.0
 */
public final class HideInfo {

    private boolean isHidden;
    private HiddenMode hiddenMode;
    private List<Rule> rules;

    private HideInfo() {}

    public boolean isHidden() { return isHidden; }
    public @NotNull HiddenMode getHiddenMode() { return hiddenMode != null ? hiddenMode : HiddenMode.NONE; }
    public @NotNull List<Rule> getRules() { return rules != null ? Collections.unmodifiableList(rules) : Collections.emptyList(); }

    /** One hide target. */
    public static final class Rule {
        private String targetType;
        private String targetId;

        private Rule() {}

        /** {@code "user"} or {@code "role"}. */
        public @NotNull String getTargetType() { return targetType; }
        public @NotNull String getTargetId() { return targetId; }
    }
}
