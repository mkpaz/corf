package corf.base.desktop.controls;

import atlantafx.base.controls.CustomTextFieldSkin;
import javafx.scene.Cursor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

public abstract class RevealablePasswordFieldSkin extends CustomTextFieldSkin {

    protected final FontIcon toggleIcon = new FontIcon(Material2MZ.VISIBILITY_OFF);

    public RevealablePasswordFieldSkin(RevealablePasswordField control) {
        super(control);

        rightProperty().set(toggleIcon);
        toggleIcon.setCursor(Cursor.HAND);

        registerChangeListener(control.revealPasswordProperty(), e -> toggleIcon.setIconCode(
                control.isRevealPassword() ? Material2MZ.VISIBILITY : Material2MZ.VISIBILITY_OFF
        ));

        toggleIcon.setOnMouseClicked(e -> control.setRevealPassword(!control.isRevealPassword()));
    }

    @Override
    protected String maskText(String txt) {
        if (getSkinnable() instanceof RevealablePasswordField pf) {
            if (pf.revealPasswordProperty().get()) { return txt; }
            return String.valueOf(pf.getBullet()).repeat(txt.length());
        }
        return String.valueOf(RevealablePasswordField.BULLET).repeat(txt.length());
    }
}
