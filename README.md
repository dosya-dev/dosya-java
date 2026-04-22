# Dosya Java SDK

Official Java SDK for [dosya.dev](https://dosya.dev) — file storage, uploads, sharing, and management.

**Java 8+** compatible. Single dependency: [Gson](https://github.com/google/gson).

## Installation

### Maven

```xml
<dependency>
    <groupId>dev.dosya</groupId>
    <artifactId>dosya-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'dev.dosya:dosya-java:0.1.0'
```

## Quick Start

```java
import dev.dosya.sdk.DosyaClient;
import dev.dosya.sdk.model.*;

DosyaClient client = new DosyaClient("dos_your_api_key");

// List workspaces
List<WorkspaceListItem> workspaces = client.workspaces().list();

// List files
ListFilesResponse files = client.files().list(new ListFilesParams("workspace_id"));

// Upload a file
UploadResult result = client.upload().file(
    UploadParams.fromBytes("workspace_id", "hello.txt", "Hello!".getBytes())
        .mimeType("text/plain")
);

// Download a file
byte[] data = client.download().downloadBytes("file_id");

// Get current user
UserProfile me = client.me().profile();
```

## Configuration

```java
import dev.dosya.sdk.DosyaClient;
import dev.dosya.sdk.DosyaClientOptions;

DosyaClient client = new DosyaClient(
    new DosyaClientOptions("dos_your_api_key")
        .baseUrl("https://dosya.dev")     // custom base URL
        .timeout(60000)                    // 60s timeout
        .maxRetries(5)                     // retry up to 5 times
        .debug(msg -> System.out.println(msg))  // debug logging
        .onRateLimit(info -> System.out.println(
            "Rate limit: " + info.getRemaining() + "/" + info.getLimit()))
);
```

## Resources

| Resource | Access | Description |
|---|---|---|
| Files | `client.files()` | List, get, delete, rename, move, copy, lock, hide, versions, share |
| Folders | `client.folders()` | Create, get, rename, delete, move, tree, lock |
| Upload | `client.upload()` | Single & multipart upload with progress, resume |
| Download | `client.download()` | Get URL, download bytes, download stream |
| Shares | `client.shares()` | List share links, revoke |
| Workspaces | `client.workspaces()` | List, get, create, update, settings, delete |
| File Requests | `client.fileRequests()` | Create, get, update, delete, uploads, recipients |
| Search | `client.search()` | Full-text search across files, folders, shares |
| Comments | `client.comments()` | List, create, edit, delete |
| Activity | `client.activity()` | Activity log with filtering |
| Me | `client.me()` | User profile, API key management |

## Files

```java
// List files in a workspace
ListFilesResponse response = client.files().list(
    new ListFilesParams("ws_id")
        .sort("newest")
        .filter("images")
        .page(1)
        .perPage(50)
);

// Get file details
FileDetail file = client.files().get("file_id");

// Rename
client.files().rename("file_id", "new-name.pdf");

// Move to folder
client.files().move("file_id", "folder_id");

// Copy
FileDetail copy = client.files().copy("file_id", "copy-name.pdf", "target_folder_id");

// Delete (soft)
boolean permanent = client.files().delete("file_id");

// Restore from trash
client.files().restore("file_id");

// Lock / Unlock
client.files().lock("file_id", "view_only");
client.files().unlock("file_id");

// Versions
FileVersionsResponse versions = client.files().listVersions("file_id");
client.files().restoreVersion("file_id", 2);

// Share
ShareLinkDetail link = client.files().createShareLink("file_id",
    new CreateShareLinkParams().expiresInDays(7).password("secret"));

client.files().shareByEmail("file_id",
    Arrays.asList("user@example.com"), "Check this out!");
```

## Upload

```java
// Upload from bytes
UploadResult result = client.upload().file(
    UploadParams.fromBytes("ws_id", "data.bin", byteArray)
        .mimeType("application/octet-stream")
        .folderId("folder_id")
        .onProgress(p -> System.out.printf(
            "%d%% (%s)%n", p.getPercent(), p.getStatus()))
);

// Upload from file
UploadResult result = client.upload().file(
    UploadParams.fromFile("ws_id", new File("/path/to/file.pdf"))
        .mimeType("application/pdf")
);

// Upload from stream
UploadResult result = client.upload().file(
    UploadParams.fromStream("ws_id", "data.csv", inputStream, fileSize)
        .mimeType("text/csv")
);

// Resume interrupted upload
UploadResult result = client.upload().resume(sessionId, remainingBytes, progress -> {});
```

Files > 10 MB are automatically split into multipart uploads with 3 concurrent parts.

## Download

```java
// Get presigned download URL
String url = client.download().getUrl("file_id");

// Download as byte array
byte[] data = client.download().downloadBytes("file_id");

// Download as stream
InputStream stream = client.download().downloadStream("file_id");

// With version
byte[] oldVersion = client.download().downloadBytes("file_id", 2, null);
```

## Search

```java
SearchResponse results = client.search().query(
    new SearchParams("ws_id", "quarterly report").page(1).perPage(20)
);

for (SearchResponse.SearchFile f : results.getFiles()) {
    System.out.println(f.getName() + " (" + f.getSizeBytes() + " bytes)");
}
```

## Error Handling

```java
import dev.dosya.sdk.exception.*;

try {
    client.files().get("nonexistent");
} catch (DosyaApiException e) {
    System.out.println("Status: " + e.getStatus());       // 404
    System.out.println("Error: " + e.getErrorMessage());   // "File not found"
} catch (DosyaNetworkException e) {
    System.out.println("Network error: " + e.getMessage());
} catch (DosyaUploadException e) {
    System.out.println("Upload failed for session: " + e.getSessionId());
}
```

## Retry & Rate Limiting

The SDK automatically retries on:
- **429** (rate limited) — respects `Retry-After` header
- **5xx** (server errors) — exponential backoff with jitter

Default: 3 retries, 500ms base delay, 30s max delay.

## License

MIT
