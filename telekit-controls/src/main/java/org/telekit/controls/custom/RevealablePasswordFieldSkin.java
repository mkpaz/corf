package org.telekit.controls.custom;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.skin.TextFieldSkin;

public abstract class RevealablePasswordFieldSkin extends TextFieldSkin {

    public RevealablePasswordFieldSkin(RevealablePasswordField control) {
        super(control);
    }

    @Override
    protected String maskText(String txt) {
        if (revealPasswordProperty().get()) { return txt; }
        return String.valueOf(((RevealablePasswordField) getNode()).getBullet()).repeat(txt.length());
    }

    public abstract BooleanProperty revealPasswordProperty();
}
