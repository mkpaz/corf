package org.telekit.controls.domain;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Dimension {

    private final double width;
    private final double height;

    public Dimension(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Dimension add(Dimension delta) {
        return new Dimension(
                getWidth() + delta.getWidth(),
                getHeight() + delta.getHeight()
        );
    }

    public Dimension subtract(Dimension delta) {
        return new Dimension(
                getWidth() - delta.getWidth(),
                getHeight() - delta.getHeight()
        );
    }

    public Dimension subtract(double delta) {
        return new Dimension(getWidth() - delta, getHeight() - delta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dimension dimension = (Dimension) o;

        if (Double.compare(dimension.width, width) != 0) return false;
        return Double.compare(dimension.height, height) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(width);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(height);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }

    public static Dimension of(double width, double height) {
        return new Dimension(width, height);
    }

    public static Dimension of(Stage stage) {
        return Dimension.of(stage.getWidth(), stage.getHeight());
    }

    public static Dimension of(Pane pane) {
        return Dimension.of(pane.getWidth(), pane.getHeight());
    }
}
