package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response returned when listing activity entries for a workspace.
 *
 * <p>Contains the activity entries, workspace members involved, and pagination metadata.</p>
 *
 * @since 0.1.0
 */
public final class ActivityListResponse {

    private List<ActivityEntry> activities;
    private List<Member> members;
    private Pagination pagination;

    private ActivityListResponse() {}

    public @NotNull List<ActivityEntry> getActivities() { return activities != null ? Collections.unmodifiableList(activities) : Collections.<ActivityEntry>emptyList(); }
    public @NotNull List<Member> getMembers() { return members != null ? Collections.unmodifiableList(members) : Collections.<Member>emptyList(); }
    public @Nullable Pagination getPagination() { return pagination; }

    /**
     * A workspace member referenced in activity entries.
     *
     * @since 0.1.0
     */
    public static final class Member {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;

        private Member() {}

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public @Nullable String getAvatarUrl() { return avatarUrl; }
    }
}
