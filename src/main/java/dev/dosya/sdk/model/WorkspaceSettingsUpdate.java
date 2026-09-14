package dev.dosya.sdk.model;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A partial change to workspace settings for {@code workspaces().updateSettings()}.
 * Only the fields whose setter was called are sent.
 *
 * <p>Numeric setters take {@code null} to clear the limit. List setters take a list
 * (sent JSON-encoded, as the API stores it) or {@code null} to clear.
 *
 * <pre>{@code
 * new WorkspaceSettingsUpdate()
 *     .maxFileSizeGb(5)
 *     .require2fa(true)
 *     .ipAllowlist(Arrays.asList("192.168.1.0/24"));
 * }</pre>
 *
 * @since 0.3.0
 */
public final class WorkspaceSettingsUpdate {

    private static final Gson GSON = new Gson();

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public WorkspaceSettingsUpdate() {}

    /** Per-file cap in whole GB (not above the plan's storage). Needs {@code change_max_file_size}. */
    public @NotNull WorkspaceSettingsUpdate maxFileSizeGb(@Nullable Integer gb) { return put("max_file_size_gb", gb); }
    /** Needs {@code change_storage_per_member}. */
    public @NotNull WorkspaceSettingsUpdate maxStoragePerMemberGb(@Nullable Integer gb) { return put("max_storage_per_member_gb", gb); }
    /** At least 1; all caps across an owner's workspaces share one plan pool. Needs {@code change_total_storage_cap}. */
    public @NotNull WorkspaceSettingsUpdate maxTotalStorageGb(@Nullable Integer gb) { return put("max_total_storage_gb", gb); }
    /** Needs {@code change_max_concurrent_uploads}. */
    public @NotNull WorkspaceSettingsUpdate maxConcurrentUploads(@Nullable Integer count) { return put("max_concurrent_uploads", count); }
    public @NotNull WorkspaceSettingsUpdate sessionTimeoutMinutes(@Nullable Integer minutes) { return put("session_timeout_minutes", minutes); }
    public @NotNull WorkspaceSettingsUpdate shareMaxExpiryDays(@Nullable Integer days) { return put("share_max_expiry_days", days); }
    /** Whole days, 1-3650, and not above {@code shareMaxExpiryDays}. */
    public @NotNull WorkspaceSettingsUpdate defaultShareExpiryDays(@Nullable Integer days) { return put("default_share_expiry_days", days); }
    /** Downloads per hour per user; null = unlimited. */
    public @NotNull WorkspaceSettingsUpdate downloadRateLimit(@Nullable Integer perHour) { return put("download_rate_limit", perHour); }
    public @NotNull WorkspaceSettingsUpdate notifyOnUpload(boolean value) { return put("notify_on_upload", value); }
    public @NotNull WorkspaceSettingsUpdate autoShareLink(boolean value) { return put("auto_share_link", value); }
    public @NotNull WorkspaceSettingsUpdate downloadTracking(boolean value) { return put("download_tracking", value); }
    public @NotNull WorkspaceSettingsUpdate disableShareLinks(boolean value) { return put("disable_share_links", value); }
    public @NotNull WorkspaceSettingsUpdate forceSharePassword(boolean value) { return put("force_share_password", value); }
    public @NotNull WorkspaceSettingsUpdate require2fa(boolean value) { return put("require_2fa", value); }
    public @NotNull WorkspaceSettingsUpdate disablePasswordLogin(boolean value) { return put("disable_password_login", value); }
    public @NotNull WorkspaceSettingsUpdate recordUploadOrigin(boolean value) { return put("record_upload_origin", value); }
    /** Needs {@code change_duplicate_scan}. */
    public @NotNull WorkspaceSettingsUpdate duplicateScanEnabled(boolean value) { return put("duplicate_scan_enabled", value); }
    /** Comma-separated, dot-prefixed, e.g. {@code ".pdf,.docx"}. Needs {@code change_allowed_file_types}. */
    public @NotNull WorkspaceSettingsUpdate allowedExtensions(@Nullable String extensions) { return put("allowed_extensions", extensions); }
    /** Needs {@code change_blocked_file_types}. */
    public @NotNull WorkspaceSettingsUpdate blockedExtensions(@Nullable String extensions) { return put("blocked_extensions", extensions); }
    /** CIDRs, e.g. {@code ["192.168.1.0/24"]}. */
    public @NotNull WorkspaceSettingsUpdate ipAllowlist(@Nullable List<String> cidrs) { return putList("ip_allowlist", cidrs); }
    public @NotNull WorkspaceSettingsUpdate ipBlocklist(@Nullable List<String> cidrs) { return putList("ip_blocklist", cidrs); }
    /** ISO 3166-1 alpha-2 codes. */
    public @NotNull WorkspaceSettingsUpdate countryAllowlist(@Nullable List<String> codes) { return putList("country_allowlist", codes); }
    public @NotNull WorkspaceSettingsUpdate countryBlocklist(@Nullable List<String> codes) { return putList("country_blocklist", codes); }
    /** Domains invites are limited to, e.g. {@code ["company.com"]}. */
    public @NotNull WorkspaceSettingsUpdate allowedEmailDomains(@Nullable List<String> domains) { return putList("allowed_email_domains", domains); }

    /** True when no setter has been called. */
    public boolean isEmpty() { return fields.isEmpty(); }

    /** The request body: the set fields under their snake_case names. */
    public @NotNull Map<String, Object> toBody() {
        return new LinkedHashMap<>(fields);
    }

    private WorkspaceSettingsUpdate put(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    private WorkspaceSettingsUpdate putList(String key, List<String> values) {
        fields.put(key, values == null ? null : GSON.toJson(values));
        return this;
    }
}
