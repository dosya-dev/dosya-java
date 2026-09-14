package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parameters for listing folders and files in a workspace folder, the trash,
 * hidden items or a folder group.
 *
 * <pre>{@code
 * ListFilesParams params = new ListFilesParams("ws_123")
 *     .folderId("fld_456")
 *     .sort(FileSort.TAKEN).dir("asc")
 *     .perPage(200);
 * }</pre>
 *
 * @since 0.1.0
 */
public final class ListFilesParams {

    private final String workspaceId;
    private String folderId;
    private String filter;
    private String sort;
    private String dir;
    private String q;
    private Boolean deleted;
    private Boolean hidden;
    private String unlockToken;
    private String groupId;
    private String folder;
    private Integer page;
    private Integer perPage;

    public ListFilesParams(@NotNull String workspaceId) {
        this.workspaceId = workspaceId;
    }

    /** The folder to list; null for the workspace root (a folder-confined member gets their folder). */
    public ListFilesParams folderId(@Nullable String folderId) { this.folderId = folderId; return this; }
    /** {@code all}, {@code documents}, {@code videos} or {@code images}. */
    public ListFilesParams filter(@Nullable String filter) { this.filter = filter; return this; }
    /**
     * Sort order, see {@link FileSort}: a legacy mode ({@code newest}, {@code oldest},
     * {@code largest}, {@code smallest}), {@code <column>_asc|_desc}, or a bare column
     * with {@link #dir(String)}. Unknown values fall back to {@code newest}.
     */
    public ListFilesParams sort(@Nullable String sort) { this.sort = sort; return this; }
    /** {@code asc} or {@code desc}; applies to a bare column in {@link #sort(String)} only. @since 0.3.0 */
    public ListFilesParams dir(@Nullable String dir) { this.dir = dir; return this; }
    public ListFilesParams q(@Nullable String q) { this.q = q; return this; }
    /** List the trash instead of live items. */
    public ListFilesParams deleted(boolean deleted) { this.deleted = deleted; return this; }
    /** Show only hidden items. Needs the {@code hide_files} permission, otherwise ignored. */
    public ListFilesParams hidden(boolean hidden) { this.hidden = hidden; return this; }
    /** Folder unlock token; required to list a {@code full_lock} folder. @since 0.3.0 */
    public ListFilesParams unlockToken(@Nullable String unlockToken) { this.unlockToken = unlockToken; return this; }
    /** List a folder group instead of a folder. @since 0.3.0 */
    public ListFilesParams groupId(@Nullable String groupId) { this.groupId = groupId; return this; }
    /** With {@link #deleted(boolean)}, browse inside this trashed folder. Ignored otherwise. @since 0.3.0 */
    public ListFilesParams folder(@Nullable String folder) { this.folder = folder; return this; }
    /** 1-based. */
    public ListFilesParams page(int page) { this.page = page; return this; }
    /** Clamped to 10-500 by the server. Default 100. */
    public ListFilesParams perPage(int perPage) { this.perPage = perPage; return this; }

    public @NotNull String getWorkspaceId() { return workspaceId; }
    public @Nullable String getFolderId() { return folderId; }
    public @Nullable String getFilter() { return filter; }
    public @Nullable String getSort() { return sort; }
    /** @since 0.3.0 */
    public @Nullable String getDir() { return dir; }
    public @Nullable String getQ() { return q; }
    public @Nullable Boolean getDeleted() { return deleted; }
    public @Nullable Boolean getHidden() { return hidden; }
    /** @since 0.3.0 */
    public @Nullable String getUnlockToken() { return unlockToken; }
    /** @since 0.3.0 */
    public @Nullable String getGroupId() { return groupId; }
    /** @since 0.3.0 */
    public @Nullable String getFolder() { return folder; }
    public @Nullable Integer getPage() { return page; }
    public @Nullable Integer getPerPage() { return perPage; }
}
