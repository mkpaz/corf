package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FileUtils {

    public static File urlToFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String sanitizeFileName(String filename) {
        return filename.replaceAll("[\\\\/:*?\"'<>|]", "_");
    }

    public static Properties loadProperties(File file, Charset charset) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static @NotNull List<Path> findFilesByPrefix(Path targetDir, String prefix) {
        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) return Collections.emptyList();
        return Arrays.stream(targetDir.toFile().listFiles((dir, name) -> name.startsWith(prefix)))
                .filter(File::isFile)
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    public static void copyFile(Path source, Path destination, StandardCopyOption option) {
        try {
            Files.copy(source, destination, option);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void copyFolder(Path source, Path destination, boolean overwrite) {
        try {
            Files.walk(source).forEach(entry -> {
                Path target = destination.resolve(source.relativize(entry));
                if (!Files.exists(target) | overwrite) {
                    copyFile(entry, target, REPLACE_EXISTING);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void deleteFile(Path targetFile) {
        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void deleteFolder(Path targetDirectory) {
        try {
            if (!Files.exists(targetDirectory)) return;

            Files.walk(targetDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean isDirEmpty(Path directory) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
