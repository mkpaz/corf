package org.telekit.base.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class CommonUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0;
    }

    public static String getPropertyOrEnv(String propertyKey, String envKey) {
        return System.getProperty(propertyKey, System.getenv(envKey));
    }

    public static Path defaultPath(String strPath, Path defaultPath) {
        return strPath != null ? Paths.get(strPath) : defaultPath;
    }

    public static String className(@NotNull Class<?> cls) {
        return cls.getCanonicalName();
    }

    public static String objectClassName(@NotNull Object object) {
        return object.getClass().getCanonicalName();
    }

    /** Catches and ignores any raised exceptions */
    public static void hush(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
