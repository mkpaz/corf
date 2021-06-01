package org.telekit.controls.util;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Supplier;

public final class Controls {

    public static <T extends Node> T create(Supplier<T> supplier, String... styleClasses) {
        T control = supplier.get();
        control.getStyleClass().addAll(styleClasses);
        return control;
    }

    public static FontIcon fontIcon(Ikon iconCode, String... styleClasses) {
        FontIcon icon = new FontIcon(iconCode);
        icon.getStyleClass().add("font-icon");
        icon.getStyleClass().addAll(styleClasses);
        return icon;
    }

    public static Button iconButton(Ikon iconCode, String... styleClasses) {
        Button button = new Button();
        button.setGraphic(fontIcon(iconCode));
        button.getStyleClass().add("icon-button");
        button.getStyleClass().addAll(styleClasses);
        return button;
    }

    public static Button circleIconButton(Ikon iconCode, String... styleClasses) {
        Button button = new Button();
        button.setGraphic(fontIcon(iconCode));
        button.getStyleClass().add("circle-icon-button");
        button.getStyleClass().addAll(styleClasses);
        // radius should be > 0, but exact value doesn't matter
        // it will be adjusted to the font size automatically
        button.setShape(new Circle(6));
        return button;
    }

    public static MenuButton menuIconButton(Ikon iconCode, String... styleClasses) {
        MenuButton button = new MenuButton();
        button.setGraphic(fontIcon(iconCode));
        button.getStyleClass().add("menu-icon-button");
        button.getStyleClass().addAll(styleClasses);
        return button;
    }
}
