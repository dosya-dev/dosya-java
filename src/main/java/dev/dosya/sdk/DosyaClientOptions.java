package dev.dosya.sdk;

import dev.dosya.sdk.model.RateLimitInfo;

import java.util.function.Consumer;

public class DosyaClientOptions {

    private final String apiKey;
    private String baseUrl = "https://dosya.dev";
    private int maxRetries = 3;
    private long baseDelay = 500;
    private long maxDelay = 30000;
    private long timeout = 30000;
    private Consumer<RateLimitInfo> onRateLimit;
    private Consumer<String> debug;

    public DosyaClientOptions(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        this.apiKey = apiKey;
    }

    public DosyaClientOptions baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public DosyaClientOptions maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public DosyaClientOptions baseDelay(long baseDelay) {
        this.baseDelay = baseDelay;
        return this;
    }

    public DosyaClientOptions maxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
        return this;
    }

    public DosyaClientOptions timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public DosyaClientOptions onRateLimit(Consumer<RateLimitInfo> onRateLimit) {
        this.onRateLimit = onRateLimit;
        return this;
    }

    public DosyaClientOptions debug(Consumer<String> debug) {
        this.debug = debug;
        return this;
    }

    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public int getMaxRetries() { return maxRetries; }
    public long getBaseDelay() { return baseDelay; }
    public long getMaxDelay() { return maxDelay; }
    public long getTimeout() { return timeout; }
    public Consumer<RateLimitInfo> getOnRateLimit() { return onRateLimit; }
    public Consumer<String> getDebug() { return debug; }
}
