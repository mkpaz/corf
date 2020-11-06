package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public final class CommonUtils {

    public static @Nullable String getPropertyOrEnv(String propertyKey, String envKey) {
        return System.getProperty(propertyKey, System.getenv(envKey));
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
