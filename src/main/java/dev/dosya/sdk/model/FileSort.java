package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

/**
 * Sort values accepted by {@link ListFilesParams#sort(String)}.
 *
 * <p>Three forms: a legacy mode ({@link #NEWEST}, {@link #OLDEST}, {@link #LARGEST},
 * {@link #SMALLEST}), a column with a direction suffix ({@link #asc(String)} /
 * {@link #desc(String)}, e.g. {@code name_asc}), or a bare column whose direction comes
 * from {@link ListFilesParams#dir(String)}. The server falls back to {@code newest}
 * for unknown values rather than refusing them.
 *
 * @since 0.3.0
 */
public final class FileSort {

    public static final String NEWEST = "newest";
    public static final String OLDEST = "oldest";
    public static final String LARGEST = "largest";
    public static final String SMALLEST = "smallest";

    // Columns.
    public static final String NAME = "name";
    public static final String SIZE = "size";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";
    /** EXIF capture date, else source creation date, else upload time. */
    public static final String TAKEN = "taken";
    public static final String TYPE = "type";
    public static final String EXTENSION = "extension";
    public static final String VERSION = "version";
    public static final String UPLOADER = "uploader";
    public static final String REGION = "region";
    public static final String ORIGIN = "origin";
    public static final String SHARES = "shares";
    public static final String COMMENTS = "comments";

    private FileSort() {}

    /** {@code <column>_asc}. */
    public static @NotNull String asc(@NotNull String column) { return column + "_asc"; }

    /** {@code <column>_desc}. */
    public static @NotNull String desc(@NotNull String column) { return column + "_desc"; }
}
