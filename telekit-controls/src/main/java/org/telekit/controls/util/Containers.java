package org.telekit.controls.util;

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.function.Supplier;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.layout.Region.USE_PREF_SIZE;

public class Containers {

    public static <T extends Pane> T create(Supplier<T> supplier, String... styleClasses) {
        T pane = supplier.get();
        pane.getStyleClass().addAll(styleClasses);
        return pane;
    }

    public static void setFixedWidth(Region region, double width) {
        region.setPrefWidth(width);
        usePrefWidth(region);
    }

    public static void setFixedHeight(Region region, double height) {
        region.setPrefHeight(height);
        usePrefHeight(region);
    }

    public static void usePrefWidth(Region region) {
        region.setMinWidth(USE_PREF_SIZE);
        region.setMaxWidth(USE_PREF_SIZE);
    }

    public static void usePrefHeight(Region region) {
        region.setMinHeight(USE_PREF_SIZE);
        region.setMaxHeight(USE_PREF_SIZE);
    }

    public static void setAnchors(Parent parent, Insets insets) {
        if (insets.getTop() >= 0) { AnchorPane.setTopAnchor(parent, insets.getTop()); }
        if (insets.getRight() >= 0) { AnchorPane.setRightAnchor(parent, insets.getRight()); }
        if (insets.getBottom() >= 0) { AnchorPane.setBottomAnchor(parent, insets.getBottom()); }
        if (insets.getLeft() >= 0) { AnchorPane.setLeftAnchor(parent, insets.getLeft()); }
    }

    public static Rectangle2D getAnchors(Stage stage) {
        return new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
    }

    public static void setAnchors(Stage stage, Rectangle2D bounds) {
        stage.setHeight(bounds.getHeight());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setX(bounds.getMinX());
    }

    public static void setScrollConstraints(ScrollPane scrollPane,
                                            ScrollBarPolicy vbarPolicy, boolean fitHeight,
                                            ScrollBarPolicy hbarPolicy, boolean fitWidth) {
        scrollPane.setVbarPolicy(vbarPolicy);
        scrollPane.setFitToHeight(fitHeight);
        scrollPane.setHbarPolicy(hbarPolicy);
        scrollPane.setFitToWidth(fitWidth);
    }

    public static ColumnConstraints columnConstraints(Priority hgrow) {
        return columnConstraints(USE_COMPUTED_SIZE, hgrow);
    }

    public static ColumnConstraints columnConstraints(double minWidth, Priority hgrow) {
        return columnConstraints(minWidth, USE_COMPUTED_SIZE, hgrow == Priority.ALWAYS ? Double.MAX_VALUE : USE_PREF_SIZE, hgrow);
    }

    public static ColumnConstraints columnConstraints(double minWidth,
                                                      double prefWidth,
                                                      double maxWidth,
                                                      Priority hgrow) {
        ColumnConstraints constraints = new ColumnConstraints(minWidth, prefWidth, maxWidth);
        constraints.setHgrow(hgrow);
        return constraints;
    }

    public static RowConstraints rowConstraints(Priority vgrow) {
        RowConstraints constraints = new RowConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, vgrow == Priority.ALWAYS ? Double.MAX_VALUE : USE_PREF_SIZE);
        constraints.setVgrow(vgrow);
        return constraints;
    }

    public static Screen getScreenForStage(Stage stage) {
        for (Screen screen : Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1)) {
            return screen;
        }
        return Screen.getPrimary();
    }

    public static Region horizontalSpacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    public static Region verticalSpacer() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    public static Region horizontalGap(double value) {
        Region region = new Region();
        HBox.setHgrow(region, Priority.NEVER);
        setFixedWidth(region, value);
        return region;
    }

    public static Region verticalGap(double value) {
        Region region = new Region();
        VBox.setVgrow(region, Priority.NEVER);
        setFixedHeight(region, value);
        return region;
    }
}
