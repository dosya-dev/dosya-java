package dev.dosya.sdk.model;

public class Pagination {

    private int page;
    private int perPage;
    private int totalFiles;
    private int totalPages;

    private Pagination() {}

    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    public int getTotalFiles() { return totalFiles; }
    public int getTotalPages() { return totalPages; }
}
