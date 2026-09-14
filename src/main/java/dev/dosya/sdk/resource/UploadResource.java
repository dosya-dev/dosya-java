package dev.dosya.sdk.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.exception.DosyaNetworkException;
import dev.dosya.sdk.exception.DosyaUploadException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.UploadBatchFile;
import dev.dosya.sdk.model.UploadBatchResult;
import dev.dosya.sdk.model.UploadInitParams;
import dev.dosya.sdk.model.UploadInitResponse;
import dev.dosya.sdk.model.UploadManyOptions;
import dev.dosya.sdk.model.UploadManyProgress;
import dev.dosya.sdk.model.UploadManyResult;
import dev.dosya.sdk.model.UploadParams;
import dev.dosya.sdk.model.UploadPartResult;
import dev.dosya.sdk.model.UploadProgress;
import dev.dosya.sdk.model.UploadResult;
import dev.dosya.sdk.model.UploadResumeOptions;
import dev.dosya.sdk.model.UploadSource;
import dev.dosya.sdk.model.UploadStatusResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Uploads: one file (single request or resumable multipart), resuming a session, the
 * low-level session calls, and many small files through the batch door.
 *
 * <p>Every upload route needs an {@code upload} or {@code full} key scope (a {@code read}
 * key is refused, including on {@code status}) and the role's upload permissions. A
 * same-name file in the destination is adopted as a new version.
 *
 * @since 0.1.0
 */
public final class UploadResource {

    private static final long MIB = 1024L * 1024L;
    private static final int DEFAULT_CONCURRENCY = 3;
    private static final int SETTLE_POLLS = 6;
    private static final int STREAM_CHUNK = (int) MIB;
    private static final Pattern MACHINE_CODE = Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final Pattern ALREADY_COMPLETE = Pattern.compile("(?i)already complete");

    /**
     * Server's maximum number of files per batch request.
     *
     * @since 0.3.0
     */
    public static final int BATCH_MAX_FILES = 200;
    /**
     * Server's per-file ceiling for the batch door.
     *
     * @since 0.3.0
     */
    public static final long BATCH_FILE_MAX_BYTES = 5 * MIB;
    /**
     * Server's summed-bytes ceiling per batch request.
     *
     * @since 0.3.0
     */
    public static final long BATCH_TOTAL_MAX_BYTES = 100 * MIB;
    /** Bytes per batch request that {@code many()} aims for: one batch is one unit of failure. */
    private static final long MANY_BATCH_TARGET_BYTES = 48 * MIB;

    private static final AtomicInteger THREADS = new AtomicInteger();

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code UploadResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public UploadResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    // ── One file ──

    /**
     * Uploads one file: init, then a single PUT (up to 50 MiB) or a resumable multipart
     * upload (10 MiB parts, {@code concurrency} in flight, the first part of a fresh session
     * alone).
     *
     * <p>Retries: a failed single PUT is repeated on a fresh session, since the old one
     * cannot accept the bytes again. Parts and complete are retried on network errors,
     * timeouts, 5xx and 429 with {@code Retry-After}; 4xx is never retried. Before repeating a
     * PUT or complete whose outcome was not seen, the session status is checked so the file is
     * never stored twice.
     *
     * @return the stored file and the session id
     * @throws DosyaApiException from init, PUT or complete, with code
     *         {@code concurrent_upload_limit} (wait for other uploads), {@code version_conflict}
     *         ({@code details.current_version}), {@code hash_mismatch} or {@code folder_locked}
     * @throws DosyaUploadException when a part keeps failing (carries {@code sessionId} and
     *         {@code partNumber}; pass the session id to {@link #resume}), or when an attempt's
     *         outcome is unknown and repeating it could store the file twice
     */
    public @NotNull UploadResult file(@NotNull UploadParams params) {
        long size = params.getFileSize();
        int concurrency = params.getConcurrency() != null ? params.getConcurrency() : DEFAULT_CONCURRENCY;
        Consumer<UploadProgress> onProgress = params.getOnProgress();
        emit(onProgress, progress(0, size, "initializing", null, null));

        UploadInitParams initParams = new UploadInitParams(params.getWorkspaceId(), params.getFileName(), size)
                .mimeType(params.getMimeType())
                .folderId(params.getFolderId())
                .fileId(params.getFileId())
                .expectedVersion(params.getExpectedVersion());
        UploadInitResponse session = init(initParams);
        Times times = new Times(params.getSourceModifiedAt(), params.getSourceCreatedAt());

        if (session.getResumable() != null) {
            return runMultipart(new Job(session.getSessionId(), session.getResumable().getPartSize(),
                    session.getResumable().getTotalParts(), size, params.getSource(), new HashSet<>(),
                    concurrency, onProgress, times));
        }
        return runSingle(session, initParams, params, size, concurrency, times);
    }

    /**
     * Continues a multipart session: re-sends only the parts the server lacks, then completes.
     *
     * @see #resume(String, UploadSource, UploadResumeOptions)
     * @since 0.3.0
     */
    public @NotNull UploadResult resume(@NotNull String sessionId, @NotNull UploadSource source) {
        return resume(sessionId, source, new UploadResumeOptions());
    }

    /**
     * Continues a multipart session: re-sends only the parts the server lacks, then completes.
     * {@code source} must be the same bytes (its size must equal the session's). A session
     * with no parts yet gets its first part alone. Single-request sessions (files up to
     * 50 MiB) cannot be resumed; start a new upload.
     *
     * @throws DosyaUploadException when the session is complete or not multipart
     * @throws IllegalArgumentException when the source size differs from the session's
     * @since 0.3.0
     */
    public @NotNull UploadResult resume(@NotNull String sessionId, @NotNull UploadSource source,
                                        @NotNull UploadResumeOptions options) {
        int concurrency = options.getConcurrency() != null ? options.getConcurrency() : DEFAULT_CONCURRENCY;
        UploadStatusResponse st = status(sessionId);
        if ("complete".equals(st.getStatus())) {
            throw new DosyaUploadException(
                    "Upload session is already complete; the file exists but the session does not report its id", sessionId);
        }
        Integer partSize = st.getPartSize();
        Integer totalParts = st.getTotalParts();
        // Gate on the session shape, not hasMultipart: that only says a part has landed.
        if (partSize == null || partSize <= 0 || totalParts == null || totalParts <= 0) {
            throw new DosyaUploadException("Cannot resume a single-request upload session; start a new upload", sessionId);
        }
        if (source.getSize() != st.getSizeBytes()) {
            throw new IllegalArgumentException("body is " + source.getSize() + " bytes but upload session "
                    + sessionId + " expects " + st.getSizeBytes());
        }
        Set<Integer> uploaded = new HashSet<>();
        for (Integer n : st.getUploadedParts()) {
            if (n != null && n >= 1 && n <= totalParts) uploaded.add(n);
        }
        return runMultipart(new Job(sessionId, partSize, totalParts, st.getSizeBytes(), source, uploaded,
                concurrency, options.getOnProgress(),
                new Times(options.getSourceModifiedAt(), options.getSourceCreatedAt())));
    }

    /**
     * Resumes a multipart session from bytes in memory.
     *
     * @deprecated use {@link #resume(String, UploadSource, UploadResumeOptions)}.
     */
    @Deprecated
    public @NotNull UploadResult resume(@NotNull String sessionId, byte @NotNull [] body,
                                        @Nullable Consumer<UploadProgress> onProgress) {
        return resume(sessionId, UploadSource.ofBytes(body), new UploadResumeOptions().onProgress(onProgress));
    }

    // ── Low level ──

    /**
     * Opens an upload session ({@code POST /api/upload/init}). Files over 50 MiB get
     * {@code resumable} part info. Refusals: 400 {@code concurrent_upload_limit},
     * 409 {@code version_conflict}, 403 {@code folder_locked}, 404 "Destination folder not
     * found", 413 size caps.
     *
     * @since 0.3.0
     */
    public @NotNull UploadInitResponse init(@NotNull UploadInitParams params) {
        return http.requestAs(HttpRequest.post("/api/upload/init").body(params.toBody()), UploadInitResponse.class);
    }

    /**
     * Opens an upload session.
     *
     * @deprecated {@code region} is ignored by the server and no longer sent; use
     * {@link #init(UploadInitParams)}.
     */
    @Deprecated
    public @NotNull UploadInitResponse init(@NotNull String workspaceId, @NotNull String fileName, long fileSize,
                                            @Nullable String mimeType, @Nullable String region,
                                            @Nullable String folderId, @Nullable String fileId) {
        return init(new UploadInitParams(workspaceId, fileName, fileSize)
                .mimeType(mimeType).folderId(folderId).fileId(fileId));
    }

    /**
     * Session state ({@code GET /api/upload/{id}/status}). {@code hasMultipart} only means a
     * part has landed. There is no file id here, so a completed session cannot be mapped back
     * to its file.
     */
    public @NotNull UploadStatusResponse status(@NotNull String sessionId) {
        return http.requestAs(HttpRequest.get("/api/upload/" + seg(sessionId) + "/status"), UploadStatusResponse.class);
    }

    /**
     * Sends one part (1-based) of a multipart session ({@code PUT /api/upload/{id}/part/{n}}).
     * At most {@code partSize} bytes (413 otherwise); idempotent server-side
     * ({@code alreadyUploaded}). Not retried. On a session with no parts yet, send the first
     * part alone and wait for it before sending others: the first part creates the storage
     * upload, and racing parts break the session.
     *
     * @throws IllegalArgumentException for a part number below 1
     * @since 0.3.0
     */
    public @NotNull UploadPartResult uploadPart(@NotNull String sessionId, int partNumber, byte @NotNull [] bytes) {
        if (partNumber < 1) throw new IllegalArgumentException("partNumber must be a positive integer, got " + partNumber);
        return http.requestAs(HttpRequest.put("/api/upload/" + seg(sessionId) + "/part/" + partNumber)
                .rawBody(bytes)
                .header("Content-Type", "application/octet-stream")
                .timeoutMs(http.getUploadTimeout())
                .retry(HttpRequest.Retry.NEVER), UploadPartResult.class);
    }

    /**
     * Finishes a multipart session after every part is stored.
     *
     * @see #complete(String, Instant, Instant)
     * @since 0.3.0
     */
    public @NotNull UploadResult complete(@NotNull String sessionId) {
        return completeOnce(sessionId, new Times(null, null));
    }

    /**
     * Finishes a multipart session after every part is stored
     * ({@code POST /api/upload/{id}/complete}), sending the source dates as headers. Not
     * retried. 409 when already complete; 400 when parts are missing.
     *
     * @param sourceModifiedAt original modification time, or null
     * @param sourceCreatedAt  original creation time, or null
     * @since 0.3.0
     */
    public @NotNull UploadResult complete(@NotNull String sessionId, @Nullable Instant sourceModifiedAt,
                                          @Nullable Instant sourceCreatedAt) {
        return completeOnce(sessionId, new Times(
                sourceModifiedAt != null ? sourceModifiedAt.getEpochSecond() : null,
                sourceCreatedAt != null ? sourceCreatedAt.getEpochSecond() : null));
    }

    // ── Many files ──

    /**
     * Uploads up to 200 small files (each at most 5 MiB, 100 MiB together) in one request
     * ({@code POST /api/upload/batch}). Limits are checked before sending. Per-file refusals do
     * not throw: each entry says ok or carries {@code error} (and {@code currentVersion} for
     * {@code version_conflict}). Whole-request failures (permission, workspace pin, network)
     * throw. Never retried, since the request is not idempotent.
     *
     * @return one entry per input file, in input order
     * @throws IllegalArgumentException for an empty list or a limit exceeded, before any request
     * @since 0.3.0
     */
    public @NotNull UploadBatchResult batch(@NotNull String workspaceId, @NotNull List<UploadBatchFile> files) {
        requireWorkspace(workspaceId);
        if (files == null || files.isEmpty()) throw new IllegalArgumentException("files must be a non-empty list");
        if (files.size() > BATCH_MAX_FILES) {
            throw new IllegalArgumentException("A batch holds at most " + BATCH_MAX_FILES + " files, got " + files.size());
        }
        long total = 0;
        for (int i = 0; i < files.size(); i++) {
            UploadBatchFile f = files.get(i);
            if (f == null) throw new IllegalArgumentException("files[" + i + "] is null");
            long size = f.getSource().getSize();
            if (size > BATCH_FILE_MAX_BYTES) {
                throw new IllegalArgumentException("files[" + i + "] (" + f.getName() + ") is " + size
                        + " bytes; the batch limit is 5 MiB per file");
            }
            total += size;
        }
        if (total > BATCH_TOTAL_MAX_BYTES) {
            throw new IllegalArgumentException("Batch is " + total + " bytes; the limit is 100 MiB per request");
        }

        // The manifest is a form field, so its keys are written in snake_case by hand.
        JsonObject manifest = new JsonObject();
        manifest.addProperty("workspace_id", workspaceId);
        JsonArray entries = new JsonArray();
        for (int i = 0; i < files.size(); i++) {
            UploadBatchFile f = files.get(i);
            JsonObject e = new JsonObject();
            e.addProperty("name", f.getName());
            e.add("folder_id", f.getFolderId() != null ? new JsonPrimitive(f.getFolderId()) : JsonNull.INSTANCE);
            e.add("file_id", f.getFileId() != null ? new JsonPrimitive(f.getFileId()) : JsonNull.INSTANCE);
            e.addProperty("field", "f" + i);
            if (f.getSha256() != null) e.addProperty("sha256", f.getSha256().toLowerCase(Locale.ROOT));
            if (f.getExpectedVersion() != null) e.addProperty("expected_version", f.getExpectedVersion());
            if (f.getSourceModifiedAt() != null) e.addProperty("source_modified_at", f.getSourceModifiedAt());
            if (f.getSourceCreatedAt() != null) e.addProperty("source_created_at", f.getSourceCreatedAt());
            entries.add(e);
        }
        manifest.add("files", entries);

        String boundary = "dosya-java-" + UUID.randomUUID().toString().replace("-", "");
        MultipartBody body = new MultipartBody(boundary, manifest.toString(), files);

        JsonObject res = http.request(HttpRequest.post("/api/upload/batch")
                .rawBody(body.publisher())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeoutMs(http.getUploadTimeout())
                .retry(HttpRequest.Retry.NEVER));

        // Results come back committed first, then failures - match them on `field`.
        Map<String, JsonObject> byField = new HashMap<>();
        JsonElement results = res.get("results");
        if (results != null && results.isJsonArray()) {
            for (JsonElement r : results.getAsJsonArray()) {
                if (r.isJsonObject() && string(r.getAsJsonObject(), "field") != null) {
                    byField.put(string(r.getAsJsonObject(), "field"), r.getAsJsonObject());
                }
            }
        }
        List<UploadBatchResult.Entry> out = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            JsonObject r = byField.get("f" + i);
            if (r == null) {
                out.add(UploadBatchResult.Entry.failure("The server did not report a result for this file", null));
            } else if (bool(r, "ok")) {
                String fileId = string(r, "fileId") != null ? string(r, "fileId") : string(r, "file_id");
                Integer version = integer(r, "version");
                out.add(UploadBatchResult.Entry.success(fileId != null ? fileId : "",
                        string(r, "name") != null ? string(r, "name") : files.get(i).getName(),
                        version != null ? version : 1));
            } else {
                String error = string(r, "error");
                out.add(UploadBatchResult.Entry.failure(error != null ? error : "Upload failed", integer(r, "current_version")));
            }
        }
        return new UploadBatchResult(out);
    }

    /**
     * Uploads many files.
     *
     * @see #many(String, List, UploadManyOptions)
     * @since 0.3.0
     */
    public @NotNull List<UploadManyResult> many(@NotNull String workspaceId, @NotNull List<UploadBatchFile> files) {
        return many(workspaceId, files, new UploadManyOptions());
    }

    /**
     * Uploads many files, choosing the door per file: in-memory and on-disk files of at most
     * 5 MiB go through {@link #batch} (up to 200 files and about 48 MiB per request); larger
     * files and streams go through {@link #file}. Results come back in input order and
     * per-file failures never throw - including a whole batch request failing, which fails
     * each file in it with {@code cause} set. It throws only for invalid input, before any
     * request, and when the calling thread is interrupted.
     *
     * @since 0.3.0
     */
    public @NotNull List<UploadManyResult> many(@NotNull String workspaceId, @NotNull List<UploadBatchFile> files,
                                                @NotNull UploadManyOptions options) {
        requireWorkspace(workspaceId);
        if (files == null) throw new IllegalArgumentException("files must be a list");
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i) == null) throw new IllegalArgumentException("files[" + i + "] is null");
        }
        int concurrency = options.getConcurrency() != null ? options.getConcurrency() : DEFAULT_CONCURRENCY;
        Consumer<UploadManyProgress> onProgress = options.getOnProgress();
        int count = files.size();
        UploadManyResult[] results = new UploadManyResult[count];
        if (count == 0) return Collections.emptyList();

        long[] sizes = new long[count];
        long totalBytes = 0;
        for (int i = 0; i < count; i++) {
            sizes[i] = files.get(i).getSource().getSize();
            totalBytes += sizes[i];
        }
        ManyState state = new ManyState(count, totalBytes, onProgress);

        // Plan: small files into batches, the rest one by one.
        List<int[]> tasks = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        long currentBytes = 0;
        for (int i = 0; i < count; i++) {
            UploadSource source = files.get(i).getSource();
            if (source.isStream() || sizes[i] > BATCH_FILE_MAX_BYTES) {
                tasks.add(new int[] {-1, i});
                continue;
            }
            if (current.size() >= BATCH_MAX_FILES
                    || (!current.isEmpty() && currentBytes + sizes[i] > MANY_BATCH_TARGET_BYTES)) {
                tasks.add(toBatchTask(current));
                current = new ArrayList<>();
                currentBytes = 0;
            }
            current.add(i);
            currentBytes += sizes[i];
        }
        if (!current.isEmpty()) tasks.add(toBatchTask(current));
        tasks.sort((a, b) -> Integer.compare(a[1], b[1]));

        runPool("dosya-upload-many", tasks, concurrency, task -> {
            if (task[0] == 1) {
                List<UploadBatchFile> subset = new ArrayList<>(task.length - 1);
                for (int k = 1; k < task.length; k++) subset.add(files.get(task[k]));
                try {
                    List<UploadBatchResult.Entry> entries = batch(workspaceId, subset).getResults();
                    for (int k = 0; k < entries.size(); k++) {
                        int i = task[k + 1];
                        UploadBatchResult.Entry entry = entries.get(k);
                        if (entry.isOk()) {
                            state.settle(results, i, UploadManyResult.success(entry.getName(), entry.getFileId(),
                                    entry.getVersion(), null), sizes[i]);
                        } else {
                            String error = entry.getError();
                            state.settle(results, i, UploadManyResult.failure(files.get(i).getName(), error,
                                    MACHINE_CODE.matcher(error).matches() ? error : null,
                                    entry.getCurrentVersion(), null), sizes[i]);
                        }
                    }
                } catch (RuntimeException err) {
                    if (Thread.currentThread().isInterrupted()) throw err;
                    for (int k = 1; k < task.length; k++) {
                        state.settle(results, task[k], failureFrom(files.get(task[k]).getName(), err), sizes[task[k]]);
                    }
                }
            } else {
                int i = task[1];
                UploadBatchFile f = files.get(i);
                try {
                    UploadParams params = UploadParams.of(workspaceId, f.getName(), f.getSource())
                            .folderId(f.getFolderId())
                            .fileId(f.getFileId())
                            .sha256(f.getSha256())
                            .expectedVersion(f.getExpectedVersion());
                    if (f.getSourceModifiedAt() != null) params.sourceModifiedAt(f.getSourceModifiedAt());
                    if (f.getSourceCreatedAt() != null) params.sourceCreatedAt(f.getSourceCreatedAt());
                    if (onProgress != null) params.onProgress(p -> state.partial(i, p.getBytesUploaded()));
                    UploadResult r = file(params);
                    state.settle(results, i, UploadManyResult.success(r.getFile().getName(), r.getFile().getId(),
                            r.getFile().getVersion(), r.getFile()), sizes[i]);
                } catch (RuntimeException err) {
                    if (Thread.currentThread().isInterrupted()) throw err;
                    state.settle(results, i, failureFrom(f.getName(), err), sizes[i]);
                }
            }
            state.report();
        });

        List<UploadManyResult> out = new ArrayList<>(count);
        Collections.addAll(out, results);
        return Collections.unmodifiableList(out);
    }

    // ── Private: single request ──

    private UploadResult runSingle(UploadInitResponse first, UploadInitParams initParams, UploadParams params,
                                   long size, int concurrency, Times times) {
        Consumer<UploadProgress> onProgress = params.getOnProgress();
        UploadInitResponse session = first;

        UploadSource source = params.getSource();
        String sha256 = params.getSha256() != null ? params.getSha256().toLowerCase(Locale.ROOT) : null;
        try {
            // A stream cannot be re-read for a retry: hold its (at most 50 MiB) bytes.
            if (source.isStream()) source = bufferStream(source, size);
            if (sha256 == null && params.isComputeSha256()) sha256 = sha256Hex(source);
        } catch (IOException | IllegalArgumentException e) {
            throw new DosyaUploadException(e.getMessage() != null ? e.getMessage() : e.toString(),
                    session.getSessionId(), null, e);
        }

        for (int attempt = 0; ; attempt++) {
            if (session.getResumable() != null) {
                // Cannot happen for the same declared size, but never PUT a multipart session.
                return runMultipart(new Job(session.getSessionId(), session.getResumable().getPartSize(),
                        session.getResumable().getTotalParts(), size, source, new HashSet<>(), concurrency,
                        onProgress, times));
            }

            emit(onProgress, progress(0, size, "uploading", null, null));
            String sessionId = session.getSessionId();
            try {
                HttpRequest req = HttpRequest.put("/api/upload/" + seg(sessionId))
                        .rawBody(publisherFor(source, sessionId))
                        .header("Content-Type", params.getMimeType() != null ? params.getMimeType() : "application/octet-stream")
                        .header("X-Dosya-Sha256", sha256)
                        .timeoutMs(http.getUploadTimeout())
                        .retry(HttpRequest.Retry.NEVER);
                times.apply(req);
                UploadResult result = new UploadResult(fileFrom(http.request(req)), sessionId);
                emit(onProgress, progress(size, size, "complete", null, null));
                return result;
            } catch (DosyaUploadException err) {
                throw err;
            } catch (DosyaException err) {
                Long delay = retryDelay(err, attempt);
                if (delay == null) throw err;

                // A dropped connection, a timeout or an edge 5xx may hide a commit that happened
                // (or is still happening). Re-sending would store the bytes again as a new
                // version, so learn the session's outcome first.
                if (!isPreHandlerRefusal(err)) settleBeforeRetry(sessionId, err);

                // The server marks a session failed (or leaves it uploading) once a PUT has
                // started, so the same session would answer 409: open a new one.
                http.sleep(delay);
                emit(onProgress, progress(0, size, "initializing", null, null));
                session = init(initParams);
            }
        }
    }

    private static BodyPublisher publisherFor(UploadSource source, String sessionId) {
        if (source.getPath() != null) {
            try {
                return BodyPublishers.ofFile(source.getPath());
            } catch (FileNotFoundException e) {
                throw new DosyaUploadException("File not found: " + source.getPath(), sessionId, null, e);
            }
        }
        try {
            return BodyPublishers.ofByteArray(source.read(0, (int) source.getSize()));
        } catch (IOException e) {
            throw new DosyaUploadException("Failed to read the upload body: " + e.getMessage(), sessionId, null, e);
        }
    }

    private static UploadSource bufferStream(UploadSource source, long size) throws IOException {
        if (size > Integer.MAX_VALUE - 8) throw new IllegalArgumentException("Stream is too large to buffer: " + size);
        byte[] bytes = source.read(0, (int) size);
        if (bytes.length != size) {
            throw new IllegalArgumentException("Stream ended after " + bytes.length + " bytes; declared size is " + size);
        }
        if (source.exceedsDeclaredSize()) {
            throw new IllegalArgumentException("Stream is longer than the declared size " + size);
        }
        return UploadSource.ofBytes(bytes);
    }

    private static String sha256Hex(UploadSource source) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
        if (source.getPath() != null) {
            try (InputStream in = Files.newInputStream(source.getPath())) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) != -1) digest.update(buf, 0, n);
            }
        } else {
            digest.update(source.read(0, (int) source.getSize()));
        }
        StringBuilder hex = new StringBuilder(64);
        for (byte b : digest.digest()) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    // ── Private: multipart ──

    private UploadResult runMultipart(Job job) {
        String sessionId = job.sessionId;
        Object lock = new Object();
        long[] bytesDone = {0};
        for (int n : job.uploaded) bytesDone[0] += job.partLength(n);
        Runnable emitUploading = () -> emit(job.onProgress, progress(bytesDone[0], job.totalBytes, "uploading",
                job.uploaded.size(), job.totalParts));
        synchronized (lock) {
            emitUploading.run();
        }

        PartSender send = (n, bytes) -> {
            sendPartWithRetry(sessionId, n, bytes);
            synchronized (lock) {
                if (job.uploaded.add(n)) bytesDone[0] += job.partLength(n);
                emitUploading.run();
            }
        };

        List<Integer> missing = new ArrayList<>();
        synchronized (lock) {
            for (int n = 1; n <= job.totalParts; n++) if (!job.uploaded.contains(n)) missing.add(n);
        }

        if (job.source.isStream()) {
            // A stream is read once, in order, so parts go one at a time.
            for (int n = 1; n <= job.totalParts; n++) {
                int expected = (int) job.partLength(n);
                byte[] chunk = readPart(job, n);
                if (chunk.length != expected) {
                    throw new DosyaUploadException("Stream ended early: part " + n + " has " + chunk.length + " of "
                            + expected + " bytes (declared size " + job.totalBytes + ")", sessionId, n);
                }
                boolean stored;
                synchronized (lock) {
                    stored = job.uploaded.contains(n);
                }
                if (!stored) send.send(n, chunk);
            }
            try {
                if (job.source.exceedsDeclaredSize()) {
                    throw new DosyaUploadException("Stream is longer than the declared size " + job.totalBytes, sessionId);
                }
            } catch (IOException e) {
                throw new DosyaUploadException("Failed to read the stream: " + e.getMessage(), sessionId, null, e);
            }
        } else {
            List<Integer> queue = missing;
            // The first part creates the storage multipart upload and fixes the object key.
            // Parts racing it each create their own, and the losers' etags make the session
            // impossible to complete - so a session with no parts yet gets its first part alone.
            if (job.uploaded.isEmpty() && !queue.isEmpty()) {
                int firstPart = queue.get(0);
                send.send(firstPart, readPart(job, firstPart));
                queue = queue.subList(1, queue.size());
            }
            runPool("dosya-upload-part", new ArrayList<>(queue), job.concurrency, n -> {
                byte[] bytes = readPart(job, n);
                if (bytes.length != job.partLength(n)) {
                    throw new DosyaUploadException("Source is shorter than its size " + job.totalBytes
                            + " (part " + n + " has " + bytes.length + " bytes)", sessionId, n);
                }
                send.send(n, bytes);
            });
        }

        synchronized (lock) {
            emit(job.onProgress, progress(bytesDone[0], job.totalBytes, "completing", job.uploaded.size(), job.totalParts));
        }
        UploadResult result = completeWithRetry(sessionId, job.times);
        emit(job.onProgress, progress(job.totalBytes, job.totalBytes, "complete", job.totalParts, job.totalParts));
        return result;
    }

    private static byte[] readPart(Job job, int n) {
        try {
            return job.source.read((long) (n - 1) * job.partSize, (int) job.partLength(n));
        } catch (IOException | IllegalStateException e) {
            throw new DosyaUploadException("Failed to read part " + n + ": " + e.getMessage(), job.sessionId, n, e);
        }
    }

    private void sendPartWithRetry(String sessionId, int partNumber, byte[] bytes) {
        for (int attempt = 0; ; attempt++) {
            try {
                uploadPart(sessionId, partNumber, bytes);
                return;
            } catch (DosyaException err) {
                Long delay = retryDelay(err, attempt);
                if (delay == null) {
                    throw new DosyaUploadException("Failed to upload part " + partNumber + ": " + err.getMessage(),
                            sessionId, partNumber, err);
                }
                http.sleep(delay);
            }
        }
    }

    private UploadResult completeOnce(String sessionId, Times times) {
        HttpRequest req = HttpRequest.post("/api/upload/" + seg(sessionId) + "/complete")
                // Assembling a large object and committing it can take well over the read timeout.
                .timeoutMs(http.getUploadTimeout())
                .retry(HttpRequest.Retry.NEVER);
        times.apply(req);
        return new UploadResult(fileFrom(http.request(req)), sessionId);
    }

    private UploadResult completeWithRetry(String sessionId, Times times) {
        for (int attempt = 0; ; attempt++) {
            try {
                return completeOnce(sessionId, times);
            } catch (DosyaUploadException err) {
                throw err;
            } catch (DosyaException err) {
                if (attempt > 0 && err instanceof DosyaApiException
                        && ((DosyaApiException) err).getStatus() == 409
                        && ((DosyaApiException) err).getErrorMessage() != null
                        && ALREADY_COMPLETE.matcher(((DosyaApiException) err).getErrorMessage()).find()) {
                    throw new DosyaUploadException("The upload completed on an earlier attempt whose response was lost; "
                            + "the file exists but its id is unknown", sessionId, null, err);
                }
                Long delay = retryDelay(err, attempt);
                if (delay == null) throw err;
                // Only repeat complete once the first attempt is known to have ended without
                // finishing; a racing second complete can fail a finished session.
                if (!isPreHandlerRefusal(err)) settleBeforeRetry(sessionId, err);
                http.sleep(delay);
            }
        }
    }

    /**
     * Waits until the server has finished handling an attempt whose outcome the client could
     * not see. Returns when the session did not complete; throws when it did (the file
     * exists), when it is still being processed after polling, or when its state cannot be
     * read - in each case a retry could store the file twice.
     */
    private void settleBeforeRetry(String sessionId, Throwable cause) {
        for (int i = 0; ; i++) {
            UploadStatusResponse st;
            try {
                st = status(sessionId);
            } catch (DosyaException e) {
                throw new DosyaUploadException("Upload failed and its outcome could not be confirmed; "
                        + "not retrying to avoid storing the file twice", sessionId, null, cause);
            }
            if ("complete".equals(st.getStatus())) {
                throw new DosyaUploadException("The upload completed but its response was lost; "
                        + "the file exists but its id is unknown", sessionId, null, cause);
            }
            if (!"uploading".equals(st.getStatus())) return;
            if (i >= SETTLE_POLLS) {
                throw new DosyaUploadException("The server is still processing an earlier attempt; "
                        + "not retrying to avoid storing the file twice", sessionId, null, cause);
            }
            // Backoff from the client's retry settings: about 30 s in total by default.
            http.sleep(http.backoff(i));
        }
    }

    /** Delay before the next attempt, or null when {@code err} must surface. */
    private @Nullable Long retryDelay(DosyaException err, int attempt) {
        if (Thread.currentThread().isInterrupted()) return null;
        if (attempt >= http.getMaxRetries()) return null;
        if (err instanceof DosyaNetworkException) return http.backoff(attempt);
        if (err instanceof DosyaApiException) {
            DosyaApiException api = (DosyaApiException) err;
            Long retryAfter = api.getRetryAfterSeconds();
            if (retryAfter != null) {
                long ms = retryAfter * 1000;
                if (ms > http.getMaxDelay()) return null;
                return api.getStatus() == 429 || api.getStatus() >= 500 ? ms : null;
            }
            // A 429 without Retry-After is a business cap that waiting will not clear.
            if (api.getStatus() >= 500) return http.backoff(attempt);
        }
        return null;
    }

    /** A refusal issued by middleware before any handler ran (rate limit with Retry-After). */
    private static boolean isPreHandlerRefusal(DosyaException err) {
        return err instanceof DosyaApiException && ((DosyaApiException) err).getStatus() == 429
                && ((DosyaApiException) err).getRetryAfterSeconds() != null;
    }

    // ── Private: helpers ──

    private UploadResult.UploadedFile fileFrom(JsonObject res) {
        JsonElement file = res.get("file");
        if (file == null || !file.isJsonObject()) throw new DosyaException("The upload response did not contain a file");
        return http.fromJson(file, UploadResult.UploadedFile.class);
    }

    private static UploadProgress progress(long bytes, long total, String status,
                                           @Nullable Integer partsCompleted, @Nullable Integer totalParts) {
        int percent = total > 0 ? (int) Math.round(bytes * 100.0 / total) : ("complete".equals(status) ? 100 : 0);
        if ("complete".equals(status)) percent = 100;
        else if (percent > 99 && "completing".equals(status)) percent = 99;
        return new UploadProgress(bytes, total, percent, partsCompleted, totalParts, status);
    }

    private static void emit(@Nullable Consumer<UploadProgress> onProgress, UploadProgress p) {
        if (onProgress != null) onProgress.accept(p);
    }

    private static void requireWorkspace(String workspaceId) {
        if (workspaceId == null || workspaceId.trim().isEmpty()) throw new IllegalArgumentException("workspaceId is required");
    }

    private static int[] toBatchTask(List<Integer> indices) {
        int[] task = new int[indices.size() + 1];
        task[0] = 1;
        for (int k = 0; k < indices.size(); k++) task[k + 1] = indices.get(k);
        return task;
    }

    private static UploadManyResult failureFrom(String name, Throwable err) {
        if (err instanceof DosyaApiException) {
            DosyaApiException api = (DosyaApiException) err;
            return UploadManyResult.failure(name, api.getErrorMessage() != null ? api.getErrorMessage() : api.getMessage(),
                    api.getCode(), integer(api.getDetails(), "current_version"), api);
        }
        if (err instanceof DosyaUploadException && err.getCause() instanceof DosyaApiException) {
            UploadManyResult inner = failureFrom(name, err.getCause());
            return UploadManyResult.failure(name, err.getMessage(), inner.getCode(), inner.getCurrentVersion(), err);
        }
        return UploadManyResult.failure(name, err.getMessage() != null ? err.getMessage() : err.toString(), null, null, err);
    }

    private static @Nullable String string(JsonObject json, String key) {
        JsonElement e = json.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
    }

    private static @Nullable Integer integer(JsonObject json, String key) {
        JsonElement e = json.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsInt() : null;
    }

    private static boolean bool(JsonObject json, String key) {
        JsonElement e = json.get(key);
        return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean() && e.getAsBoolean();
    }

    /**
     * Runs {@code fn} over {@code items} with at most {@code limit} in flight on a bounded pool
     * that is always shut down. Stops starting new work after the first failure (work already
     * in flight finishes) and rethrows that failure.
     */
    private static <T> void runPool(String name, List<T> items, int limit, ItemTask<T> fn) {
        if (items.isEmpty()) return;
        int threads = Math.min(limit, items.size());
        ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>(items);
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(threads, daemonThreads(name));
        try {
            List<Future<?>> workers = new ArrayList<>(threads);
            for (int w = 0; w < threads; w++) {
                workers.add(executor.submit(() -> {
                    T item;
                    while (failure.get() == null && (item = queue.poll()) != null) {
                        try {
                            fn.run(item);
                        } catch (RuntimeException e) {
                            failure.compareAndSet(null, e);
                        }
                    }
                }));
            }
            executor.shutdown();
            for (Future<?> worker : workers) {
                try {
                    worker.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    executor.shutdownNow();
                    throw new DosyaNetworkException("Upload interrupted", e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Error) throw (Error) cause;
                    failure.compareAndSet(null, cause instanceof RuntimeException
                            ? (RuntimeException) cause : new DosyaException(String.valueOf(cause.getMessage()), cause));
                }
            }
        } finally {
            executor.shutdownNow();
        }
        RuntimeException first = failure.get();
        if (first != null) throw first;
    }

    private static ThreadFactory daemonThreads(String name) {
        return r -> {
            Thread t = new Thread(r, name + "-" + THREADS.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }

    @FunctionalInterface
    private interface ItemTask<T> {
        void run(T item);
    }

    @FunctionalInterface
    private interface PartSender {
        void send(int partNumber, byte[] bytes);
    }

    /** Source dates in unix seconds, sent as headers on the single PUT and complete. */
    private static final class Times {
        final Long modifiedAt;
        final Long createdAt;

        Times(@Nullable Long modifiedAt, @Nullable Long createdAt) {
            this.modifiedAt = modifiedAt;
            this.createdAt = createdAt;
        }

        void apply(HttpRequest req) {
            if (modifiedAt != null) req.header("X-Dosya-Source-Mtime", String.valueOf(modifiedAt));
            if (createdAt != null) req.header("X-Dosya-Source-Ctime", String.valueOf(createdAt));
        }
    }

    private static final class Job {
        final String sessionId;
        final long partSize;
        final int totalParts;
        final long totalBytes;
        final UploadSource source;
        final Set<Integer> uploaded;
        final int concurrency;
        final Consumer<UploadProgress> onProgress;
        final Times times;

        Job(String sessionId, long partSize, int totalParts, long totalBytes, UploadSource source,
            Set<Integer> uploaded, int concurrency, Consumer<UploadProgress> onProgress, Times times) {
            this.sessionId = sessionId;
            this.partSize = partSize;
            this.totalParts = totalParts;
            this.totalBytes = totalBytes;
            this.source = source;
            this.uploaded = uploaded;
            this.concurrency = concurrency;
            this.onProgress = onProgress;
            this.times = times;
        }

        long partLength(int n) {
            return Math.max(0, Math.min(partSize, totalBytes - (long) (n - 1) * partSize));
        }
    }

    /** Book-keeping and progress for {@code many()}. */
    private static final class ManyState {
        private final int totalFiles;
        private final long totalBytes;
        private final Consumer<UploadManyProgress> onProgress;
        private final Map<Integer, Long> inFlight = new ConcurrentHashMap<>();
        private long finishedBytes;
        private int filesCompleted;
        private int filesFailed;

        ManyState(int totalFiles, long totalBytes, Consumer<UploadManyProgress> onProgress) {
            this.totalFiles = totalFiles;
            this.totalBytes = totalBytes;
            this.onProgress = onProgress;
        }

        synchronized void settle(UploadManyResult[] results, int index, UploadManyResult result, long size) {
            results[index] = result;
            inFlight.remove(index);
            if (result.isOk()) {
                filesCompleted++;
                finishedBytes += size;
            } else {
                filesFailed++;
            }
        }

        void partial(int index, long bytes) {
            inFlight.put(index, bytes);
            report();
        }

        synchronized void report() {
            if (onProgress == null) return;
            long partial = 0;
            for (long b : inFlight.values()) partial += b;
            onProgress.accept(new UploadManyProgress(filesCompleted, filesFailed, totalFiles, finishedBytes + partial, totalBytes));
        }
    }

    /**
     * A {@code multipart/form-data} body with a known length whose file parts are read lazily,
     * so a large batch never sits in memory whole.
     */
    private static final class MultipartBody {
        private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.US_ASCII);

        private final List<Object> segments = new ArrayList<>();
        private long length;

        MultipartBody(String boundary, String manifest, List<UploadBatchFile> files) {
            addText("--" + boundary + "\r\nContent-Disposition: form-data; name=\"manifest\"\r\n\r\n");
            addText(manifest);
            addText("\r\n");
            for (int i = 0; i < files.size(); i++) {
                // The filename must be present: without it the server reads a string, not a Blob.
                addText("--" + boundary + "\r\nContent-Disposition: form-data; name=\"f" + i + "\"; filename=\"f" + i
                        + "\"\r\nContent-Type: application/octet-stream\r\n\r\n");
                UploadSource source = files.get(i).getSource();
                segments.add(source);
                length += source.getSize();
                segments.add(CRLF);
                length += CRLF.length;
            }
            addText("--" + boundary + "--\r\n");
        }

        private void addText(String text) {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            segments.add(bytes);
            length += bytes.length;
        }

        BodyPublisher publisher() {
            Iterable<byte[]> chunks = () -> new Iterator<byte[]>() {
                private int index;
                private long offset;

                @Override
                public boolean hasNext() {
                    return index < segments.size();
                }

                @Override
                public byte[] next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    Object segment = segments.get(index);
                    if (segment instanceof byte[]) {
                        index++;
                        return (byte[]) segment;
                    }
                    UploadSource source = (UploadSource) segment;
                    long remaining = source.getSize() - offset;
                    if (remaining <= 0) {
                        index++;
                        offset = 0;
                        return new byte[0];
                    }
                    int take = (int) Math.min(STREAM_CHUNK, remaining);
                    byte[] chunk;
                    try {
                        chunk = source.read(offset, take);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    if (chunk.length != take) {
                        throw new UncheckedIOException(new IOException("A batch file is shorter than its size "
                                + source.getSize()));
                    }
                    offset += take;
                    if (offset >= source.getSize()) {
                        index++;
                        offset = 0;
                    }
                    return chunk;
                }
            };
            return BodyPublishers.fromPublisher(BodyPublishers.ofByteArrays(chunks), length);
        }
    }
}
