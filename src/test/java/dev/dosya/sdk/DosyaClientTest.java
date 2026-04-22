package dev.dosya.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DosyaClientTest {

    @Test
    void constructorRejectsNullOptions() {
        assertThatThrownBy(() -> new DosyaClient((DosyaClientOptions) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("options must not be null");
    }

    @Test
    void constructorRejectsApiKeyWithoutDosPrefix() {
        DosyaClientOptions options = new DosyaClientOptions("invalid_key_123");
        assertThatThrownBy(() -> new DosyaClient(options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key must start with 'dos_'");
    }

    @Test
    void constructorAcceptsValidApiKey() {
        DosyaClient client = new DosyaClient(new DosyaClientOptions("dos_test_key_123"));
        assertThat(client).isNotNull();
    }

    @Test
    void convenienceConstructorWithStringApiKey() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client).isNotNull();
    }

    @Test
    void convenienceConstructorRejectsInvalidKey() {
        assertThatThrownBy(() -> new DosyaClient("bad_key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key must start with 'dos_'");
    }

    @Test
    void filesAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.files()).isNotNull();
    }

    @Test
    void foldersAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.folders()).isNotNull();
    }

    @Test
    void uploadAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.upload()).isNotNull();
    }

    @Test
    void downloadAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.download()).isNotNull();
    }

    @Test
    void sharesAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.shares()).isNotNull();
    }

    @Test
    void workspacesAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.workspaces()).isNotNull();
    }

    @Test
    void fileRequestsAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.fileRequests()).isNotNull();
    }

    @Test
    void searchAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.search()).isNotNull();
    }

    @Test
    void commentsAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.comments()).isNotNull();
    }

    @Test
    void meAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.me()).isNotNull();
    }

    @Test
    void activityAccessorReturnsNonNull() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.activity()).isNotNull();
    }

    @Test
    void allAccessorsReturnStableInstances() {
        DosyaClient client = new DosyaClient("dos_test_key_123");
        assertThat(client.files()).isSameAs(client.files());
        assertThat(client.folders()).isSameAs(client.folders());
        assertThat(client.upload()).isSameAs(client.upload());
        assertThat(client.download()).isSameAs(client.download());
        assertThat(client.shares()).isSameAs(client.shares());
        assertThat(client.workspaces()).isSameAs(client.workspaces());
        assertThat(client.fileRequests()).isSameAs(client.fileRequests());
        assertThat(client.search()).isSameAs(client.search());
        assertThat(client.comments()).isSameAs(client.comments());
        assertThat(client.me()).isSameAs(client.me());
        assertThat(client.activity()).isSameAs(client.activity());
    }
}
