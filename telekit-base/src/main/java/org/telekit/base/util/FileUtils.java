package org.telekit.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FileUtils {

    public static final String EOL_SPLIT_PATTERN = "\\r?\\n";
    public static final Character BOM = '\ufeff';
    public static final Map<String, String> LINE_SEPARATOR = Map.of(
            "UNIX", "\n",
            "WINDOWS", "\r\n",
            "OSX", "\r"
    );

    public static File createFromURL(URL url) {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFolder(Path targetDirectory) {
        try {
            Files.walk(targetDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
