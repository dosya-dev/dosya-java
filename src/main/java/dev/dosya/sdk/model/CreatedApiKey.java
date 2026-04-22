package dev.dosya.sdk.model;

public class CreatedApiKey {

    private String id;
    private String name;
    private String scope;
    private String plainKey;
    private Long expiresAt;
    private long createdAt;

    private CreatedApiKey() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getScope() { return scope; }
    public String getPlainKey() { return plainKey; }
    public Long getExpiresAt() { return expiresAt; }
    public long getCreatedAt() { return createdAt; }
}
