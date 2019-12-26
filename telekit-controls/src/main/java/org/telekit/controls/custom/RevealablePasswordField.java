package org.telekit.controls.custom;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public class RevealablePasswordField extends TextField {

    private static final char BULLET = '\u25cf';
    private final char bullet;

    public RevealablePasswordField() {
        this("", BULLET);
    }

    public RevealablePasswordField(char bullet) {
        this("", bullet);
    }

    public RevealablePasswordField(String text, char bullet) {
        super(text);
        this.bullet = bullet;
    }

    private final BooleanProperty revealPasswordProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            String txt = getText();
            setText(null);
            setText(txt);
        }
    };

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RevealablePasswordFieldSkin(this) {
            @Override
            public BooleanProperty revealPasswordProperty() {
                return RevealablePasswordField.this.revealPasswordProperty();
            }
        };
    }

    public BooleanProperty revealPasswordProperty() {
        return revealPasswordProperty;
    }

    public char getBullet() {
        return bullet;
    }
}
