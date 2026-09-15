package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

/**
 * The bytes of one upload: an in-memory array, a file on disk, or an input stream
 * with a declared size.
 *
 * <p>Files are read part by part with positional reads and never loaded whole. A
 * stream is read once, forward only: it is buffered for a single-request upload (at
 * most 50 MiB) and read part by part for a multipart upload, so it cannot be retried
 * past what has already been consumed.
 *
 * @since 0.3.0
 */
public final class UploadSource {

    private final byte[] bytes;
    private final Path path;
    private final InputStream stream;
    private final long size;
    private long position;

    private UploadSource(byte[] bytes, Path path, InputStream stream, long size) {
        this.bytes = bytes;
        this.path = path;
        this.stream = stream;
        this.size = size;
    }

    /**
     * Bytes held in memory. The array is not copied; do not modify it while an upload runs.
     *
     * @param data the file content
     * @return the source
     */
    public static @NotNull UploadSource ofBytes(byte @NotNull [] data) {
        Objects.requireNonNull(data, "data");
        return new UploadSource(data, null, null, data.length);
    }

    /**
     * A file on disk. Only its size is read here; the content is read part by part
     * during the upload.
     *
     * @param path the file
     * @return the source
     * @throws IOException if the file's size cannot be read
     */
    public static @NotNull UploadSource ofPath(@NotNull Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        if (!Files.isRegularFile(path)) throw new IOException("Not a regular file: " + path);
        return new UploadSource(null, path, null, Files.size(path));
    }

    /**
     * A file on disk. Same as {@link #ofPath(Path)}.
     *
     * @param file the file
     * @return the source
     * @throws IOException if the file's size cannot be read
     */
    public static @NotNull UploadSource ofFile(@NotNull File file) throws IOException {
        Objects.requireNonNull(file, "file");
        return ofPath(file.toPath());
    }

    /**
     * A stream of exactly {@code size} bytes. The upload fails when the stream ends
     * early or holds more bytes than declared. The SDK does not close the stream.
     *
     * @param stream the content
     * @param size   the exact number of bytes the stream holds
     * @return the source
     */
    public static @NotNull UploadSource ofStream(@NotNull InputStream stream, long size) {
        Objects.requireNonNull(stream, "stream");
        if (size < 0) throw new IllegalArgumentException("size must be non-negative, got " + size);
        return new UploadSource(null, null, stream, size);
    }

    /** Returns the size in bytes (declared for a stream). */
    public long getSize() {
        return size;
    }

    /** Returns true for a stream source, which can only be read once, in order. */
    public boolean isStream() {
        return stream != null;
    }

    /** Returns true for an in-memory source. */
    public boolean isBytes() {
        return bytes != null;
    }

    /** Returns the file of a path source, or null. */
    public @Nullable Path getPath() {
        return path;
    }

    /**
     * Reads {@code length} bytes starting at {@code offset}. The result is shorter only
     * when the source ends first. A stream source must be read forward: skipped bytes
     * are discarded and an offset before the current position throws.
     *
     * @param offset first byte to read
     * @param length number of bytes to read
     * @return the bytes read
     * @throws IOException if reading fails
     * @throws IllegalStateException if a stream is asked for bytes it has already passed
     */
    public byte @NotNull [] read(long offset, int length) throws IOException {
        if (offset < 0 || length < 0) throw new IllegalArgumentException("offset and length must be non-negative");
        if (bytes != null) {
            if (offset >= bytes.length) return new byte[0];
            int end = (int) Math.min((long) bytes.length, offset + length);
            if (offset == 0 && end == bytes.length) return bytes;
            return Arrays.copyOfRange(bytes, (int) offset, end);
        }
        if (path != null) {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                ByteBuffer buf = ByteBuffer.allocate(length);
                while (buf.hasRemaining()) {
                    int n = channel.read(buf, offset + buf.position());
                    if (n < 0) break;
                }
                return buf.position() == length ? buf.array() : Arrays.copyOf(buf.array(), buf.position());
            }
        }
        synchronized (this) {
            if (offset < position) {
                throw new IllegalStateException("A stream source can only be read forward (at byte "
                        + position + ", asked for " + offset + ")");
            }
            while (position < offset) {
                long skipped = stream.readNBytes((int) Math.min(offset - position, 64 * 1024)).length;
                if (skipped == 0) return new byte[0];
                position += skipped;
            }
            byte[] out = stream.readNBytes(length);
            position += out.length;
            return out;
        }
    }

    /**
     * For a stream that has been read to its declared size, returns true when it still
     * holds more bytes. Consumes one byte when it does. Always false for other sources.
     *
     * @return whether the stream is longer than declared
     * @throws IOException if reading fails
     */
    public boolean exceedsDeclaredSize() throws IOException {
        if (stream == null) return false;
        synchronized (this) {
            if (position < size) return false;
            if (stream.read() == -1) return false;
            position++;
            return true;
        }
    }
}
