package org.telekit.base.util;

import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public final class DesktopUtils {

    public static void open(File file) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    desktop.open(file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // linux hangs if Desktop methods is run in FX thread
            if (SystemUtils.IS_OS_LINUX) {
                new Thread(() -> {
                    try {
                        desktop.open(file);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    public static void browse(URI uri) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    desktop.browse(uri);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // linux hangs if Desktop methods is run in FX thread
            if (SystemUtils.IS_OS_LINUX) {
                new Thread(() -> {
                    try {
                        desktop.browse(uri);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    public static void openQuietly(File file) {
        try {
            open(file);
        } catch (Throwable ignored) {}
    }

    public static void browseQuietly(URI uri) {
        try {
            browse(uri);
        } catch (Throwable ignored) {}
    }

    private static void xdgOpen(String fileOrURI) throws IOException {
        if (Runtime.getRuntime().exec(new String[] {"which", "xdg-open"}).getInputStream().read() != -1) {
            Runtime.getRuntime().exec(new String[] {"xdg-open", fileOrURI});
        }
    }
}
