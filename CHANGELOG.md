# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.1] - 2026-04-22

### Changed
- Developer contact updated to Dosya.dev Team &lt;support@dosya.dev&gt;
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
