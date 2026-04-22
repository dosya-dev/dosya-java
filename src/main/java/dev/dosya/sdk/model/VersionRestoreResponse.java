package dev.dosya.sdk.model;

public class VersionRestoreResponse {

    private int version;
    private int restoredFrom;

    private VersionRestoreResponse() {}

    public int getVersion() { return version; }
    public int getRestoredFrom() { return restoredFrom; }
}
