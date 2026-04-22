# Dosya Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/dev.dosya/dosya-java)](https://central.sonatype.com/artifact/dev.dosya/dosya-java)
[![Javadoc](https://javadoc.io/badge2/dev.dosya/dosya-java/javadoc.svg)](https://javadoc.io/doc/dev.dosya/dosya-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Official Java SDK for [dosya.dev](https://dosya.dev) — file storage, uploads, sharing, and management.

- **Java 8+** compatible
- Single runtime dependency ([Gson](https://github.com/google/gson))
- Optional [SLF4J](https://www.slf4j.org/) integration for structured logging
- Thread-safe after construction
- Nullability annotations via [JetBrains Annotations](https://github.com/JetBrains/java-annotations)

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Resources](#resources)
  - [Files](#files)
  - [Folders](#folders)
  - [Upload](#upload)
  - [Download](#download)
  - [Workspaces](#workspaces)
  - [Shares](#shares)
  - [Search](#search)
  - [File Requests](#file-requests)
  - [Comments](#comments)
  - [Activity](#activity)
  - [Me / API Keys](#me--api-keys)
- [Error Handling](#error-handling)
- [Retry & Rate Limiting](#retry--rate-limiting)
- [Logging](#logging)
- [Observability](#observability)
- [Thread Safety](#thread-safety)
- [Changelog](#changelog)
- [License](#license)

## Installation

### Maven

```xml
<dependency>
    <groupId>dev.dosya</groupId>
    <artifactId>dosya-java</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.dosya:dosya-java:0.2.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'dev.dosya:dosya-java:0.2.0'
```

## Quick Start

```java
import dev.dosya.sdk.DosyaClient;
import dev.dosya.sdk.model.*;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        DosyaClient client = new DosyaClient("dos_your_api_key");

        // List workspaces
        List<WorkspaceListItem> workspaces = client.workspaces().list();

        // List files in a workspace
        ListFilesResponse files = client.files().list(
            new ListFilesParams(workspaces.get(0).getId()));

        // Upload a file
        UploadResult result = client.upload().file(
            UploadParams.fromBytes("workspace_id", "hello.txt", "Hello!".getBytes())
                .mimeType("text/plain")
        );

        // Download a file
        byte[] data = client.download().downloadBytes(result.getFile().getId());

        // Get current user
        UserProfile me = client.me().profile();
        System.out.println("Logged in as: " + me.getName());
    }
}
```

## Configuration

```java
DosyaClient client = new DosyaClient(
    new DosyaClientOptions("dos_your_api_key")
        .baseUrl("https://dosya.dev")       // custom base URL (HTTPS required)
        .connectTimeout(5000)               // TCP connect timeout (default 10s)
        .readTimeout(60000)                 // response read timeout (default 30s)
        .maxRetries(5)                      // retry up to 5 times (default 3)
        .baseDelay(1000)                    // initial retry delay (default 500ms)
        .maxDelay(60000)                    // max retry delay (default 30s)
        .debug(msg -> System.out.println(msg))
        .onRateLimit(info -> System.out.printf(
            "Rate limit: %d/%d remaining, resets at %d%n",
            info.getRemaining(), info.getLimit(), info.getResetAt()))
        .interceptor(new MyInterceptor())   // observability hook
);
```

You can also set both timeouts at once with `timeout(ms)`:

```java
new DosyaClientOptions("dos_your_api_key")
    .timeout(60000)  // sets both connectTimeout and readTimeout to 60s
```

> **Note:** `baseUrl()` enforces HTTPS. Passing an HTTP URL will throw `IllegalArgumentException`.

## Resources

| Resource | Accessor | Description |
|---|---|---|
| Files | `client.files()` | List, get, delete, rename, move, copy, lock, hide, versions, share |
| Folders | `client.folders()` | Create, get, rename, delete, move, tree, lock |
| Upload | `client.upload()` | Single & multipart upload with progress tracking and resume |
| Download | `client.download()` | Get URL, download as bytes, download as stream |
| Workspaces | `client.workspaces()` | List, get, create, update, settings, delete |
| Shares | `client.shares()` | List share links, revoke |
| Search | `client.search()` | Full-text search across files, folders, shares, file requests |
| File Requests | `client.fileRequests()` | Create, get, update, delete, uploads, recipients |
| Comments | `client.comments()` | List, create, edit, delete |
| Activity | `client.activity()` | Activity log with filtering |
| Me | `client.me()` | User profile, API key management |

---

### Files

```java
// List files in a workspace
ListFilesResponse response = client.files().list(
    new ListFilesParams("workspace_id")
        .folderId("folder_id")    // optional: filter by folder
        .sort("newest")
        .filter("images")
        .page(1)
        .perPage(50)
);

for (FileListItem file : response.getFiles()) {
    System.out.println(file.getName() + " — " + file.getSizeBytes() + " bytes");
}

// Get file details
FileDetail file = client.files().get("file_id");

// Rename
client.files().rename("file_id", "new-name.pdf");

// Move to folder
client.files().move("file_id", "target_folder_id");

// Copy (with optional new name and target folder)
FileDetail copy = client.files().copy("file_id", "copy-name.pdf", "target_folder_id");
FileDetail copy2 = client.files().copy("file_id"); // same name, same folder

// Delete and restore
boolean permanent = client.files().delete("file_id");
client.files().restore("file_id");

// Lock / Unlock
client.files().lock("file_id", "view_only");
client.files().lock("file_id", "password", "my_secret");
client.files().unlock("file_id");

// Versions
FileVersionsResponse versions = client.files().listVersions("file_id");
client.files().restoreVersion("file_id", 2);

// Share links
ShareLinkDetail link = client.files().createShareLink("file_id",
    new CreateShareLinkParams().expiresInDays(7).password("secret"));
List<ShareLinkDetail> links = client.files().getShareLinks("file_id");

// Share by email
client.files().shareByEmail("file_id",
    Arrays.asList("user@example.com"), "Check this out!");

// Share bundle (multiple files)
ShareBundleLink bundle = client.files().createShareBundle("file_id",
    new CreateShareBundleParams(Arrays.asList("file_1", "file_2"))
        .expiresInDays(30));
```

### Folders

```java
// Create a folder
CreateFolderResponse response = client.folders().create("workspace_id", "Reports");

// Create nested folder
CreateFolderResponse nested = client.folders().create("workspace_id", "Q4", "parent_folder_id");

// Get folder details
FolderDetail folder = client.folders().get("folder_id");

// Rename
client.folders().rename("folder_id", "Archived Reports");

// Move to another folder
client.folders().move("folder_id", "new_parent_folder_id");

// Get folder tree for a workspace
List<FolderTreeItem> tree = client.folders().tree("workspace_id");

// Lock / Unlock
client.folders().lock("folder_id", "view_only");
client.folders().lock("folder_id", "password", "my_secret");
client.folders().unlock("folder_id");

// Delete
client.folders().delete("folder_id");
```

### Upload

```java
// Upload from byte array
UploadResult result = client.upload().file(
    UploadParams.fromBytes("workspace_id", "data.bin", byteArray)
        .mimeType("application/octet-stream")
        .folderId("folder_id")
        .onProgress(p -> System.out.printf(
            "%d%% (%s)%n", p.getPercent(), p.getStatus()))
);
System.out.println("Uploaded: " + result.getFile().getId());

// Upload from file
UploadResult result = client.upload().file(
    UploadParams.fromFile("workspace_id", new File("/path/to/report.pdf"))
        .mimeType("application/pdf")
);

// Upload from stream
UploadResult result = client.upload().file(
    UploadParams.fromStream("workspace_id", "data.csv", inputStream, fileSize)
        .mimeType("text/csv")
);

// Resume an interrupted multipart upload
UploadResult result = client.upload().resume(
    sessionId, fullFileBytes, progress -> System.out.println(progress.getPercent() + "%"));

// Check upload status
UploadStatusResponse status = client.upload().status(sessionId);
System.out.println("Status: " + status.getStatus());
```

Files larger than 10 MB are automatically split into multipart uploads with 3 concurrent parts.

### Download

```java
// Get a presigned download URL
String url = client.download().getUrl("file_id");

// Download as byte array
byte[] data = client.download().downloadBytes("file_id");

// Download as stream (caller must close)
try (InputStream stream = client.download().downloadStream("file_id")) {
    // read from stream...
}

// Download a specific version
byte[] v2 = client.download().downloadBytes("file_id", 2, null);

// Download with unlock token (for locked files)
byte[] locked = client.download().downloadBytes("file_id", null, "unlock_token");
```

### Workspaces

```java
// List all workspaces
List<WorkspaceListItem> workspaces = client.workspaces().list();

// Get workspace details with settings
WorkspaceGetResponse ws = client.workspaces().get("workspace_id");
System.out.println("Storage used: " + ws.getWorkspace().getStorageUsedBytes());
System.out.println("Max file size: " + ws.getSettings().getMaxFileSizeGb() + " GB");

// Create a workspace
WorkspaceDetail created = client.workspaces().create(
    new CreateWorkspaceParams("My Workspace")
        .iconInitials("MW")
        .iconColor("#3B82F6")
        .defaultRegion("eu-west-1")
);

// Update workspace
client.workspaces().update("workspace_id",
    new UpdateWorkspaceParams().name("Renamed Workspace").iconColor("#EF4444"));

// Update workspace settings
client.workspaces().updateSettings("workspace_id", settings);

// Delete workspace
client.workspaces().delete("workspace_id");
```

### Shares

```java
// List all share links in a workspace
SharesListResponse shares = client.shares().list("workspace_id");
for (ShareLinkDetail link : shares.getLinks()) {
    System.out.println(link.getFileName() + " — " + link.getStatus());
}
System.out.println("Active: " + shares.getStats().getActive());

// Revoke a share link
client.shares().revoke("link_id");
```

### Search

```java
SearchResponse results = client.search().query(
    new SearchParams("workspace_id", "quarterly report")
        .page(1)
        .perPage(20)
);

for (SearchResponse.SearchFile f : results.getFiles()) {
    System.out.println(f.getName() + " (" + f.getSizeBytes() + " bytes)");
}
for (SearchResponse.SearchFolder f : results.getFolders()) {
    System.out.println("Folder: " + f.getName());
}
```

### File Requests

```java
// Create a file request
FileRequestCreateResponse created = client.fileRequests().create(
    new CreateFileRequestParams("workspace_id")
        .title("Upload your documents")
        .message("Please submit by Friday")
        .expiresInDays(7)
        .maxFiles(5)
        .emails(Arrays.asList("user@example.com"))
);
System.out.println("Share this URL: " + created.getRequest().getUrl());

// Get file request details
FileRequestDetail detail = client.fileRequests().get("request_id");

// Update title/message
client.fileRequests().update("request_id", "New Title", "New message");

// List uploads and recipients
List<FileRequestsResource.FileRequestUpload> uploads =
    client.fileRequests().listUploads("request_id");
List<FileRequestsResource.FileRequestRecipient> recipients =
    client.fileRequests().listRecipients("request_id");

// Resend to all recipients
client.fileRequests().resend("request_id");

// Resend to specific recipients
client.fileRequests().resend("request_id", Arrays.asList("recipient_id_1"));

// Delete
client.fileRequests().delete("request_id");
```

### Comments

```java
// List comments in a workspace
List<CommentDetail> comments = client.comments().list("workspace_id");

// List comments on a specific file
List<CommentDetail> fileComments =
    client.comments().list("workspace_id", "file_id", null);

// Create a comment on a file
CommentDetail comment = client.comments().create(
    new CreateCommentParams("workspace_id", "Looks good!")
        .fileId("file_id")
);

// Reply to a comment
CommentDetail reply = client.comments().create(
    new CreateCommentParams("workspace_id", "Thanks!")
        .fileId("file_id")
        .parentId(comment.getId())
);

// Edit a comment
client.comments().edit("comment_id", "Updated text");

// Delete a comment
client.comments().delete("comment_id");
```

### Activity

```java
// List recent activity in a workspace
ActivityListResponse activity = client.activity().list(
    new ListActivityParams("workspace_id")
        .page(1)
        .perPage(50)
);

for (ActivityEntry entry : activity.getActivities()) {
    System.out.printf("[%s] %s %s %s%n",
        entry.getUserName(), entry.getAction(),
        entry.getResourceType(), entry.getResourceName());
}

// Filter by category and action
ActivityListResponse uploads = client.activity().list(
    new ListActivityParams("workspace_id")
        .category("file")
        .action("upload")
);

// Filter by user
ActivityListResponse userActivity = client.activity().list(
    new ListActivityParams("workspace_id")
        .userId("user_id")
);
```

### Me / API Keys

```java
// Get current user profile
UserProfile me = client.me().profile();
System.out.println(me.getName() + " (" + me.getEmail() + ")");

// List API keys
List<ApiKeyItem> keys = client.me().listApiKeys();
for (ApiKeyItem key : keys) {
    System.out.println(key.getName() + " — " + key.getKeyPrefix() + "...");
}

// Create a new API key
CreatedApiKey newKey = client.me().createApiKey(
    new CreateApiKeyParams("CI/CD Key")
        .scope("read")
        .expiresInDays(90)
);
System.out.println("Save this key: " + newKey.getPlainKey());

// Delete an API key
client.me().deleteApiKey("key_id");
```

## Error Handling

All SDK exceptions extend `DosyaException` (unchecked). Catch specific subtypes for granular handling:

```java
import dev.dosya.sdk.exception.*;

try {
    client.files().get("nonexistent");
} catch (DosyaApiException e) {
    // API returned an error response
    System.out.println("Status: " + e.getStatus());           // 404
    System.out.println("Error: " + e.getErrorMessage());       // "File not found"
    System.out.println("Request ID: " + e.getRequestId());     // server correlation ID
    System.out.println("Raw body: " + e.getRaw());             // full JSON response
} catch (DosyaNetworkException e) {
    // Connection failed, DNS error, timeout, etc.
    System.out.println("Network error: " + e.getMessage());
} catch (DosyaUploadException e) {
    // Upload-specific failure
    System.out.println("Session: " + e.getSessionId());
    System.out.println("Failed part: " + e.getPartNumber());   // null for single-part uploads
}
```

The request ID (`e.getRequestId()`) is extracted from the `X-Request-Id` response header and is useful for support requests and log correlation.

## Retry & Rate Limiting

The SDK automatically retries on:

- **429 Too Many Requests** — respects the `Retry-After` header (both delay-seconds and HTTP-date formats)
- **5xx Server Errors** — exponential backoff with 20% jitter
- **Network errors** (connection refused, timeout) — same backoff strategy

| Setting | Default | Method |
|---|---|---|
| Max retries | 3 | `maxRetries(int)` |
| Base delay | 500 ms | `baseDelay(long)` |
| Max delay | 30,000 ms | `maxDelay(long)` |

Backoff formula: `min(baseDelay * 2^attempt + jitter, maxDelay)`

## Logging

The SDK supports two logging mechanisms:

### SLF4J (recommended)

Add any SLF4J binding to your classpath (Logback, Log4j2, etc.) and the SDK will log automatically:

- **DEBUG** — every request/response with URL, status code, and duration
- **WARN** — retries (rate limits, server errors, network errors)

```xml
<!-- Example: add Logback -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.14</version>
</dependency>
```

SLF4J is an optional dependency — if no binding is present, logging is silently disabled.

### Debug callback

For quick debugging without a logging framework:

```java
new DosyaClientOptions("dos_your_api_key")
    .debug(msg -> System.out.println("[dosya] " + msg));
```

The callback receives human-readable messages like:
```
GET https://dosya.dev/api/files (attempt 1/4)
GET https://dosya.dev/api/files -> 200
Rate limited, retrying in 1000ms
```

## Observability

Register a `DosyaInterceptor` to hook into every HTTP request and response — useful for OpenTelemetry, Micrometer, or custom metrics:

```java
import dev.dosya.sdk.DosyaInterceptor;

DosyaClient client = new DosyaClient(
    new DosyaClientOptions("dos_your_api_key")
        .interceptor(new DosyaInterceptor() {
            @Override
            public void beforeRequest(String method, String url) {
                // start a span, increment a counter, etc.
            }

            @Override
            public void afterResponse(String method, String url,
                                      int statusCode, String requestId,
                                      long durationMs) {
                System.out.printf("%s %s -> %d (%dms) [%s]%n",
                    method, url, statusCode, durationMs, requestId);
            }
        })
);
```

The `requestId` parameter is the value of the `X-Request-Id` response header (may be `null`).

## Thread Safety

`DosyaClient` and all resource instances are **thread-safe after construction**. You can safely share a single client across multiple threads:

```java
// Create once, use everywhere
DosyaClient client = new DosyaClient("dos_your_api_key");

// Safe to call from any thread
executor.submit(() -> client.files().list(new ListFilesParams("ws_id")));
executor.submit(() -> client.workspaces().list());
```

Do not mutate `DosyaClientOptions` after passing it to the client constructor.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a complete list of changes in each release.

## Support

If you have questions, found a bug, or need help integrating the SDK, reach out to the Dosya.dev team:

- **Email:** [support@dosya.dev](mailto:support@dosya.dev)
- **GitHub Issues:** [dosya-dev/dosya-java/issues](https://github.com/dosya-dev/dosya-java/issues)

## License

[MIT](https://opensource.org/licenses/MIT)
