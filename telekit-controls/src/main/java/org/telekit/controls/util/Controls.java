package org.telekit.controls.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.controls.custom.RevealablePasswordField;
import org.telekit.controls.custom.ToggleIcon;

import java.util.function.Supplier;

public final class Controls {

    public static <T extends Node> T create(Supplier<T> supplier, String... styleClasses) {
        T control = supplier.get();
        control.getStyleClass().addAll(styleClasses);
        return control;
    }

    public static FontIcon fontIcon(Ikon icon, String... styleClasses) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add("font-icon");
        fontIcon.getStyleClass().addAll(styleClasses);
        return fontIcon;
    }

    public static Button button(String text, Ikon icon, String... styleClasses) {
        Button button = new Button(text);
        if (icon != null) { button.setGraphic(fontIcon(icon)); }
        button.getStyleClass().addAll(styleClasses);
        return button;
    }

    public static Button iconButton(Ikon icon, String... styleClasses) {
        Button button = button(null, icon, "icon-button");
        button.getStyleClass().addAll(styleClasses);
        return button;
    }

    public static Button circleIconButton(Ikon icon, String... styleClasses) {
        Button button = button(null, icon, "circle-icon-button");
        button.getStyleClass().addAll(styleClasses);
        // radius should be > 0, but exact value doesn't matter
        // it will be adjusted to the font size automatically
        button.setShape(new Circle(6));
        return button;
    }

    public static MenuButton menuIconButton(Ikon icon, String... styleClasses) {
        MenuButton button = new MenuButton();
        button.setGraphic(fontIcon(icon));
        button.getStyleClass().add("menu-icon-button");
        button.getStyleClass().addAll(styleClasses);
        return button;
    }

    public static MenuItem menuItem(String text, Node graphic, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem(text);
        if (graphic != null) { item.setGraphic(graphic); }
        item.setOnAction(handler);
        return item;
    }

    public static Label gridLabel(String text, HPos hpos, Node node) {
        Label label = new Label(text);
        label.setLabelFor(node);
        label.setWrapText(false);
        GridPane.setHalignment(label, hpos);
        return label;
    }

    public static RevealablePasswordField passwordField() {
        ToggleIcon toggle = new ToggleIcon(Material2MZ.VISIBILITY_OFF, Material2MZ.VISIBILITY);
        toggle.setCursor(Cursor.HAND);
        StackPane.setMargin(toggle, new Insets(0, 10, 0, 0));

        RevealablePasswordField passwordField = new RevealablePasswordField();
        passwordField.revealPasswordProperty().bind(toggle.toggledProperty());

        StackPane authPasswordPane = new StackPane();
        authPasswordPane.getChildren().addAll(passwordField, toggle);
        authPasswordPane.setAlignment(Pos.CENTER_RIGHT);
        toggle.toFront();

        return passwordField;
    }
}
