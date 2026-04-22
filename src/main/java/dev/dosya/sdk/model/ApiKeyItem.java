package dev.dosya.sdk.model;

public class ApiKeyItem {

    private String id;
    private String name;
    private String scope;
    private String keyPrefix;
    private Long lastUsedAt;
    private Long expiresAt;
    private long createdAt;

    private ApiKeyItem() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getScope() { return scope; }
    public String getKeyPrefix() { return keyPrefix; }
    public Long getLastUsedAt() { return lastUsedAt; }
    public Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }
}
