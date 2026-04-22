package dev.dosya.sdk;

import dev.dosya.sdk.model.RateLimitInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Configuration options for {@link DosyaClient}.
 *
 * <p>Use the fluent builder methods to customize behaviour:
 * <pre>{@code
 * DosyaClientOptions options = new DosyaClientOptions("dos_your_key")
 *     .connectTimeout(5000)
 *     .readTimeout(30000)
 *     .maxRetries(5);
 * }</pre>
 *
 * <p>Thread-safe after construction. Do not mutate after passing to {@link DosyaClient}.
 *
 * @since 0.1.0
 */
public final class DosyaClientOptions {

    private final String apiKey;
    private String baseUrl = "https://dosya.dev";
    private int maxRetries = 3;
    private long baseDelay = 500;
    private long maxDelay = 30000;
    private long connectTimeout = 10000;
    private long readTimeout = 30000;
    private Consumer<RateLimitInfo> onRateLimit;
    private Consumer<String> debug;
    private DosyaInterceptor interceptor;

    /**
     * Creates options with the given API key.
     *
     * @param apiKey the Dosya API key (must start with {@code dos_})
     * @throws IllegalArgumentException if the API key is null or empty
     */
    public DosyaClientOptions(@NotNull String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        this.apiKey = apiKey;
    }

    /**
     * Sets the base URL for the Dosya API. Must use HTTPS.
     *
     * @param baseUrl the base URL (e.g. {@code https://dosya.dev})
     * @return this options instance for chaining
     * @throws IllegalArgumentException if the URL does not use HTTPS
     */
    public DosyaClientOptions baseUrl(@NotNull String baseUrl) {
        if (baseUrl != null && !baseUrl.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("Base URL must use HTTPS: " + baseUrl);
        }
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Sets the maximum number of retry attempts for failed requests.
     *
     * @param maxRetries the maximum number of retries (default 3)
     * @return this options instance for chaining
     */
    public DosyaClientOptions maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Sets the initial delay for exponential backoff in milliseconds.
     *
     * @param baseDelay the base delay in ms (default 500)
     * @return this options instance for chaining
     */
    public DosyaClientOptions baseDelay(long baseDelay) {
        this.baseDelay = baseDelay;
        return this;
    }

    /**
     * Sets the maximum delay between retries in milliseconds.
     *
     * @param maxDelay the maximum delay in ms (default 30000)
     * @return this options instance for chaining
     */
    public DosyaClientOptions maxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
        return this;
    }

    /**
     * Convenience method that sets both connect and read timeouts to the same value.
     *
     * @param timeout the timeout in ms for both connect and read
     * @return this options instance for chaining
     */
    public DosyaClientOptions timeout(long timeout) {
        this.connectTimeout = timeout;
        this.readTimeout = timeout;
        return this;
    }

    /**
     * Sets the TCP connection timeout in milliseconds.
     *
     * @param connectTimeout the connect timeout in ms (default 10000)
     * @return this options instance for chaining
     */
    public DosyaClientOptions connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the read/response timeout in milliseconds.
     *
     * @param readTimeout the read timeout in ms (default 30000)
     * @return this options instance for chaining
     */
    public DosyaClientOptions readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Registers a callback invoked when rate-limit response headers are received.
     *
     * @param onRateLimit the callback, or null to disable
     * @return this options instance for chaining
     */
    public DosyaClientOptions onRateLimit(@Nullable Consumer<RateLimitInfo> onRateLimit) {
        this.onRateLimit = onRateLimit;
        return this;
    }

    /**
     * Registers a debug callback that receives human-readable log messages.
     *
     * @param debug the callback, or null to disable
     * @return this options instance for chaining
     */
    public DosyaClientOptions debug(@Nullable Consumer<String> debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Registers an interceptor for observing HTTP requests and responses.
     *
     * @param interceptor the interceptor, or null to disable
     * @return this options instance for chaining
     */
    public DosyaClientOptions interceptor(@Nullable DosyaInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    /** Returns the API key. */
    @NotNull public String getApiKey() { return apiKey; }
    /** Returns the base URL. */
    @NotNull public String getBaseUrl() { return baseUrl; }
    /** Returns the maximum retry count. */
    public int getMaxRetries() { return maxRetries; }
    /** Returns the base delay for exponential backoff in milliseconds. */
    public long getBaseDelay() { return baseDelay; }
    /** Returns the maximum retry delay in milliseconds. */
    public long getMaxDelay() { return maxDelay; }
    /** Returns the TCP connection timeout in milliseconds. */
    public long getConnectTimeout() { return connectTimeout; }
    /** Returns the read/response timeout in milliseconds. */
    public long getReadTimeout() { return readTimeout; }
    /** Returns the rate-limit callback, or null. */
    @Nullable public Consumer<RateLimitInfo> getOnRateLimit() { return onRateLimit; }
    /** Returns the debug callback, or null. */
    @Nullable public Consumer<String> getDebug() { return debug; }
    /** Returns the interceptor, or null. */
    @Nullable public DosyaInterceptor getInterceptor() { return interceptor; }

    @Override
    public String toString() {
        return "DosyaClientOptions{baseUrl='" + baseUrl + "', apiKey=***}";
    }
}
