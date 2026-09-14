package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * One page of a workspace activity log, newest first.
 *
 * @since 0.1.0
 */
public final class ActivityListResponse {

    private List<ActivityEntry> activities;
    private List<Member> members;
    private ActivityPagination pagination;

    private ActivityListResponse() {}

    public @NotNull List<ActivityEntry> getActivities() { return activities != null ? Collections.unmodifiableList(activities) : Collections.emptyList(); }
    /** Every workspace member, for filter pickers. */
    public @NotNull List<Member> getMembers() { return members != null ? Collections.unmodifiableList(members) : Collections.emptyList(); }
    /** @since 0.3.0 (was the file-listing {@code Pagination}, whose total fields never matched) */
    public @NotNull ActivityPagination getPagination() { return pagination; }

    /**
     * A workspace member.
     *
     * @since 0.1.0
     */
    public static final class Member {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;

        private Member() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        public @NotNull String getEmail() { return email; }
        public @Nullable String getAvatarUrl() { return avatarUrl; }
    }
}
