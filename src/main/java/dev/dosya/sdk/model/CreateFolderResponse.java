package dev.dosya.sdk.model;

import java.util.List;

public class CreateFolderResponse {

    private FolderDetail folder;
    private int createdCount;
    private List<FolderDetail> createdFolders;

    private CreateFolderResponse() {}

    public FolderDetail getFolder() { return folder; }
    public int getCreatedCount() { return createdCount; }
    public List<FolderDetail> getCreatedFolders() { return createdFolders; }
}
