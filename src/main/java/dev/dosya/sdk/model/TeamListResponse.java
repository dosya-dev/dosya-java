package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code team().list()}: members, pending invites, recent activity and counts.
 *
 * @since 0.3.0
 */
public final class TeamListResponse {

    private Workspace workspace;
    private List<TeamMember> members;
    private List<TeamInvite> invites;
    private List<TeamActivityItem> activity;
    private Stats stats;

    private TeamListResponse() {}

    public @Nullable Workspace getWorkspace() { return workspace; }
    /** Oldest first, no paging. */
    public @NotNull List<TeamMember> getMembers() { return list(members); }
    /** Pending, unexpired invites, newest first. */
    public @NotNull List<TeamInvite> getInvites() { return list(invites); }
    /** The 6 most recent workspace events. */
    public @NotNull List<TeamActivityItem> getActivity() { return list(activity); }
    public @Nullable Stats getStats() { return stats; }

    private static <T> List<T> list(List<T> l) {
        return l != null ? Collections.unmodifiableList(l) : Collections.emptyList();
    }

    /** The workspace's name and icon. */
    public static final class Workspace {
        private String name;
        private String iconInitials;
        private String iconColor;

        private Workspace() {}

        public @NotNull String getName() { return name; }
        public @Nullable String getIconInitials() { return iconInitials; }
        public @Nullable String getIconColor() { return iconColor; }
    }

    /** Team counts. */
    public static final class Stats {
        private int members;
        private int pending;
        private int sharesThisWeek;

        private Stats() {}

        public int getMembers() { return members; }
        public int getPending() { return pending; }
        public int getSharesThisWeek() { return sharesThisWeek; }
    }
}
