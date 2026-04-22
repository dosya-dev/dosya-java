package dev.dosya.sdk.model;

public class FolderTreeItem {

    private String id;
    private String name;
    private String parentId;
    private int fileCount;

    private FolderTreeItem() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
    public int getFileCount() { return fileCount; }
}
