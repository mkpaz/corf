package org.telekit.base.desktop;

import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public record Dimension(double width, double height) {

    public Dimension add(Dimension dimension) {
        return new Dimension(width() + dimension.width(), height() + dimension.height());
    }

    public Dimension subtract(Dimension dimension) {
        return new Dimension(width() - dimension.width(), height() - dimension.height());
    }

    public boolean gt(Rectangle2D rect) {
        return rect.getWidth() < width && rect.getHeight() < height;
    }

    public boolean lt(Rectangle2D rect) {
        return rect.getWidth() > width && rect.getHeight() > height;
    }

    public static Dimension of(double size) {
        return new Dimension(size, size);
    }

    public static Dimension of(Stage stage) {
        return new Dimension(stage.getWidth(), stage.getHeight());
    }

    public static Dimension of(Region region) {
        return new Dimension(region.getWidth(), region.getHeight());
    }
}
