package corf.base.desktop;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class OS {

    /** Returns system clipboard content. */
    public static @Nullable String getClipboard() {
        return Clipboard.getSystemClipboard().getString();
    }

    /**
     * Sets system clipboard content.
     * Null value is ignored.
     */
    public static void setClipboard(@Nullable String s) {
        if (s == null) { return; }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(s);
        clipboard.setContent(content);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AWT                                                                   //
    ///////////////////////////////////////////////////////////////////////////

    /** Tries to open file using {@link Desktop} API. */
    public static void open(File file) {
        Objects.requireNonNull(file, "file");
        if (!isActionSupported(Desktop.Action.OPEN)) { return; }

        SwingUtilities.invokeLater(() -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Tries to browse URI using {@link Desktop} API. */
    public static void browse(URI uri) {
        Objects.requireNonNull(uri, "uri");
        if (!isActionSupported(Desktop.Action.BROWSE)) { return; }

        SwingUtilities.invokeLater(() -> {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** See {@link OS#open(File)}. */
    public static void openQuietly(final File file) {
        try {
            open(file);
        } catch (Throwable ignored) { /* dear error-prone, when I say 'silently', I mean it ' */ }
    }

    /** See {@link OS#browse(URI)}. */
    public static void browseQuietly(final URI uri) {
        try {
            browse(uri);
        } catch (Throwable ignored) { /* dear error-prone, when I say 'silently', I mean it ' */ }
    }

    /** Checks whether {@link Desktop} action supported. */
    public static boolean isActionSupported(Desktop.Action action) {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(action);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other                                                                 //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Checks whether 'XDG_CURRENT_DESKTOP' env variable matches any value from
     * the given arg list.
     */
    public static boolean xdgDesktopMatches(String... args) {
        var value = System.getenv("XDG_CURRENT_DESKTOP");
        if (value == null) { return false; }
        for (var name : args) {
            if (value.contains(name)) { return true; }
        }
        return false;
    }

    /** Executes 'xdg-open' with the given arg. */
    public static void xdgOpen(String arg) {
        Objects.requireNonNull(arg, "arg");

        try {
            if (Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1) {
                Runtime.getRuntime().exec(new String[] { "xdg-open", arg });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Resolves '$XDG_CONFIG_HOME' env variable. */
    public static @Nullable Path getXdgConfigDir() {
        var value = System.getenv("XDG_CONFIG_HOME");
        return value != null && !value.isBlank() ? Paths.get(value) : null;
    }

    /** Resolves '%LOCALAPPDATA' env variable. */
    public static @Nullable Path getLocalAppDataDir() {
        var value = System.getenv("LOCALAPPDATA");
        return value != null && !value.isBlank() ? Paths.get(value) : null;
    }
}
