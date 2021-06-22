package org.telekit.controls.custom;

import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

public class ToggleIconSkin extends SkinBase<ToggleIcon> {

    protected static final Ikon CODE_BLANK = Material2OutlinedAL.CHECK_BOX_OUTLINE_BLANK;
    protected static final Ikon CODE_CHECK = Material2OutlinedAL.CHECK_BOX;
    protected static final PseudoClass HOVER = PseudoClass.getPseudoClass("hover");

    protected StackPane content;
    protected FontIcon icon;

    public ToggleIconSkin(ToggleIcon control) {
        super(control);

        icon = new FontIcon(CODE_BLANK);
        icon.getStyleClass().add("icon");

        content = new StackPane();
        content.getChildren().add(icon);
        content.getStyleClass().add("content");

        icon.iconCodeProperty().bind(Bindings.createObjectBinding(
                () -> control.isOn() ?
                        getSkinnable().getToggledIcon() :
                        getSkinnable().getInitialIcon()
                , control.toggledProperty()
        ));
        icon.setOnMouseClicked(event -> control.toggle());

        // maintain hover state
        content.setOnMouseEntered(event -> pseudoClassStateChanged(HOVER, true));
        content.setOnMouseExited(event -> pseudoClassStateChanged(HOVER, false));

        // ensure control is never resized beyond it's preferred size
        control.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        getChildren().add(content);
    }
}
