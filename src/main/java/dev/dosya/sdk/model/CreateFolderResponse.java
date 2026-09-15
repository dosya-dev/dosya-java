package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Result of creating a folder or folder path (find-or-create).
 *
 * @since 0.1.0
 */
public final class CreateFolderResponse {

    private CreatedFolder folder;
    private int createdCount;
    private List<CreatedFolderEntry> createdFolders;

    private CreateFolderResponse() {}

    /**
     * The leaf folder the call resolved to, created now or already there.
     *
     * @since 0.3.0 (was {@code FolderDetail})
     */
    public @NotNull CreatedFolder getFolder() { return folder; }

    /** Folders actually inserted by this call; {@code 0} when every segment already existed. */
    public int getCreatedCount() { return createdCount; }

    /**
     * The inserted folders, outermost first.
     *
     * @since 0.3.0 (was {@code List<FolderDetail>})
     */
    public @NotNull List<CreatedFolderEntry> getCreatedFolders() {
        return createdFolders != null ? Collections.unmodifiableList(createdFolders) : Collections.emptyList();
    }

    /**
     * The leaf folder of a create call.
     *
     * @since 0.3.0
     */
    public static final class CreatedFolder {
        private String id;
        private String name;
        private String parentId;
        private String workspaceId;

        private CreatedFolder() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        public @Nullable String getParentId() { return parentId; }
        public @NotNull String getWorkspaceId() { return workspaceId; }
    }

    /**
     * One folder inserted by a create call.
     *
     * @since 0.3.0
     */
    public static final class CreatedFolderEntry {
        private String id;
        private String name;
        private String parentId;

        private CreatedFolderEntry() {}

        public @NotNull String getId() { return id; }
        public @NotNull String getName() { return name; }
        public @Nullable String getParentId() { return parentId; }
    }
}
