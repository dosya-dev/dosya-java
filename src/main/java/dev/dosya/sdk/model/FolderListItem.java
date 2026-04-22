package dev.dosya.sdk.model;

public class FolderListItem {

    private String id;
    private String name;
    private long createdAt;
    private int fileCount;
    private String lockMode;
    private int isHidden;
    private String hiddenMode;
    private int isSynced;

    private FolderListItem() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public long getCreatedAt() { return createdAt; }
    public int getFileCount() { return fileCount; }
    public String getLockMode() { return lockMode; }
    public int getIsHidden() { return isHidden; }
    public String getHiddenMode() { return hiddenMode; }
    public int getIsSynced() { return isSynced; }
}
