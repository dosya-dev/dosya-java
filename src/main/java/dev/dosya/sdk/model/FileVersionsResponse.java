package dev.dosya.sdk.model;

import java.util.List;

public class FileVersionsResponse {

    private String fileName;
    private int currentVersion;
    private List<FileVersion> versions;

    private FileVersionsResponse() {}

    public String getFileName() { return fileName; }
    public int getCurrentVersion() { return currentVersion; }
    public List<FileVersion> getVersions() { return versions; }
}
