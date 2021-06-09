package org.telekit.base.desktop;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Window;

public record Dimension(double width, double height) {

    public Dimension add(Dimension d) {
        return new Dimension(width() + d.width(), height() + d.height());
    }

    public Dimension subtract(Dimension d) {
        return new Dimension(width() - d.width(), height() - d.height());
    }

    public boolean gt(Rectangle2D r) {
        return r.getWidth() < width && r.getHeight() < height;
    }

    public boolean lt(Rectangle2D r) {
        return r.getWidth() > width && r.getHeight() > height;
    }

    public static Dimension of(double size) {
        return new Dimension(size, size);
    }

    public static Dimension of(Window w) {
        return new Dimension(w.getWidth(), w.getHeight());
    }

    public static Dimension of(Scene s) {
        return new Dimension(s.getWidth(), s.getHeight());
    }

    public static Dimension of(Region r) {
        return new Dimension(r.getWidth(), r.getHeight());
    }
}
