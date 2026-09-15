package dev.dosya.sdk;

import dev.dosya.sdk.exception.DosyaApiException;
import dev.dosya.sdk.exception.DosyaNetworkException;
import dev.dosya.sdk.exception.DosyaTimeoutException;
import dev.dosya.sdk.exception.DosyaUploadException;
import dev.dosya.sdk.model.*;
import dev.dosya.sdk.webhook.WebhookEvent;
import dev.dosya.sdk.webhook.WebhookEventType;
import dev.dosya.sdk.webhook.WebhookSignature;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Keeps the README honest: every snippet in it is copied from a method here, so a
 * signature change that breaks the documentation breaks the build. The methods are
 * compiled, never run (they would call the live API).
 */
class ReadmeExamplesCompileTest {

    @Test
    void examplesCompile() throws Exception {
        assertThat(ReadmeExamplesCompileTest.class.getDeclaredMethod("quickStart")).isNotNull();
    }

    @SuppressWarnings("unused")
    private static void quickStart() throws IOException {
        DosyaClient client = new DosyaClient(System.getenv("DOSYA_API_KEY"));

        String workspaceId = client.workspaces().list().getWorkspaces().get(0).getId();

        UploadResult uploaded = client.upload().file(
                UploadParams.fromPath(workspaceId, Paths.get("report.pdf"))
                        .onProgress(p -> System.out.println(p.getPercent() + "%")));

        ListFilesResponse listing = client.files().list(new ListFilesParams(workspaceId).sort("modified_desc"));

        DownloadLink link = client.download().getUrl(uploaded.getFile().getId(), new DownloadOptions().ttl(600));

        CreatedShareLink share = client.files().createShareLink(uploaded.getFile().getId(),
                new CreateShareLinkParams().expiresInDays(7));
        System.out.println(share.getUrl() + " " + link.getUrl() + " " + listing.getFiles().size());
    }

    @SuppressWarnings("unused")
    private static void configuration() {
        DosyaClient client = new DosyaClient(new DosyaClientOptions("dos_...")
                .baseUrl("https://api.dosya.dev")
                .connectTimeout(10_000)
                .readTimeout(30_000)
                .uploadTimeout(600_000)
                .maxRetries(3)
                .baseDelay(500)
                .maxDelay(30_000)
                .readYourWrites(true)
                .onRateLimit(info -> System.out.println(info.getRemaining() + "/" + info.getLimit()))
                .debug(System.out::println));
    }

    @SuppressWarnings("unused")
    private static void uploads(DosyaClient client, String workspaceId, String folderId, String sessionId,
                                List<Path> localFiles) throws IOException {
        client.upload().file(UploadParams.fromPath(workspaceId, Paths.get("clip.mp4"))
                .folderId(folderId)
                .sourceModifiedAt(java.nio.file.Files.getLastModifiedTime(Paths.get("clip.mp4")).toInstant())
                .computeSha256(true)
                .expectedVersion(3)
                .concurrency(4));

        client.upload().resume(sessionId, UploadSource.ofPath(Paths.get("clip.mp4")));

        List<UploadBatchFile> files = localFiles.stream()
                .map(p -> {
                    try {
                        return UploadBatchFile.of(p).folderId(folderId);
                    } catch (IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                })
                .collect(Collectors.toList());
        for (UploadManyResult r : client.upload().many(workspaceId, files,
                new UploadManyOptions().onProgress(p -> System.out.println(p.getFilesCompleted() + "/" + p.getTotalFiles())))) {
            if (!r.isOk()) System.err.println(r.getName() + ": " + r.getError());
        }
    }

    @SuppressWarnings("unused")
    private static void downloads(DosyaClient client, String fileId, String folderId) throws IOException {
        DownloadLink link = client.download().getUrl(fileId, new DownloadOptions().ttl(3600));
        byte[] bytes = client.download().downloadBytes(fileId);
        long written = client.download().downloadTo(fileId, Paths.get("out.bin"));
        try (InputStream part = client.download().downloadStream(fileId, new DownloadOptions().range(0, 1023))) {
            part.readAllBytes();
        }
        try (InputStream zip = client.download().archive(null, Arrays.asList(folderId)).body()) {
            zip.transferTo(java.io.OutputStream.nullOutputStream());
        }

        UnlockGrant grant = client.files().unlock(fileId, "secret");
        client.download().downloadBytes(fileId, new DownloadOptions().unlockToken(grant.getUnlockToken()));
    }

    @SuppressWarnings("unused")
    private static void sharing(DosyaClient client, String fileId, String folderId) {
        CreatedShareLink link = client.files().createShareLink(fileId, new CreateShareLinkParams()
                .expiresInDays(14)
                .password("at-least-8-chars")
                .maxDownloads(50));

        client.folders().createShareLink(folderId, new CreateShareLinkParams()
                .accessMode(ShareAccessMode.RESTRICTED)
                .recipientEmails(Arrays.asList("client@example.com")));

        ShareByEmailResult sent = client.files().shareByEmail(fileId,
                new ShareByEmailParams(Arrays.asList("a@example.com")));

        client.shares().update(link.getId(), new UpdateShareLinkParams().maxDownloads(100));
        client.shares().revoke(link.getId());
    }

    @SuppressWarnings("unused")
    private static void webhooks(DosyaClient client, String workspaceId, byte[] rawBody, String signatureHeader) {
        CreatedWebhookEndpoint endpoint = client.webhooks().create(new CreateWebhookParams(workspaceId,
                "https://example.com/hooks/dosya", WebhookEventType.FILE_UPLOADED, WebhookEventType.FILE_DELETED));
        String secret = endpoint.getSecret();

        WebhookEvent event = WebhookSignature.constructEvent(rawBody, signatureHeader, secret);
        if (event.getEventType() == WebhookEventType.FILE_UPLOADED) {
            System.out.println(event.getFileId());
        }
    }

    @SuppressWarnings("unused")
    private static void errors(DosyaClient client, String workspaceId, String folderId) {
        try {
            client.files().list(new ListFilesParams(workspaceId).folderId(folderId));
        } catch (DosyaApiException e) {
            e.getStatus();
            e.getErrorMessage();
            e.getCode();
            e.getDetails();
            e.getRetryAfterSeconds();
            e.getRequestId();
        } catch (DosyaTimeoutException e) {
            e.getTimeoutMs();
        } catch (DosyaNetworkException e) {
            e.getCause();
        } catch (DosyaUploadException e) {
            e.getSessionId();
            e.getPartNumber();
        }
    }

    @SuppressWarnings("unused")
    private static void gotchas(DosyaClient client, String workspaceId, String folderId, String fileId) {
        client.folders().purge(folderId);
        client.files().lock(fileId, LockMode.NONE);
        client.workspaces().requestDeletion(workspaceId);
        client.workspaces().delete(workspaceId, "123456", "My workspace");
        client.regions().list();
        client.remoteDownloads().waitFor(
                client.remoteDownloads().create(new CreateRemoteDownloadParams(workspaceId, "https://example.com/a.zip")).getId(),
                workspaceId);
    }
}
