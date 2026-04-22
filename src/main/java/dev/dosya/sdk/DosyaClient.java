package dev.dosya.sdk;

import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.resource.*;

public class DosyaClient {

    private final FilesResource files;
    private final FoldersResource folders;
    private final UploadResource upload;
    private final DownloadResource download;
    private final SharesResource shares;
    private final WorkspacesResource workspaces;
    private final FileRequestsResource fileRequests;
    private final SearchResource search;
    private final CommentsResource comments;
    private final MeResource me;
    private final ActivityResource activity;

    public DosyaClient(DosyaClientOptions options) {
        if (!options.getApiKey().startsWith("dos_")) {
            throw new IllegalArgumentException("API key must start with 'dos_'");
        }

        DosyaHttpClient http = new DosyaHttpClient(options);
        this.files = new FilesResource(http);
        this.folders = new FoldersResource(http);
        this.upload = new UploadResource(http);
        this.download = new DownloadResource(http);
        this.shares = new SharesResource(http);
        this.workspaces = new WorkspacesResource(http);
        this.fileRequests = new FileRequestsResource(http);
        this.search = new SearchResource(http);
        this.comments = new CommentsResource(http);
        this.me = new MeResource(http);
        this.activity = new ActivityResource(http);
    }

    public DosyaClient(String apiKey) {
        this(new DosyaClientOptions(apiKey));
    }

    public FilesResource files() { return files; }
    public FoldersResource folders() { return folders; }
    public UploadResource upload() { return upload; }
    public DownloadResource download() { return download; }
    public SharesResource shares() { return shares; }
    public WorkspacesResource workspaces() { return workspaces; }
    public FileRequestsResource fileRequests() { return fileRequests; }
    public SearchResource search() { return search; }
    public CommentsResource comments() { return comments; }
    public MeResource me() { return me; }
    public ActivityResource activity() { return activity; }
}
