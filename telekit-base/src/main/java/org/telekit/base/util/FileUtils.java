package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FileUtils {

    public static @NotNull File urlToFile(URL url) {
        try {
            return new File(Objects.requireNonNull(url).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) return "";
        return filename.replaceAll("[\\\\/:*?\"'<>|]", "_");
    }

    public static @NotNull Path ensureNotNull(String path, Path defaultValue) {
        return path != null ? Paths.get(path) : Objects.requireNonNull(defaultValue);
    }

    public static @NotNull List<Path> findFilesByPrefix(Path folder, String prefix) {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) return Collections.emptyList();
        return Arrays.stream(folder.toFile().listFiles((dir, name) -> name.startsWith(prefix)))
                .filter(File::isFile)
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    public static @NotNull Path createTempFile() {
        return createTempFile(null, null);
    }

    public static @NotNull Path createTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull Path createTempDir() {
        return createTempDir(null);
    }

    public static @NotNull Path createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(Path source, Path dest, StandardCopyOption option) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(dest);

        try {
            Files.copy(source, dest, option);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFolder(Path source, Path dest, boolean overwrite) {
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

    public static void deleteFile(Path file) {
        if (file == null) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFolder(Path folder) {
        try {
            if (folder == null || !Files.exists(folder)) return;
            //noinspection ResultOfMethodCallIgnored
            Files.walk(folder)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isFolderEmpty(Path folder) {
        if (folder == null) return true;
        try (DirectoryStream<Path> folderStream = Files.newDirectoryStream(folder)) {
            return !folderStream.iterator().hasNext();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
