package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Response returned when creating a new folder.
 *
 * <p>Contains the created folder detail, the number of folders created,
 * and the full list of created folders (for nested path creation).</p>
 *
 * @since 0.1.0
 */
public final class CreateFolderResponse {

    private FolderDetail folder;
    private int createdCount;
    private List<FolderDetail> createdFolders;

    private CreateFolderResponse() {}

    public @NotNull FolderDetail getFolder() { return folder; }
    public int getCreatedCount() { return createdCount; }
    public @NotNull List<FolderDetail> getCreatedFolders() { return createdFolders != null ? Collections.unmodifiableList(createdFolders) : Collections.<FolderDetail>emptyList(); }
}
