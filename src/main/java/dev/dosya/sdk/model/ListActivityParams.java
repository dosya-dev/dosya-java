package dev.dosya.sdk.model;

import dev.dosya.sdk.internal.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parameters for listing a workspace's activity log. The {@code category}, {@code action} and
 * {@code userId} filters take one value or several (sent comma-joined).
 *
 * <pre>{@code
 * new ListActivityParams("ws_123")
 *     .category(ActivityCategory.FILES, ActivityCategory.SHARING)
 *     .action("file_uploaded")
 *     .page(1);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class ListActivityParams {

    private final String workspaceId;
    private Integer page;
    private Integer perPage;
    private String category;
    private String action;
    private String userId;

    public ListActivityParams(@NotNull String workspaceId) {
        this.workspaceId = Objects.requireNonNull(workspaceId, "workspaceId");
    }

    /** 1-based page. */
    public @NotNull ListActivityParams page(int page) { this.page = page; return this; }
    /** 10-100, default 30. Values below 10 are raised to 10. */
    public @NotNull ListActivityParams perPage(int perPage) { this.perPage = perPage; return this; }

    /**
     * One or more category wire values ({@code files}, {@code folders}, {@code sharing},
     * {@code members}, {@code workspace}, {@code comments}). Unknown values are ignored by the API;
     * if every value is unknown, no category filter applies at all.
     */
    public @NotNull ListActivityParams category(@NotNull String... categories) { this.category = join(Arrays.asList(categories)); return this; }
    /** One or more categories. @since 0.3.0 */
    public @NotNull ListActivityParams category(@NotNull ActivityCategory... categories) {
        List<String> values = new ArrayList<>(categories.length);
        for (ActivityCategory c : categories) values.add(c.value());
        this.category = join(values);
        return this;
    }
    /** One or more category wire values. @since 0.3.0 */
    public @NotNull ListActivityParams category(@NotNull List<String> categories) { this.category = join(categories); return this; }

    /** One or more action names, e.g. {@code file_uploaded}. */
    public @NotNull ListActivityParams action(@NotNull String... actions) { this.action = join(Arrays.asList(actions)); return this; }
    /** One or more action names. @since 0.3.0 */
    public @NotNull ListActivityParams action(@NotNull List<String> actions) { this.action = join(actions); return this; }

    /** One or more actor user ids (at most 50 are honoured). */
    public @NotNull ListActivityParams userId(@NotNull String... userIds) { this.userId = join(Arrays.asList(userIds)); return this; }
    /** One or more actor user ids (at most 50 are honoured). @since 0.3.0 */
    public @NotNull ListActivityParams userId(@NotNull List<String> userIds) { this.userId = join(userIds); return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable Integer getPage() { return page; }
    public @Nullable Integer getPerPage() { return perPage; }
    /** The comma-joined category filter, or null. */
    public @Nullable String getCategory() { return category; }
    /** The comma-joined action filter, or null. */
    public @Nullable String getAction() { return action; }
    /** The comma-joined user id filter, or null. */
    public @Nullable String getUserId() { return userId; }

    /** Adds the query parameters that were set. @since 0.3.0 */
    public @NotNull HttpRequest applyTo(@NotNull HttpRequest request) {
        return request
                .query("workspace_id", workspaceId)
                .query("page", page)
                .query("per_page", perPage)
                .query("category", category)
                .query("action", action)
                .query("user_id", userId);
    }

    private static String join(List<String> values) {
        if (values == null) return null;
        List<String> kept = new ArrayList<>();
        for (String v : values) if (v != null && !v.isEmpty()) kept.add(v);
        return kept.isEmpty() ? null : String.join(",", kept);
    }
}
