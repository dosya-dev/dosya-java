package dev.dosya.sdk;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.resource.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The main entry point for the Dosya Java SDK.
 *
 * <p>Provides access to all Dosya API resources through dedicated sub-resource accessors.
 * Create an instance with either a {@link DosyaClientOptions} object for full configuration
 * or a simple API key string for quick setup.</p>
 *
 * <pre>{@code
 * DosyaClient client = new DosyaClient("dos_your_api_key");
 * UserProfile profile = client.me().profile();
 * }</pre>
 *
 * @since 0.1.0
 */
public final class DosyaClient {

    private final FilesResource files;
    private final FavouritesResource favourites;
    private final FoldersResource folders;
    private final UploadResource upload;
    private final DownloadResource download;
    private final SharesResource shares;
    private final WorkspacesResource workspaces;
    private final TeamResource team;
    private final RolesResource roles;
    private final RegionsResource regions;
    private final FileRequestsResource fileRequests;
    private final SearchResource search;
    private final CommentsResource comments;
    private final MeResource me;
    private final ActivityResource activity;
    private final WebhooksResource webhooks;
    private final RemoteDownloadsResource remoteDownloads;

    /**
     * Creates a new {@code DosyaClient} with the given options.
     *
     * @param options the client configuration options
     * @throws NullPointerException     if {@code options} is {@code null}
     * @throws IllegalArgumentException if the API key does not start with {@code "dos_"}
     */
    public DosyaClient(@NotNull DosyaClientOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        if (!options.getApiKey().startsWith("dos_")) {
            throw new IllegalArgumentException("API key must start with 'dos_'");
        }

        DosyaHttpClient http = new DosyaHttpClient(options);
        this.files = new FilesResource(http);
        this.favourites = new FavouritesResource(http);
        this.folders = new FoldersResource(http);
        this.upload = new UploadResource(http);
        this.download = new DownloadResource(http);
        this.shares = new SharesResource(http);
        this.workspaces = new WorkspacesResource(http);
        this.team = new TeamResource(http);
        this.roles = new RolesResource(http);
        this.regions = new RegionsResource(http);
        this.fileRequests = new FileRequestsResource(http);
        this.search = new SearchResource(http);
        this.comments = new CommentsResource(http);
        this.me = new MeResource(http);
        this.activity = new ActivityResource(http);
        this.webhooks = new WebhooksResource(http);
        this.remoteDownloads = new RemoteDownloadsResource(http);
    }

    /**
     * Creates a new {@code DosyaClient} with a simple API key using default settings.
     *
     * @param apiKey the Dosya API key (must start with {@code "dos_"})
     * @throws IllegalArgumentException if the API key does not start with {@code "dos_"}
     */
    public DosyaClient(@NotNull String apiKey) {
        this(new DosyaClientOptions(apiKey));
    }

    /** Returns the files resource for file operations. */
    public @NotNull FilesResource files() { return files; }

    /** Returns the favourites resource. @since 0.3.0 */
    public @NotNull FavouritesResource favourites() { return favourites; }

    /** Returns the folders resource for folder operations. */
    public @NotNull FoldersResource folders() { return folders; }

    /** Returns the upload resource for file upload operations. */
    public @NotNull UploadResource upload() { return upload; }

    /** Returns the download resource for file download operations. */
    public @NotNull DownloadResource download() { return download; }

    /** Returns the shares resource for share link operations. */
    public @NotNull SharesResource shares() { return shares; }

    /** Returns the workspaces resource for workspace operations. */
    public @NotNull WorkspacesResource workspaces() { return workspaces; }

    /** Returns the team resource: members, invites and join links. @since 0.3.0 */
    public @NotNull TeamResource team() { return team; }

    /** Returns the roles resource. @since 0.3.0 */
    public @NotNull RolesResource roles() { return roles; }

    /** Returns the regions resource: valid workspace locations. @since 0.3.0 */
    public @NotNull RegionsResource regions() { return regions; }

    /** Returns the file requests resource for file request operations. */
    public @NotNull FileRequestsResource fileRequests() { return fileRequests; }

    /** Returns the search resource for search operations. */
    public @NotNull SearchResource search() { return search; }

    /** Returns the comments resource for comment operations. */
    public @NotNull CommentsResource comments() { return comments; }

    /** Returns the me resource for authenticated user operations. */
    public @NotNull MeResource me() { return me; }

    /** Returns the activity resource for activity log operations. */
    public @NotNull ActivityResource activity() { return activity; }

    /** Returns the webhooks resource: endpoint management and delivery logs. @since 0.3.0 */
    public @NotNull WebhooksResource webhooks() { return webhooks; }

    /** Returns the remote downloads resource: fetch a URL into a workspace. @since 0.3.0 */
    public @NotNull RemoteDownloadsResource remoteDownloads() { return remoteDownloads; }
}
