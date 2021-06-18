package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

import static org.telekit.base.util.StringUtils.ensureNotNull;

public final class CommonUtils {

    public static <T, R> R extract(T t, Function<T, R> func) {
        return t != null ? func.apply(t) : null;
    }

    public static @NotNull Properties loadProperties(File file) {
        return loadProperties(file, StandardCharsets.UTF_8);
    }

    public static @NotNull Properties loadProperties(File file, Charset charset) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull String className(Class<?> cls) {
        return Objects.requireNonNull(cls).getCanonicalName();
    }

    public static @NotNull String objectClassName(Object object) {
        return Objects.requireNonNull(object).getClass().getCanonicalName();
    }

    public static @NotNull String localizedFileName(String fileName, String fileExtension) {
        return localizedFileName(fileName, fileExtension, Locale.getDefault());
    }

    /**
     * Returns file name that corresponds most common practice to name i18n resources,
     * region tag is not included. Example: {@code config.txt > config_en.txt}.
     * If locale is null, then just plain file name returned.
     */
    public static @NotNull String localizedFileName(String fileName, String fileExtension, Locale locale) {
        if (locale == null) { return fileName + fileExtension; }
        return ensureNotNull(fileName) + "_" + locale.getLanguage() + ensureNotNull(fileExtension);
    }

    /**
     * Catches and ignores any raised exceptions.
     */
    public static void hush(Runnable runnable) {
        try {
            Objects.requireNonNull(runnable).run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
