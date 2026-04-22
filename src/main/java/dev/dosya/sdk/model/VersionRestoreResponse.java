package dev.dosya.sdk.model;

/**
 * Response returned after restoring a file to a previous version.
 *
 * @since 0.1.0
 */
public final class VersionRestoreResponse {

    private int version;
    private int restoredFrom;

    private VersionRestoreResponse() {}

    public int getVersion() { return version; }
    public int getRestoredFrom() { return restoredFrom; }
}
