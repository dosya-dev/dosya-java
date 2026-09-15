package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Per-link analytics from {@code shares().analytics()}.
 *
 * @since 0.3.0
 */
public final class ShareAnalytics {

    private Link link;
    private int range;
    private List<Day> timeline;
    private Reach reach;
    private List<Recipient> recipients;
    private Log log;
    private Gaps gaps;

    private ShareAnalytics() {}

    public @NotNull Link getLink() { return link; }
    /** The range in days (7, 30 or 90). */
    public int getRange() { return range; }
    /** One row per day in the range. */
    public @NotNull List<Day> getTimeline() { return list(timeline); }
    public @Nullable Reach getReach() { return reach; }
    /** Invited recipients for restricted links; null for public links. */
    public @Nullable List<Recipient> getRecipients() {
        return recipients != null ? Collections.unmodifiableList(recipients) : null;
    }
    public @Nullable Log getLog() { return log; }
    /** What the analytics cannot report, as human-readable notes. */
    public @Nullable Gaps getGaps() { return gaps; }

    private static <T> List<T> list(List<T> l) {
        return l != null ? Collections.unmodifiableList(l) : Collections.emptyList();
    }

    /** The link the analytics describe. */
    public static final class Link {
        private String linkId;
        private String url;
        private String displayName;
        private boolean isFolder;
        private boolean isBundle;
        private String fileId;
        private Long sizeBytes;
        private String extension;
        private String region;
        private long createdAt;
        private String createdBy;
        private String createdByName;
        private boolean isMine;
        private Long expiresAt;
        private boolean isRevoked;
        private Long revokedAt;
        private boolean isPasswordProtected;
        private LockMode lockMode;
        private ShareAccessMode accessMode;
        private ShareStatus status;
        private int viewCount;
        private int downloadCount;
        private Integer maxDownloads;
        private Integer downloadsLeft;

        private Link() {}

        public @NotNull String getLinkId() { return linkId; }
        public @NotNull String getUrl() { return url; }
        public @NotNull String getDisplayName() { return displayName; }
        public boolean isFolder() { return isFolder; }
        public boolean isBundle() { return isBundle; }
        public @Nullable String getFileId() { return fileId; }
        public @Nullable Long getSizeBytes() { return sizeBytes; }
        public @Nullable String getExtension() { return extension; }
        public @Nullable String getRegion() { return region; }
        public long getCreatedAt() { return createdAt; }
        public @NotNull String getCreatedBy() { return createdBy; }
        public @Nullable String getCreatedByName() { return createdByName; }
        public boolean isMine() { return isMine; }
        public @Nullable Long getExpiresAt() { return expiresAt; }
        public boolean isRevoked() { return isRevoked; }
        public @Nullable Long getRevokedAt() { return revokedAt; }
        public boolean isPasswordProtected() { return isPasswordProtected; }
        public @NotNull LockMode getLockMode() { return lockMode != null ? lockMode : LockMode.NONE; }
        public @NotNull ShareAccessMode getAccessMode() { return accessMode != null ? accessMode : ShareAccessMode.PUBLIC; }
        public @Nullable ShareStatus getStatus() { return status; }
        public int getViewCount() { return viewCount; }
        public int getDownloadCount() { return downloadCount; }
        public @Nullable Integer getMaxDownloads() { return maxDownloads; }
        /** Null when downloads are not capped. */
        public @Nullable Integer getDownloadsLeft() { return downloadsLeft; }
    }

    /** One day of the timeline. */
    public static final class Day {
        private String day;
        private int opens;
        private int downloads;

        private Day() {}

        /** {@code YYYY-MM-DD}. */
        public @NotNull String getDay() { return day; }
        /** First opens per distinct address. */
        public int getOpens() { return opens; }
        public int getDownloads() { return downloads; }
    }

    /** Distinct visitors and their devices and browsers. */
    public static final class Reach {
        private int visitors;
        private boolean truncated;
        private List<Tally> devices;
        private List<Tally> browsers;

        private Reach() {}

        public int getVisitors() { return visitors; }
        /** True when there were more visitors than the 5000 counted. */
        public boolean isTruncated() { return truncated; }
        public @NotNull List<Tally> getDevices() { return list(devices); }
        public @NotNull List<Tally> getBrowsers() { return list(browsers); }
    }

    /** A label and how often it occurred. */
    public static final class Tally {
        private String label;
        private int count;

        private Tally() {}

        public @NotNull String getLabel() { return label; }
        public int getCount() { return count; }
    }

    /** A recipient of a restricted link. */
    public static final class Recipient {
        private String email;
        private Long verifiedAt;
        private long invitedAt;

        private Recipient() {}

        public @NotNull String getEmail() { return email; }
        public @Nullable Long getVerifiedAt() { return verifiedAt; }
        public long getInvitedAt() { return invitedAt; }
    }

    /** One page of the access log (25 rows per page). */
    public static final class Log {
        private int total;
        private int offset;
        private int limit;
        private List<LogRow> rows;

        private Log() {}

        public int getTotal() { return total; }
        public int getOffset() { return offset; }
        public int getLimit() { return limit; }
        public @NotNull List<LogRow> getRows() { return list(rows); }
    }

    /** One access log entry. */
    public static final class LogRow {
        private String id;
        private String visitor;
        private String event;
        private String device;
        private String deviceLabel;
        private String country;
        private long viewedAt;

        private LogRow() {}

        public @NotNull String getId() { return id; }
        /** A short opaque handle, not an identity. */
        public @NotNull String getVisitor() { return visitor; }
        /** {@code view} or {@code download}. */
        public @NotNull String getEvent() { return event; }
        public @Nullable String getDevice() { return device; }
        public @Nullable String getDeviceLabel() { return deviceLabel; }
        /** ISO 3166-1 alpha-2, or null. */
        public @Nullable String getCountry() { return country; }
        public long getViewedAt() { return viewedAt; }
    }

    /** Notes on what the analytics cannot answer. */
    public static final class Gaps {
        private String repeatVisits;
        private String perRecipient;

        private Gaps() {}

        public @Nullable String getRepeatVisits() { return repeatVisits; }
        public @Nullable String getPerRecipient() { return perRecipient; }
    }
}
