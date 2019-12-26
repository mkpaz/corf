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

    public boolean gt(Dimension d) {
        return d.width() < width && d.height() < height;
    }

    public boolean gte(Dimension d) {
        return d.width() <= width && d.height() <= height;
    }

    public boolean lt(Dimension d) {
        return d.width() > width && d.height() > height;
    }

    public boolean lte(Dimension d) {
        return d.width() >= width && d.height() >= height;
    }

    public static Dimension of(Rectangle2D r) {
        return new Dimension(r.getWidth(), r.getHeight());
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
