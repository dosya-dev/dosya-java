package dev.dosya.sdk.model;

import java.util.List;

public class ActivityListResponse {

    private List<ActivityEntry> activities;
    private List<Member> members;
    private Pagination pagination;

    private ActivityListResponse() {}

    public List<ActivityEntry> getActivities() { return activities; }
    public List<Member> getMembers() { return members; }
    public Pagination getPagination() { return pagination; }

    public static class Member {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;

        private Member() {}

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getAvatarUrl() { return avatarUrl; }
    }
}
