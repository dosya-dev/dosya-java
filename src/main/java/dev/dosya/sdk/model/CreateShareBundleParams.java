package dev.dosya.sdk.model;

import java.util.List;

public class CreateShareBundleParams {

    private final List<String> fileIds;
    private Integer expiresInDays;
    private String password;

    public CreateShareBundleParams(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    public CreateShareBundleParams expiresInDays(int expiresInDays) { this.expiresInDays = expiresInDays; return this; }
    public CreateShareBundleParams password(String password) { this.password = password; return this; }

    public List<String> getFileIds() { return fileIds; }
    public Integer getExpiresInDays() { return expiresInDays; }
    public String getPassword() { return password; }
}
