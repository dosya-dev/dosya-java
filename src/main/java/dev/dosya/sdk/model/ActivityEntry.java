package dev.dosya.sdk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One activity log entry.
 *
 * @since 0.1.0
 */
public final class ActivityEntry {

    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String resourceName;
    private long createdAt;
    private String userId;
    private String userName;
    private String userEmail;
    private String userAvatar;
    private String actorId;
    private String actorType;
    private String outcome;
    private String source;
    private String actionGroup;
    private String onBehalfOf;
    private String oboName;
    private String oboEmail;
    private String requestId;
    private String sessionId;
    private String sourceIp;
    private String userAgent;
    private JsonElement geo;
    private JsonElement meta;
    private String metadata;

    private ActivityEntry() {}

    public @NotNull String getId() { return id; }
    /** e.g. {@code file_uploaded}, {@code folder_renamed}. */
    public @NotNull String getAction() { return action; }
    /** e.g. {@code file}, {@code folder}, {@code member}. @since 0.3.0 */
    public @NotNull String getEntityType() { return entityType; }
    /** @since 0.3.0 */
    public @Nullable String getEntityId() { return entityId; }

    /** @deprecated the API calls it {@code entity_type}; use {@link #getEntityType()} (this returned null in 0.2.x). */
    @Deprecated
    public @Nullable String getResourceType() { return entityType; }

    /** @deprecated the API calls it {@code entity_id}; use {@link #getEntityId()} (this returned null in 0.2.x). */
    @Deprecated
    public @Nullable String getResourceId() { return entityId; }

    /** Null when the caller cannot open the item. */
    public @Nullable String getResourceName() { return resourceName; }
    /** Unix seconds. */
    public long getCreatedAt() { return createdAt; }
    /** Null for system actors and for actors whose account no longer exists (see {@link #getActorId()}). */
    public @Nullable String getUserId() { return userId; }
    public @Nullable String getUserName() { return userName; }
    /** @since 0.3.0 */
    public @Nullable String getUserEmail() { return userEmail; }
    /** @since 0.3.0 */
    public @Nullable String getUserAvatar() { return userAvatar; }
    /** @since 0.3.0 */
    public @Nullable String getActorId() { return actorId; }
    /** @since 0.3.0 */
    public @NotNull String getActorType() { return actorType; }
    /** @since 0.3.0 */
    public @NotNull String getOutcome() { return outcome; }
    /** @since 0.3.0 */
    public @NotNull String getSource() { return source; }
    /** @since 0.3.0 */
    public @NotNull String getActionGroup() { return actionGroup; }
    /** @since 0.3.0 */
    public @Nullable String getOnBehalfOf() { return onBehalfOf; }
    /** @since 0.3.0 */
    public @Nullable String getOboName() { return oboName; }
    /** @since 0.3.0 */
    public @Nullable String getOboEmail() { return oboEmail; }
    /** Null for non-owner/admin callers viewing other members' rows. @since 0.3.0 */
    public @Nullable String getRequestId() { return requestId; }
    /** Null for non-owner/admin callers viewing other members' rows. @since 0.3.0 */
    public @Nullable String getSessionId() { return sessionId; }
    /** Null for non-owner/admin callers viewing other members' rows. @since 0.3.0 */
    public @Nullable String getSourceIp() { return sourceIp; }
    /** Null for non-owner/admin callers viewing other members' rows. @since 0.3.0 */
    public @Nullable String getUserAgent() { return userAgent; }

    /** Geo data, when recorded and visible to the caller. @since 0.3.0 */
    public @Nullable JsonObject getGeo() {
        return geo != null && geo.isJsonObject() ? geo.getAsJsonObject().deepCopy() : null;
    }

    /**
     * Parsed metadata with its original snake_case keys (e.g. {@code old_name}). Reduced to a safe
     * subset for callers who are not owner or admin viewing other members' rows.
     *
     * @since 0.3.0
     */
    public @Nullable JsonObject getMeta() {
        return meta != null && meta.isJsonObject() ? meta.getAsJsonObject().deepCopy() : null;
    }

    /**
     * {@link #getMeta()} as plain Java values, keys unchanged: strings, {@code Boolean},
     * {@code Long} for whole numbers, {@code Double} otherwise, {@code List} and {@code Map}.
     *
     * @since 0.3.0
     */
    public @Nullable Map<String, Object> getMetaMap() {
        if (meta == null || !meta.isJsonObject()) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) toJava(meta);
        return map;
    }

    /** The same metadata as a raw JSON string. */
    public @Nullable String getMetadata() { return metadata; }

    private static Object toJava(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        if (e.isJsonObject()) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
                out.put(en.getKey(), toJava(en.getValue()));
            }
            return Collections.unmodifiableMap(out);
        }
        if (e.isJsonArray()) {
            JsonArray arr = e.getAsJsonArray();
            List<Object> out = new ArrayList<>(arr.size());
            for (JsonElement item : arr) out.add(toJava(item));
            return Collections.unmodifiableList(out);
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) {
            BigDecimal d = p.getAsBigDecimal();
            try {
                return d.longValueExact();
            } catch (ArithmeticException notWhole) {
                return d.doubleValue();
            }
        }
        return p.getAsString();
    }
}
