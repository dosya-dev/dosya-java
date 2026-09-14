# Dosya Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/dev.dosya/dosya-java)](https://central.sonatype.com/artifact/dev.dosya/dosya-java)
[![Javadoc](https://javadoc.io/badge2/dev.dosya/dosya-java/javadoc.svg)](https://javadoc.io/doc/dev.dosya/dosya-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Official Java SDK for [dosya.dev](https://dosya.dev) - file storage, resumable uploads, sharing, webhooks and workspace management.

- **Java 11+** (uses the JDK's `java.net.http` client; stay on 0.2.x for Java 8)
- Single runtime dependency ([Gson](https://github.com/google/gson))
- Optional [SLF4J](https://www.slf4j.org/) integration for structured logging
- Thread-safe after construction
- Nullability annotations via [JetBrains Annotations](https://github.com/JetBrains/java-annotations)

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Authentication](#authentication)
- [Configuration](#configuration)
- [Retries and timeouts](#retries-and-timeouts)
- [Uploads](#uploads)
- [Downloads](#downloads)
- [Sharing](#sharing)
- [Webhooks](#webhooks)
- [Error Handling](#error-handling)
- [Things worth knowing](#things-worth-knowing)
- [Resources](#resources)
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
    <version>0.3.0</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.dosya:dosya-java:0.3.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'dev.dosya:dosya-java:0.3.0'
```

## Quick Start

```java
DosyaClient client = new DosyaClient(System.getenv("DOSYA_API_KEY"));

String workspaceId = client.workspaces().list().getWorkspaces().get(0).getId();

// Upload - small files go up in one request, large files resume part by part
UploadResult uploaded = client.upload().file(
        UploadParams.fromPath(workspaceId, Paths.get("report.pdf"))
                .onProgress(p -> System.out.println(p.getPercent() + "%")));

// List a folder
ListFilesResponse listing = client.files().list(new ListFilesParams(workspaceId).sort("modified_desc"));

// A short-lived download URL
DownloadLink link = client.download().getUrl(uploaded.getFile().getId(), new DownloadOptions().ttl(600));

// Share it
CreatedShareLink share = client.files().createShareLink(uploaded.getFile().getId(),
        new CreateShareLinkParams().expiresInDays(7));
System.out.println(share.getUrl());
```

## Authentication

Every request carries an API key (`dos_...`). Create one in the web app under **Settings > API keys**.

| Scope | Can call |
|---|---|
| `read` | GET endpoints, downloads, archive downloads |
| `upload` | Upload endpoints and `folders().create` only |
| `full` | Everything an API key can reach |

A key can also be pinned to one workspace. Pinned keys are refused (403) on routes whose workspace cannot be checked up front, such as webhook management, comment writes, share bundles, remote downloads, and team or share-link changes by id.

Creating or listing API keys, 2FA, passwords and sessions require an interactive login, so they are not part of this SDK. `client.me().revokeCurrentKey()` is the one key operation a key can perform on itself.

## Configuration

```java
DosyaClient client = new DosyaClient(new DosyaClientOptions("dos_...")
        .baseUrl("https://api.dosya.dev")   // default; HTTPS required (http://localhost allowed for tests)
        .connectTimeout(10_000)
        .readTimeout(30_000)                // per attempt, JSON requests, including the body
        .uploadTimeout(600_000)             // per attempt, requests that carry file bytes
        .maxRetries(3)
        .baseDelay(500)
        .maxDelay(30_000)
        .readYourWrites(true)               // echo X-D1-Bookmark so reads see your writes
        .onRateLimit(info -> System.out.println(info.getRemaining() + "/" + info.getLimit()))
        .debug(System.out::println));
```

Pass your own `java.net.http.HttpClient` with `.httpClient(...)` for proxies or custom TLS.

## Retries and timeouts

- GET, HEAD, PUT and DELETE are retried on 5xx, timeouts and network errors, with exponential backoff and jitter.
- POST and PATCH are only retried on a rate-limit 429 carrying `Retry-After`, which the API sends before any work is done. A failed POST is never replayed, so it cannot create a duplicate share link, webhook or download job.
- A 429 without `Retry-After` is a business limit (for example "Daily limit of 20 remote downloads") and is thrown immediately.
- When `Retry-After` asks for longer than `maxDelay`, the exception is thrown with `getRetryAfterSeconds()` set instead of sleeping.
- Operations whose repeat would change their meaning (trash then purge, restore, move) are never retried automatically.
- Uploads run their own retries: before repeating an upload whose outcome was not seen, the SDK asks the server how the first attempt ended, so a file is never stored twice.

## Uploads

```java
client.upload().file(UploadParams.fromPath(workspaceId, Paths.get("clip.mp4"))
        .folderId(folderId)
        .sourceModifiedAt(Files.getLastModifiedTime(Paths.get("clip.mp4")).toInstant())
        .computeSha256(true)      // verified by the server on single-request uploads
        .expectedVersion(3)       // 409 version_conflict if someone else wrote first
        .concurrency(4));
```

- Files up to 50 MiB go up in one request; larger files use resumable 10 MiB parts, read from disk one part at a time.
- Uploading a name that already exists in the folder creates a new version of that file.
- The stored MIME type is derived from the file extension.

Resume after a crash with the session id from `DosyaUploadException.getSessionId()`:

```java
client.upload().resume(sessionId, UploadSource.ofPath(Paths.get("clip.mp4")));
```

Many small files in few requests:

```java
List<UploadBatchFile> files = ...; // UploadBatchFile.of(path).folderId(folderId)
for (UploadManyResult r : client.upload().many(workspaceId, files,
        new UploadManyOptions().onProgress(p -> System.out.println(p.getFilesCompleted() + "/" + p.getTotalFiles())))) {
    if (!r.isOk()) System.err.println(r.getName() + ": " + r.getError());
}
```

`many()` packs files up to 5 MiB into `batch()` requests (up to 200 files each) and sends larger files through `file()`, reporting per-file failures in the results instead of throwing. `batch()`, `init()`, `uploadPart()`, `complete()` and `status()` are available for custom pipelines.

## Downloads

```java
DownloadLink link = client.download().getUrl(fileId, new DownloadOptions().ttl(3600));
byte[] bytes = client.download().downloadBytes(fileId);
long written = client.download().downloadTo(fileId, Paths.get("out.bin"));
try (InputStream part = client.download().downloadStream(fileId, new DownloadOptions().range(0, 1023))) {
    // ...
}
try (InputStream zip = client.download().archive(null, Arrays.asList(folderId)).body()) {
    // streamed zip of a whole folder
}
```

Password-locked (`FULL_LOCK`) items need an unlock token first:

```java
UnlockGrant grant = client.files().unlock(fileId, "secret");
client.download().downloadBytes(fileId, new DownloadOptions().unlockToken(grant.getUnlockToken()));
```

## Sharing

```java
CreatedShareLink link = client.files().createShareLink(fileId, new CreateShareLinkParams()
        .expiresInDays(14)
        .password("at-least-8-chars")
        .maxDownloads(50));

// Private link: only these recipients can open it, after an emailed code
client.folders().createShareLink(folderId, new CreateShareLinkParams()
        .accessMode(ShareAccessMode.RESTRICTED)
        .recipientEmails(Arrays.asList("client@example.com")));

ShareByEmailResult sent = client.files().shareByEmail(fileId,
        new ShareByEmailParams(Arrays.asList("a@example.com")));

client.shares().update(link.getId(), new UpdateShareLinkParams().maxDownloads(100));
client.shares().revoke(link.getId());
```

## Webhooks

```java
CreatedWebhookEndpoint endpoint = client.webhooks().create(new CreateWebhookParams(workspaceId,
        "https://example.com/hooks/dosya", WebhookEventType.FILE_UPLOADED, WebhookEventType.FILE_DELETED));
String secret = endpoint.getSecret(); // shown only here and by rollSecret()
```

Verify deliveries on your server with the raw request body. `WebhookSignature` needs no client:

```java
WebhookEvent event = WebhookSignature.constructEvent(rawBody, request.getHeader("X-Dosya-Signature"), secret);
if (event.getEventType() == WebhookEventType.FILE_UPLOADED) {
    System.out.println(event.getFileId());
}
```

Payload keys in `event.getData()` are the documented snake_case names. `X-Dosya-Event-Id` is stable across retries; use it to deduplicate. `share.accessed` payloads include the share token, so treat webhook bodies as secrets.

## Error Handling

All SDK exceptions extend `DosyaException` (unchecked).

```java
try {
    client.files().list(new ListFilesParams(workspaceId).folderId(folderId));
} catch (DosyaApiException e) {
    e.getStatus();             // 403
    e.getErrorMessage();       // the API's message
    e.getCode();               // machine code when present: "folder_locked", "version_conflict", "quota", ...
    e.getDetails();            // extra fields as a JsonObject, e.g. folder_id, lock_mode
    e.getRetryAfterSeconds();  // when the API sent Retry-After
    e.getRequestId();          // X-Request-Id, for support
} catch (DosyaTimeoutException e) {
    e.getTimeoutMs();
} catch (DosyaNetworkException e) {
    e.getCause();
} catch (DosyaUploadException e) {
    e.getSessionId();          // pass to upload().resume()
    e.getPartNumber();
}
```

## Things worth knowing

- **Deleting is two-stage.** `files().delete()` and `folders().delete()` move an item to the trash; calling delete again on a trashed item removes it permanently. Use `folders().purge()` to empty a trashed folder completely.
- **Unlock does not remove a lock.** `unlock(id, password)` grants one hour of access to a `FULL_LOCK` item. Remove a lock with `lock(id, LockMode.NONE)`.
- **Deleting a workspace needs a person.** Call `workspaces().requestDeletion(id)`, read the 6-digit code from the owner's email, then `workspaces().delete(id, code, confirmName)`.
- **A workspace's region is fixed at creation.** List valid codes with `client.regions().list()`.
- **Addresses at dosya.dev** are refused as share and file-request recipients (400) unless the sender is also a dosya.dev account.

## Resources

| Accessor | Methods |
|---|---|
| `files()` | `list`, `get`, `delete`, `restore`, `rename`, `move`, `copy`, `getLock`, `lock`, `unlock`, `getHide`, `hide`, `listVersions`, `restoreVersion`, `getShareLinks`, `createShareLink`, `shareByEmail`, `createShareBundle`, `batchDelete`, `duplicates` |
| `folders()` | `create`, `createBatch`, `get`, `rename`, `restore`, `delete`, `purge`, `move`, `tree`, `children`, `search`, `getLock`, `lock`, `unlock`, `getHide`, `hide`, `getShareLinks`, `createShareLink`, `shareByEmail` |
| `upload()` | `file`, `resume`, `many`, `batch`, `init`, `uploadPart`, `complete`, `status` |
| `download()` | `getUrl`, `downloadBytes`, `downloadStream`, `downloadTo`, `raw`, `thumbnail`, `archive`, `archiveEntries`, `archiveEntry` |
| `favourites()` | `list`, `add`, `remove` |
| `shares()` | `list`, `update`, `analytics`, `revoke`, `withMe` |
| `workspaces()` | `list`, `get`, `create`, `update`, `getSettings`, `updateSettings`, `uploadLimits`, `deletePreview`, `requestDeletion`, `delete`, `transfer`, `leave` |
| `team()` | `list`, `invite`, `resendInvite`, `revokeInvite`, `updateMember`, `removeMember`, `listInviteLinks`, `createInviteLink`, `revokeInviteLink` |
| `roles()` | `list`, `create`, `update`, `delete` |
| `regions()` | `list` |
| `fileRequests()` | `list`, `create`, `get`, `update`, `delete`, `listUploads`, `listRecipients`, `addRecipient`, `removeRecipient`, `resend` |
| `search()` | `query` (supports an `ext:pdf` token) |
| `comments()` | `list`, `listForFile`, `listForFolder`, `create`, `edit`, `delete` |
| `activity()` | `list` |
| `webhooks()` | `list`, `create`, `get`, `update`, `delete`, `rollSecret`, `test`, `listDeliveries`, `redeliver` |
| `remoteDownloads()` | `list`, `create`, `cancel`, `waitFor` |
| `me()` | `profile`, `permissions`, `updateName`, `revokeCurrentKey` |

Every method's Javadoc lists its key scope, permission and limits.

Upgrading from 0.2.x? See [CHANGELOG.md](CHANGELOG.md) for the breaking changes.

## Logging

The SDK supports two logging mechanisms:

### SLF4J (recommended)

Add any SLF4J binding to your classpath (Logback, Log4j2, etc.) and the SDK will log automatically:

- **DEBUG** - every request/response with URL, status code, and duration
- **WARN** - retries (rate limits, server errors, network errors)

```xml
<!-- Example: add Logback -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.14</version>
</dependency>
```

SLF4J is an optional dependency - if no binding is present, logging is silently disabled.

### Debug callback

For quick debugging without a logging framework:

```java
new DosyaClientOptions("dos_your_api_key")
    .debug(msg -> System.out.println("[dosya] " + msg));
```

The callback receives human-readable messages like:
```
GET https://api.dosya.dev/api/files?workspace_id=ws_1 (attempt 1/4)
GET https://api.dosya.dev/api/files?workspace_id=ws_1 -> 200
HTTP 429, retrying in 1000ms
```

## Observability

Register a `DosyaInterceptor` to hook into every HTTP request and response - useful for OpenTelemetry, Micrometer, or custom metrics:

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

[MIT](LICENSE) - the SDK is free to embed in your own applications. It talks to the
official [dosya.dev](https://dosya.dev) service.

## Security

Found a vulnerability? Please report it privately via
[GitHub private vulnerability reporting](../../security/advisories/new) rather than a
public issue.

## The dosya.dev client family

| Repository | What it is | License |
|---|---|---|
| [desktop](https://github.com/dosya-dev/desktop) | Desktop client - sync, upload, manage | Source-available |
| [cli](https://github.com/dosya-dev/cli) | Command-line interface | Source-available |
| [app.dosya.dev](https://github.com/dosya-dev/app.dosya.dev) | Web application | Source-available |
| [shared](https://github.com/dosya-dev/shared) | Shared TypeScript types & utilities | Source-available |
| [dosya-js](https://github.com/dosya-dev/dosya-js) | Official JavaScript SDK | MIT |
| [dosya-java](https://github.com/dosya-dev/dosya-java) | Official Java SDK | MIT |
