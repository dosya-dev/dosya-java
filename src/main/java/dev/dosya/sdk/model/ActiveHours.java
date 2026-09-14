package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * When a role may use the workspace.
 *
 * <pre>{@code
 * new ActiveHours("Europe/Berlin", Arrays.asList(1, 2, 3, 4, 5), "09:00", "17:00");
 * }</pre>
 *
 * @since 0.3.0
 */
public final class ActiveHours {

    private final String tz;
    private final List<Integer> days;
    private final String from;
    private final String to;

    /**
     * @param tz   IANA time zone
     * @param days weekdays, 0 = Sunday; at least one
     * @param from {@code HH:MM}
     * @param to   {@code HH:MM}
     */
    public ActiveHours(@NotNull String tz, @NotNull List<Integer> days, @NotNull String from, @NotNull String to) {
        this.tz = Objects.requireNonNull(tz, "tz");
        this.days = new ArrayList<>(Objects.requireNonNull(days, "days"));
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    public @NotNull String getTz() { return tz; }
    public @NotNull List<Integer> getDays() { return Collections.unmodifiableList(days); }
    public @NotNull String getFrom() { return from; }
    public @NotNull String getTo() { return to; }

    /** The wire object: {@code {tz, days, from, to}}. */
    public @NotNull Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tz", tz);
        body.put("days", new ArrayList<>(days));
        body.put("from", from);
        body.put("to", to);
        return body;
    }
}
