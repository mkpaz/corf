package telekit.base.util;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static telekit.base.Env.TEMP_DIR;

public final class FileSystemUtils {

    /** Removes any special characters from file name */
    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) { return ""; }
        return filename.replaceAll("[\\\\/:*?\"'<>|]", "_");
    }

    public static @Nullable Path getParentPath(File file) {
        if (file == null) { return null; }
        File parent = file.getParentFile();
        return parent != null ? parent.toPath() : null;
    }

    public static Path getTempFilePath() {
        return getTempPath(null, ".tmp");
    }

    public static Path getTempDirPath() {
        return getTempPath("tmp-", null);
    }

    /** Only returns generated temp path, does not create nor temp file nor directory */
    public static Path getTempPath(String prefix, String suffix) {
        String filename = UUID.randomUUID().toString().replace("-", "");
        prefix = defaultString(prefix);
        suffix = defaultString(suffix);
        return TEMP_DIR.resolve(prefix + filename + suffix);
    }

    public static Path createTempFile() {
        return createTempFile(null, null);
    }

    public static Path createTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path createTempDir() {
        return createTempDir(null);
    }

    public static Path createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDir(Path path) {
        try {
            if (!exists(path)) { Files.createDirectory(path); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createDirTree(Path path) {
        try {
            if (!exists(path)) { Files.createDirectories(path); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(Path source, Path dest, StandardCopyOption option) {
        try {
            Files.copy(Objects.requireNonNull(source), Objects.requireNonNull(dest), option);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyDir(Path source, Path dest, boolean overwrite) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(dest);
        try {
            Files.walk(source).forEach(entry -> {
                Path target = dest.resolve(source.relativize(entry));
                if (!Files.exists(target) | overwrite) {
                    copyFile(entry, target, REPLACE_EXISTING);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFile(Path path) {
        if (!fileExists(path)) { return; }
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDir(Path path) {
        if (!dirExists(path)) { return; }
        try {
            //noinspection ResultOfMethodCallIgnored
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmptyDir(Path dir) {
        if (!dirExists(dir)) { return true; }
        try (DirectoryStream<Path> folderStream = Files.newDirectoryStream(dir)) {
            return !folderStream.iterator().hasNext();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean exists(Path path) {
        return path != null && Files.exists(path);
    }

    public static boolean dirExists(Path path) {
        return exists(path) && Files.isDirectory(path);
    }

    public static boolean fileExists(Path path) {
        return exists(path) && Files.isRegularFile(path);
    }

    /**
     * Creates a copy of a file in the temp directory and returns path to the
     * created temp file or null if copying has failed.
     */
    public static @Nullable Path backupFile(Path source) {
        if (!exists(source)) { return null; }

        Path tmp = getTempFilePath();
        try {
            copyFile(source, tmp, REPLACE_EXISTING);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }
}
