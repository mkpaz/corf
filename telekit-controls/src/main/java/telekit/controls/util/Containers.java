package telekit.controls.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.function.Supplier;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.layout.Region.USE_PREF_SIZE;

public class Containers {

    public static final ColumnConstraints HGROW_NEVER = columnConstraints(Priority.NEVER);
    public static final ColumnConstraints HGROW_SOMETIMES = columnConstraints(Priority.SOMETIMES);
    public static final ColumnConstraints HGROW_ALWAYS = columnConstraints(Priority.ALWAYS);

    public static final RowConstraints VGROW_NEVER = rowConstraints(Priority.NEVER);
    public static final RowConstraints VGROW_SOMETIMES = rowConstraints(Priority.SOMETIMES);
    public static final RowConstraints VGROW_ALWAYS = rowConstraints(Priority.ALWAYS);

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
        //@formatter:off
        if (insets.getTop()    >= 0) { AnchorPane.setTopAnchor(parent, insets.getTop());       }
        if (insets.getRight()  >= 0) { AnchorPane.setRightAnchor(parent, insets.getRight());   }
        if (insets.getBottom() >= 0) { AnchorPane.setBottomAnchor(parent, insets.getBottom()); }
        if (insets.getLeft()   >= 0) { AnchorPane.setLeftAnchor(parent, insets.getLeft());     }
        //@formatter:on
    }

    public static void setAnchors(Stage stage, Rectangle2D bounds) {
        stage.setWidth(bounds.getWidth());
        stage.setX(bounds.getMinX());
        stage.setHeight(bounds.getHeight());
        stage.setY(bounds.getMinY());
    }

    public static Rectangle2D getAnchors(Stage stage) {
        return new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
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
        double maxWidth = hgrow == Priority.ALWAYS ? Double.MAX_VALUE : USE_PREF_SIZE;
        ColumnConstraints constraints = new ColumnConstraints(minWidth, USE_COMPUTED_SIZE, maxWidth);
        constraints.setHgrow(hgrow);
        return constraints;
    }

    public static RowConstraints rowConstraints(Priority vgrow) {
        return rowConstraints(USE_COMPUTED_SIZE, vgrow);
    }

    public static RowConstraints rowConstraints(double minHeight, Priority vgrow) {
        double maxHeight = vgrow == Priority.ALWAYS ? Double.MAX_VALUE : USE_PREF_SIZE;
        RowConstraints constraints = new RowConstraints(minHeight, USE_COMPUTED_SIZE, maxHeight);
        constraints.setVgrow(vgrow);
        return constraints;
    }

    public static HBox hbox(double spacing, Pos alignment, Insets padding, String... styleClasses) {
        HBox box = new HBox();
        box.setSpacing(spacing);
        box.setAlignment(alignment);
        box.setPadding(padding);
        box.getStyleClass().addAll(styleClasses);
        return box;
    }

    public static VBox vbox(double spacing, Pos alignment, Insets padding, String... styleClasses) {
        VBox box = new VBox();
        box.setSpacing(spacing);
        box.setAlignment(alignment);
        box.setPadding(padding);
        box.getStyleClass().addAll(styleClasses);
        return box;
    }

    public static GridPane gridPane(double hgap, double vgap, Insets padding, String... styleClasses) {
        GridPane grid = new GridPane();
        grid.setHgap(hgap);
        grid.setVgap(vgap);
        grid.setPadding(padding);
        grid.getStyleClass().addAll(styleClasses);
        return grid;
    }

    public static TabPane stretchedTabPane(Tab... tabs) {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("no-menu-button");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().setAll(tabs);
        // should be set after adding tabs
        tabPane.tabMinWidthProperty().bind(
                tabPane.widthProperty().divide(tabPane.getTabs().size()).subtract(20)
        );
        return tabPane;
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

    public static Region horizontalSeparator() {
        Separator separator = new Separator();
        HBox.setHgrow(separator, Priority.ALWAYS);
        return separator;
    }

    public static Region verticalSeparator() {
        Separator separator = new Separator();
        VBox.setVgrow(separator, Priority.ALWAYS);
        return separator;
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
