# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0] - Unreleased

Brings the SDK in line with the current dosya.dev API. Several 0.2.1 methods did not work against the API at all; they are fixed here, some with new signatures.

### Breaking changes

- **Java 11 is now required.** The SDK uses the JDK's `java.net.http` client: `HttpURLConnection` cannot send PATCH, which several endpoints need, and cannot time out a stalled response body. Stay on 0.2.x for Java 8.
- **Default base URL is now `https://api.dosya.dev`.** The old default (`https://dosya.dev`) serves the website, so every call failed.
- `files().unlock(id)` and `folders().unlock(id)` now take a password and return `UnlockGrant`. The old calls always failed (400). Remove a lock with `lock(id, LockMode.NONE)`.
- `files().hide(id)` (no mode, which un-hid the file) is removed; use `hide(id, SetHideParams)`, which sends `targets` instead of the ignored `target_ids`.
- `files().copy(...)` returns `CopiedFile`; `newName` was never honoured and the old `FileDetail` result was always empty.
- `files().getShareLinks/createShareLink/createShareBundle` return `ItemShareLink`, `CreatedShareLink` and `CreatedShareBundle`; `ShareLinkDetail` and `ShareBundleLink` are deprecated.
- `folders().rename()` called the restore endpoint; it now calls `/rename`. `folders().delete()` returns a trash-or-purge `DeleteFolderResult`.
- `download().getUrl(fileId)` returns `DownloadLink` (url, size, name, region, expiry) from `/download-url` instead of scraping a redirect. Storage errors are `DosyaApiException` with their real status.
- `workspaces().list()` returns `WorkspaceListResponse`; `create()` returns `CreatedWorkspace`; `updateSettings()` takes `WorkspaceSettingsUpdate`; `delete(id)` is replaced by `delete(id, code, confirmName)`; `UpdateWorkspaceParams.defaultRegion()` is removed (the region is fixed at creation).
- `fileRequests().get()` reads `/:id/uploads` and returns `FileRequestWithActivity`; `update()` uses PATCH; `resend(id, recipientId)` takes one recipient; `listUploads()`/`listRecipients()` return the real shapes.
- `shares().list()` rows are `WorkspaceShareLink` (`linkId`, `sharedAt`).
- Search and activity pagination use `SearchPagination` / `ActivityPagination`; activity entries expose `entityType`/`entityId`; `comments().list(ws)` without a target is removed.
- Removed `me().listApiKeys()`, `createApiKey()`, `deleteApiKey()` and their models. Those endpoints require an interactive login and always returned 403 to an API key.
- Retries: POST and PATCH are no longer retried on 5xx, timeouts or network errors (only on a 429 with `Retry-After`); a 429 without `Retry-After` is not retried; trash, purge, restore and move are never retried.
- `UploadParams.region` is deprecated and no longer sent. `upload().resume()` checks that the source size matches the session.

### Fixed

- Multipart uploads could corrupt their session when the first parts raced; the first part is now sent alone.
- `UploadParams.fromFile` and `fromStream` no longer read the whole file into memory; parts are read on demand.
- A failed single-request upload is retried on a fresh session, and only after the server confirms the first attempt did not store the file. `complete` is not repeated while an earlier attempt may still be finishing.
- A JSON response whose body stalls after the headers now times out (`DosyaTimeoutException`).
- `files().list()` trash and hidden filters were ignored.
- Workspace settings with digits in their names (`require_2fa`) were sent and read under the wrong key.
- Flags the API returns as `0/1` deserialize into `boolean` fields.
- Every id is encoded as a single path segment and `.`/`..` are rejected, so an id cannot select a different endpoint.

### Added

- `webhooks()` (endpoint management, test, deliveries, redeliver) and `dev.dosya.sdk.webhook.WebhookSignature` for verifying deliveries.
- `team()`, `roles()`, `regions()`, `favourites()`, `remoteDownloads()` (with `waitFor`).
- Files: `getLock`, `getHide`, `batchDelete`, `duplicates`; list gains `dir`, `unlockToken`, `groupId`, `folder` and column sorts; `move` can rename in the same call; share links accept `expiresAt`, `accessMode`, `recipientEmails`, `maxDownloads`.
- Folders: `restore`, `purge`, `children`, `search`, `createBatch`, `getLock`, `getHide`, `hide`, and folder share links.
- Upload: `many`, `batch`, `uploadPart`, `complete`; `UploadSource` (bytes, paths, streams); `sha256`/`computeSha256`, `sourceModifiedAt`/`sourceCreatedAt`, `expectedVersion`, `concurrency`.
- Download: `downloadTo`, `raw`, `thumbnail`, `archive`, `archiveEntries`, `archiveEntry`, `ttl` and byte ranges.
- Workspaces: `getSettings`, `uploadLimits`, `deletePreview`, `requestDeletion`, `transfer`, `leave`. Shares: `update`, `analytics`, `withMe`. File requests: `list`, `addRecipient`, `removeRecipient`. Me: `permissions`, `updateName`, `revokeCurrentKey`.
- `DosyaApiException` exposes `getCode()`, `getDetails()`, `getRetryAfterSeconds()`, `getMethod()`, `getPath()`. New `DosyaTimeoutException` and `DosyaWebhookSignatureException`.
- Options `uploadTimeout`, `readYourWrites` (sends `X-D1-Bookmark` so a read right after a write is consistent) and `httpClient`.
- `LockMode`, `HiddenMode`, `ShareAccessMode` enums.

## [0.2.1] - 2026-04-22

### Changed
- Developer contact updated to Dosya.dev Team <support@dosya.dev>
- Added Support section to README with email and GitHub Issues links

## [0.2.0] - 2026-04-22

### Added
- SLF4J logging support (optional dependency)
- `@Nullable` / `@NotNull` annotations on all public APIs (JetBrains annotations, provided scope)
- `DosyaInterceptor` interface for request/response observability hooks
- Separate `connectTimeout` and `readTimeout` configuration in `DosyaClientOptions`
- Request ID propagation on `DosyaApiException` via `getRequestId()`
- `Automatic-Module-Name: dev.dosya.sdk` in JAR manifest for JPMS compatibility
- Binary compatibility checking via japicmp (configured, skipped until next release)
- Reproducible build configuration
- Comprehensive Javadoc on all public classes and methods
- Test suite with JUnit 5 and MockWebServer

### Changed
- HTTPS is now enforced by default — `baseUrl()` rejects non-HTTPS URLs unless explicitly opted out
- `UploadStatusResponse.hasMultipart()` replaces `isHasMultipart()` (naming fix)
- Retry-After header now supports both delay-seconds and HTTP-date formats

### Fixed
- `CreateFileRequestParams.emails()` now stores a defensive copy of the list

## [0.1.0] - 2025-04-01

### Added
- Initial release of Dosya Java SDK
- File operations: list, get, delete, restore, rename, move, copy, lock/unlock, hide, versions
- Folder operations: create, get, rename, delete, move, tree, lock/unlock
- Upload: single-part and resumable multipart with progress tracking
- Download: URL, bytes, and streaming with version support
- Share links and bundles with password and expiry options
- Workspace management with settings
- File requests with recipient management
- Search across files, folders, shares, and file requests
- Comments with threading support
- Activity log with filtering
- User profile and API key management
- Exponential backoff retry with jitter
- Rate limit handling with Retry-After support
- Configurable timeouts, retries, and debug logging

[0.2.1]: https://github.com/dosya-dev/dosya-java/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/dosya-dev/dosya-java/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/dosya-dev/dosya-java/releases/tag/v0.1.0
