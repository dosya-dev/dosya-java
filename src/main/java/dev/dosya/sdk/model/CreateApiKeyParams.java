package dev.dosya.sdk.model;

public class CreateApiKeyParams {

    private final String name;
    private String scope;
    private Integer expiresInDays;

    public CreateApiKeyParams(String name) {
        this.name = name;
    }

    public CreateApiKeyParams scope(String scope) { this.scope = scope; return this; }
    public CreateApiKeyParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }

    public String getName() { return name; }
    public String getScope() { return scope; }
    public Integer getExpiresInDays() { return expiresInDays; }
}
