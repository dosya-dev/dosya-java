package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.exception.DosyaException;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CancelRemoteDownloadResult;
import dev.dosya.sdk.model.CreateRemoteDownloadParams;
import dev.dosya.sdk.model.RemoteDownloadJob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Server-side fetches of a public URL into workspace storage. A finished job creates a
 * file and emits a {@code file.uploaded} webhook.
 *
 * @since 0.3.0
 */
public final class RemoteDownloadsResource {

    /** Default poll interval of {@link #waitFor(String, String)}, in milliseconds. */
    public static final long DEFAULT_POLL_INTERVAL_MS = 2000;

    private static final Type JOB_LIST = new TypeToken<List<RemoteDownloadJob>>() {}.getType();

    private final DosyaHttpClient http;

    /**
     * Creates a new {@code RemoteDownloadsResource} backed by the given HTTP client.
     *
     * @param http the HTTP client used to make API requests
     */
    public RemoteDownloadsResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Lists the workspace's jobs (every member's), newest first, max 50. Dismissed jobs are
     * omitted. This is also how progress is polled - there is no single-job GET.
     *
     * @param workspaceId the workspace
     * @return the jobs
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull List<RemoteDownloadJob> list(@NotNull String workspaceId) {
        JsonObject resp = http.request(listRequest(workspaceId));
        List<RemoteDownloadJob> jobs = http.fromJson(resp.get("jobs"), JOB_LIST);
        return jobs == null ? Collections.emptyList() : Collections.unmodifiableList(jobs);
    }

    /**
     * Starts fetching a URL and returns the {@code queued} job. Needs a {@code full} scope key
     * (upload-scope keys are refused), the {@code upload_files} permission, and a key that is
     * NOT workspace-pinned.
     *
     * <p>Limits: 3 concurrent jobs and 20 jobs per 24h per workspace (429 without
     * {@code Retry-After}, so never retried). The API probes the URL first; a refusal is a 400
     * whose {@link dev.dosya.sdk.exception.DosyaApiException#getCode()} is {@code ssrf_blocked},
     * {@code not_a_file}, {@code unknown_size}, {@code network}, {@code http_<status>},
     * {@code too_large} (over 1 TB when the source supports Range requests and sends an ETag or
     * Last-Modified, otherwise over 10 GiB) or {@code quota}.
     *
     * @param params workspace, URL and optional folder
     * @return the queued job
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull RemoteDownloadJob create(@NotNull CreateRemoteDownloadParams params) {
        JsonObject resp = http.request(HttpRequest.post("/api/remote-downloads").body(params.toBody()));
        return http.fromJson(resp.get("job"), RemoteDownloadJob.class);
    }

    /**
     * Cancels an active job, or dismisses a finished one from the list. Only the member who
     * started the job may do this (403). Needs a {@code full} scope key. Not retried: a replay
     * after a cancel would dismiss the job.
     *
     * @param jobId       the job id ({@code rdl_...})
     * @param workspaceId the job's workspace
     * @return whether the job was cancelled or dismissed
     * @throws dev.dosya.sdk.exception.DosyaApiException if the API returns an error
     */
    public @NotNull CancelRemoteDownloadResult cancel(@NotNull String jobId, @NotNull String workspaceId) {
        return http.requestAs(
                HttpRequest.delete("/api/remote-downloads/" + seg(jobId))
                        .query("workspace_id", workspaceId)
                        .retry(HttpRequest.Retry.NEVER),
                CancelRemoteDownloadResult.class);
    }

    /**
     * Polls {@link #list(String)} every 2 seconds, with no time limit, until the job is
     * {@code done}. See {@link #waitFor(String, String, long, Long, Consumer)}.
     *
     * @return the finished job ({@code fileId} set)
     * @throws RemoteDownloadFailedException if the job ends in {@code error} or {@code cancelled}, or is no longer listed
     */
    public @NotNull RemoteDownloadJob waitFor(@NotNull String jobId, @NotNull String workspaceId) {
        return waitFor(jobId, workspaceId, DEFAULT_POLL_INTERVAL_MS, null, null);
    }

    /**
     * Polls {@link #list(String)} until the job is {@code done} and returns it ({@code fileId} set).
     * Jobs whose worker died stay {@code queued} or {@code downloading}, so set {@code timeoutMs}
     * for unattended code. Interrupting the thread stops the wait with a
     * {@link dev.dosya.sdk.exception.DosyaNetworkException}.
     *
     * @param jobId       the job id
     * @param workspaceId the job's workspace
     * @param intervalMs  poll interval in milliseconds
     * @param timeoutMs   give up after this many milliseconds, or {@code null} for no limit
     * @param onProgress  called after every poll with the latest job state, or {@code null}
     * @return the finished job
     * @throws RemoteDownloadFailedException if the job ends in {@code error} (see
     *         {@link RemoteDownloadJob#getErrorCode()}) or {@code cancelled}, is no longer listed,
     *         or {@code timeoutMs} passes; carries the last job state when known
     * @throws dev.dosya.sdk.exception.DosyaApiException if a poll fails
     */
    public @NotNull RemoteDownloadJob waitFor(@NotNull String jobId, @NotNull String workspaceId, long intervalMs,
                                              @Nullable Long timeoutMs,
                                              @Nullable Consumer<RemoteDownloadJob> onProgress) {
        long interval = Math.max(0, intervalMs);
        long start = System.nanoTime();
        for (;;) {
            RemoteDownloadJob job = null;
            for (RemoteDownloadJob j : list(workspaceId)) {
                if (jobId.equals(j.getId())) {
                    job = j;
                    break;
                }
            }
            if (job == null) {
                throw new RemoteDownloadFailedException("Remote download " + jobId + " was not found", null);
            }
            if (onProgress != null) onProgress.accept(job);
            switch (job.getStatus()) {
                case DONE:
                    return job;
                case ERROR:
                    throw new RemoteDownloadFailedException("Remote download " + jobId + " failed"
                            + (job.getErrorCode() != null ? ": " + job.getErrorCode() : ""), job);
                case CANCELLED:
                    throw new RemoteDownloadFailedException("Remote download " + jobId + " was cancelled", job);
                default:
                    break;
            }
            if (timeoutMs != null) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
                if (elapsedMs + interval > timeoutMs) {
                    throw new RemoteDownloadFailedException("Remote download " + jobId + " did not finish within "
                            + timeoutMs + "ms (last status: " + job.getRawStatus() + ")", job);
                }
            }
            http.sleep(interval);
        }
    }

    private static HttpRequest listRequest(String workspaceId) {
        return HttpRequest.get("/api/remote-downloads").query("workspace_id", workspaceId);
    }

    /**
     * Thrown by {@link #waitFor} when the job ends in {@code error} or {@code cancelled},
     * disappears from the list, or does not finish in time.
     *
     * @since 0.3.0
     */
    public static final class RemoteDownloadFailedException extends DosyaException {

        private final transient RemoteDownloadJob job;

        /**
         * @param message the detail message
         * @param job     the last known job state, or {@code null} when the job was no longer listed
         */
        public RemoteDownloadFailedException(@NotNull String message, @Nullable RemoteDownloadJob job) {
            super(message);
            this.job = job;
        }

        /** The last known job state; {@code null} when the job was no longer listed. */
        public @Nullable RemoteDownloadJob getJob() {
            return job;
        }
    }
}
