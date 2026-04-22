package dev.dosya.sdk;

import dev.dosya.sdk.model.RateLimitInfo;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DosyaClientOptionsTest {

    // ---- API key validation ----

    @Test
    void constructorRejectsNullApiKey() {
        assertThatThrownBy(() -> new DosyaClientOptions(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key is required");
    }

    @Test
    void constructorRejectsEmptyApiKey() {
        assertThatThrownBy(() -> new DosyaClientOptions(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key is required");
    }

    @Test
    void constructorAcceptsValidApiKey() {
        DosyaClientOptions options = new DosyaClientOptions("dos_abc123");
        assertThat(options.getApiKey()).isEqualTo("dos_abc123");
    }

    // ---- Default values ----

    @Test
    void defaultBaseUrl() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getBaseUrl()).isEqualTo("https://dosya.dev");
    }

    @Test
    void defaultConnectTimeout() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getConnectTimeout()).isEqualTo(10000);
    }

    @Test
    void defaultReadTimeout() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getReadTimeout()).isEqualTo(30000);
    }

    @Test
    void defaultMaxRetries() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getMaxRetries()).isEqualTo(3);
    }

    @Test
    void defaultBaseDelay() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getBaseDelay()).isEqualTo(500);
    }

    @Test
    void defaultMaxDelay() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getMaxDelay()).isEqualTo(30000);
    }

    @Test
    void defaultOnRateLimitIsNull() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getOnRateLimit()).isNull();
    }

    @Test
    void defaultDebugIsNull() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getDebug()).isNull();
    }

    @Test
    void defaultInterceptorIsNull() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.getInterceptor()).isNull();
    }

    // ---- Fluent setters return this ----

    @Test
    void baseUrlReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.baseUrl("https://custom.dosya.dev")).isSameAs(options);
    }

    @Test
    void maxRetriesReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.maxRetries(5)).isSameAs(options);
    }

    @Test
    void baseDelayReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.baseDelay(1000)).isSameAs(options);
    }

    @Test
    void maxDelayReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.maxDelay(60000)).isSameAs(options);
    }

    @Test
    void connectTimeoutReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.connectTimeout(5000)).isSameAs(options);
    }

    @Test
    void readTimeoutReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.readTimeout(15000)).isSameAs(options);
    }

    @Test
    void timeoutReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.timeout(20000)).isSameAs(options);
    }

    @Test
    void onRateLimitReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.onRateLimit(info -> {})).isSameAs(options);
    }

    @Test
    void debugReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThat(options.debug(msg -> {})).isSameAs(options);
    }

    @Test
    void interceptorReturnsSameInstance() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        DosyaInterceptor noop = new DosyaInterceptor() {
            @Override
            public void beforeRequest(String method, String url) {}
            @Override
            public void afterResponse(String method, String url, int statusCode, String requestId, long durationMs) {}
        };
        assertThat(options.interceptor(noop)).isSameAs(options);
    }

    // ---- Setter functionality ----

    @Test
    void baseUrlSetsValue() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key")
                .baseUrl("https://api.dosya.dev");
        assertThat(options.getBaseUrl()).isEqualTo("https://api.dosya.dev");
    }

    @Test
    void baseUrlRejectsHttp() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThatThrownBy(() -> options.baseUrl("http://insecure.dosya.dev"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Base URL must use HTTPS");
    }

    @Test
    void baseUrlRejectsFtp() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key");
        assertThatThrownBy(() -> options.baseUrl("ftp://dosya.dev"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Base URL must use HTTPS");
    }

    @Test
    void baseUrlAcceptsHttpsCaseInsensitive() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key")
                .baseUrl("HTTPS://dosya.dev");
        assertThat(options.getBaseUrl()).isEqualTo("HTTPS://dosya.dev");
    }

    @Test
    void maxRetriesSetsValue() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key").maxRetries(7);
        assertThat(options.getMaxRetries()).isEqualTo(7);
    }

    @Test
    void baseDelaySetsValue() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key").baseDelay(250);
        assertThat(options.getBaseDelay()).isEqualTo(250);
    }

    @Test
    void maxDelaySetsValue() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key").maxDelay(60000);
        assertThat(options.getMaxDelay()).isEqualTo(60000);
    }

    @Test
    void connectTimeoutSetsValue() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key").connectTimeout(5000);
        assertThat(options.getConnectTimeout()).isEqualTo(5000);
    }

    @Test
    void readTimeoutSetsValue() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key").readTimeout(15000);
        assertThat(options.getReadTimeout()).isEqualTo(15000);
    }

    @Test
    void timeoutSetsBothConnectAndReadTimeout() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key").timeout(20000);
        assertThat(options.getConnectTimeout()).isEqualTo(20000);
        assertThat(options.getReadTimeout()).isEqualTo(20000);
    }

    @Test
    void onRateLimitSetsCallback() {
        Consumer<RateLimitInfo> callback = info -> {};
        DosyaClientOptions options = new DosyaClientOptions("dos_key").onRateLimit(callback);
        assertThat(options.getOnRateLimit()).isSameAs(callback);
    }

    @Test
    void debugSetsCallback() {
        Consumer<String> callback = msg -> {};
        DosyaClientOptions options = new DosyaClientOptions("dos_key").debug(callback);
        assertThat(options.getDebug()).isSameAs(callback);
    }

    @Test
    void interceptorSetsValue() {
        DosyaInterceptor interceptor = new DosyaInterceptor() {
            @Override
            public void beforeRequest(String method, String url) {}
            @Override
            public void afterResponse(String method, String url, int statusCode, String requestId, long durationMs) {}
        };
        DosyaClientOptions options = new DosyaClientOptions("dos_key").interceptor(interceptor);
        assertThat(options.getInterceptor()).isSameAs(interceptor);
    }

    // ---- Fluent chaining ----

    @Test
    void fluentChainingWorks() {
        DosyaClientOptions options = new DosyaClientOptions("dos_key")
                .baseUrl("https://api.dosya.dev")
                .maxRetries(5)
                .baseDelay(250)
                .maxDelay(60000)
                .connectTimeout(5000)
                .readTimeout(15000)
                .debug(msg -> {});

        assertThat(options.getBaseUrl()).isEqualTo("https://api.dosya.dev");
        assertThat(options.getMaxRetries()).isEqualTo(5);
        assertThat(options.getBaseDelay()).isEqualTo(250);
        assertThat(options.getMaxDelay()).isEqualTo(60000);
        assertThat(options.getConnectTimeout()).isEqualTo(5000);
        assertThat(options.getReadTimeout()).isEqualTo(15000);
        assertThat(options.getDebug()).isNotNull();
    }

    // ---- toString ----

    @Test
    void toStringMasksApiKey() {
        DosyaClientOptions options = new DosyaClientOptions("dos_secret_key_abc");
        String str = options.toString();
        assertThat(str).contains("apiKey=***");
        assertThat(str).doesNotContain("dos_secret_key_abc");
        assertThat(str).contains("DosyaClientOptions");
        assertThat(str).contains("baseUrl=");
    }
}
