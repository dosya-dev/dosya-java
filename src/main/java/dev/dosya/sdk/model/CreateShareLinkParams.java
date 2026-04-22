package dev.dosya.sdk.model;

public class CreateShareLinkParams {

    private Integer expiresInDays;
    private String password;
    private String lockMode;

    public CreateShareLinkParams() {}

    public CreateShareLinkParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    public CreateShareLinkParams password(String password) { this.password = password; return this; }
    public CreateShareLinkParams lockMode(String lockMode) { this.lockMode = lockMode; return this; }

    public Integer getExpiresInDays() { return expiresInDays; }
    public String getPassword() { return password; }
    public String getLockMode() { return lockMode; }
}
