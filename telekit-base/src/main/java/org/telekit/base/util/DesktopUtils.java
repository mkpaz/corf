package org.telekit.base.util;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.Env;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class DesktopUtils {

    public static boolean isSupported(Desktop.Action action) {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(action);
    }

    public static void open(File file) {
        Objects.requireNonNull(file);
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    desktop.open(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // X can hang if Desktop method is run in FX thread
            if (SystemUtils.IS_OS_LINUX) {
                new Thread(() -> {
                    try {
                        desktop.open(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    public static void browse(URI uri) {
        Objects.requireNonNull(uri);
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // X can hang if Desktop method is run in FX thread
            if (SystemUtils.IS_OS_LINUX) {
                new Thread(() -> {
                    try {
                        desktop.browse(uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    public static void openQuietly(final File file) {
        CommonUtils.hush(() -> open(file));
    }

    public static void browseQuietly(final URI uri) {
        CommonUtils.hush(() -> browse(uri));
    }

    public static boolean xdgCurrentDesktopMatches(String... desktopEnvNames) {
        String xdgCurrentDesktop = System.getenv("XDG_CURRENT_DESKTOP");
        if (xdgCurrentDesktop == null) { return false; }
        for (String name : desktopEnvNames) {
            if (xdgCurrentDesktop.contains(name)) { return true; }
        }
        return false;
    }

    public static void xdgOpen(String fileOrURL) {
        Objects.requireNonNull(fileOrURL);

        try {
            if (Runtime.getRuntime().exec(new String[] {"which", "xdg-open"}).getInputStream().read() != -1) {
                Runtime.getRuntime().exec(new String[] {"xdg-open", fileOrURL});
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // if $XDG_CONFIG_HOME is either not set or empty, a default equal to $HOME/.config should be used.
    public static @NotNull Path getXdgConfigDir() {
        String value = System.getenv("XDG_CONFIG_HOME");
        if (value == null || value.isBlank()) {
            return Env.HOME_DIR.resolve(".config");
        }
        return Paths.get(value);
    }

    public static @NotNull Path getLocalAppDataDir() {
        String value = System.getenv("LOCALAPPDATA");
        if (value == null || value.isBlank()) {
            return Env.HOME_DIR.resolve("AppData").resolve("Local");
        }
        return Paths.get(value);
    }
}
