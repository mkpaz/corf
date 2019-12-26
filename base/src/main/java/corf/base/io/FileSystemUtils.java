package corf.base.io;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public final class FileSystemUtils {

    private FileSystemUtils() { }

    /** Returns file parent path. */
    public static @Nullable Path getParentPath(@Nullable File file) {
        if (file == null) {
            return null;
        }

        File parent = file.getParentFile();
        return parent != null ? parent.toPath() : null;
    }

    /** Removes any special characters from the given file name. */
    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) { return ""; }
        return filename.replaceAll("[\\\\/:*?\"'<>|]", "_");
    }

    /** Generates and returns temp path, <b>does not create nor temp file nor directory.</b>. */
    public static Path getTempPath(@Nullable String prefix,
                                   @Nullable String suffix) {
        prefix = StringUtils.defaultString(prefix);
        suffix = StringUtils.defaultString(suffix);
        var filename = UUID.randomUUID().toString().replace("-", "");
        return Env.TEMP_DIR.resolve(prefix + filename + suffix);
    }

    /** See {@link #getTempPath(String, String)}. */
    public static Path getTempFilePath() {
        return getTempPath(null, ".tmp");
    }

    /** See {@link #getTempPath(String, String)}. */
    public static Path getTempDirPath() {
        return getTempPath("tmp-", null);
    }

    /** Creates temp file. */
    public static Path createTempFile(@Nullable String prefix,
                                      @Nullable String suffix) {
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** See {@link #createTempFile(String, String)}. */
    public static Path createTempFile() {
        return createTempFile(null, null);
    }

    /** Creates temp directory. */
    public static Path createTempDir(@Nullable String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** See {@link #createTempDir(String)}. */
    public static Path createTempDir() {
        return createTempDir(null);
    }

    /** Creates new directory (non-recursively). */
    public static void createDir(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            if (!exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Creates new directory (recursively). */
    public static void createDirTree(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            if (!exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Copies file from the source to the destination. */
    public static void copyFile(Path source, Path dest, StandardCopyOption option) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(dest, "dest");

        try {
            Files.copy(source, dest, option);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Copies given directory with all content to the destination. */
    public static void copyDir(Path source, Path dest, boolean overwrite) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(dest, "dest");

        try (var stream = Files.walk(source)) {
            stream.forEach(entry -> {
                Path target = dest.resolve(source.relativize(entry));
                if (!Files.exists(target) || overwrite) {
                    copyFile(entry, target, StandardCopyOption.REPLACE_EXISTING);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Removes file. */
    public static void deleteFile(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            if (fileExists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Removes directory. */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDir(Path path) {
        Objects.requireNonNull(path, "path");
        if (!dirExists(path)) { return; }

        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Checks whether directory empty or not. */
    public static boolean isEmptyDir(Path dir) {
        Objects.requireNonNull(dir, "dir");
        if (!dirExists(dir)) { return true; }

        try (DirectoryStream<Path> folderStream = Files.newDirectoryStream(dir)) {
            return !folderStream.iterator().hasNext();
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Checks that file-system resource (ether file or directory) exists. */
    public static boolean exists(@Nullable Path path) {
        return path != null && Files.exists(path);
    }

    /** Checks that given path leads to the existing directory. */
    public static boolean dirExists(@Nullable Path path) {
        return exists(path) && Files.isDirectory(path);
    }

    /** Checks that given path leads to the existing regular file. */
    public static boolean fileExists(@Nullable Path path) {
        return exists(path) && Files.isRegularFile(path);
    }

    /**
     * Creates a copy of a file in the temp directory and returns path
     * to the created temp file or null if copying was failed.
     */
    public static @Nullable Path backupFile(Path source) {
        Objects.requireNonNull(source, "source");
        if (!exists(source)) { return null; }

        Path tmp = getTempFilePath();
        try {
            copyFile(source, tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }
}
