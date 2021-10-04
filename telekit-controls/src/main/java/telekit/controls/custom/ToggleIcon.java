package telekit.controls.custom;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.util.Objects;

public class ToggleIcon extends Control {

    protected final BooleanProperty toggled = new SimpleBooleanProperty(false);

    private final Ikon initialIcon;
    private final Ikon toggledIcon;

    public ToggleIcon(Ikon initialIcon, Ikon toggledIcon) {
        super();

        this.initialIcon = Objects.requireNonNull(initialIcon);
        this.toggledIcon = Objects.requireNonNull(toggledIcon);

        getStyleClass().add("toggle-icon");
    }

    public boolean getToggled() {
        return toggled.get();
    }

    public BooleanProperty toggledProperty() {
        return toggled;
    }

    public boolean isOn() {
        return getToggled();
    }

    public boolean isOff() {
        return !getToggled();
    }

    public void setToggled(boolean toggled) {
        this.toggled.set(toggled);
    }

    public void toggle() {
        setToggled(!getToggled());
    }

    public Ikon getInitialIcon() {
        return initialIcon;
    }

    public Ikon getToggledIcon() {
        return toggledIcon;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ToggleIconSkin(this);
    }

    public static ToggleIcon squareCheckBox() {
        ToggleIcon icon = new ToggleIcon(
                Material2OutlinedAL.CHECK_BOX_OUTLINE_BLANK,
                Material2OutlinedAL.CHECK_BOX
        );
        icon.getStyleClass().addAll("checkbox");
        return icon;
    }

    public static ToggleIcon squareCheckBox(String styleClass) {
        ToggleIcon icon = squareCheckBox();
        icon.getStyleClass().add(styleClass);
        return icon;
    }
}
