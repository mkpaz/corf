package org.telekit.base.util;

import java.awt.*;
import java.io.File;
import java.net.URI;

public final class DesktopUtils {

    public static void openQuietly(File file) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        try {
            if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
            }
        } catch (Throwable ignored) {}
    }

    public static void browseQuietly(URI uri) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        try {
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            }
        } catch (Throwable ignored) {}
    }
}
