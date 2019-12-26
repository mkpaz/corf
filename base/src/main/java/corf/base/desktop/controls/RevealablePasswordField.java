package corf.base.desktop.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public class RevealablePasswordField extends TextField {

    static final char BULLET = '\u25cf';
    private final char bullet;

    public RevealablePasswordField() {
        this("", BULLET);
    }

    public RevealablePasswordField(String text, char bullet) {
        super(text);
        this.bullet = bullet;
    }

    /** Return password mask symbol. */
    public char getBullet() {
        return bullet;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    /**
     * Returns an ObjectProperty wrapping the {@link Node} that is placed
     * on the left of the text field.
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    /**
     * Returns the {@link Node} that is placed on the left of the text field.
     */
    public final Node getLeft() {
        return left.get();
    }

    /**
     * Sets the {@link Node} that is placed on the left of the text field.
     */
    public final void setLeft(Node value) {
        left.set(value);
    }

    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

    /**
     * Property representing the {@link Node} that is placed on the right of the text field.
     * That's private, because right side is reserved for the password (un) mask toggle icon.
     */
    private ObjectProperty<Node> rightProperty() {
        return right;
    }

    private final BooleanProperty revealPassword = new SimpleBooleanProperty(this, "revealPassword", false) {
        @Override
        protected void invalidated() {
            String txt = getText();
            setText(null);
            setText(txt);
        }
    };

    /**
     * Returns whether password revealed or not.
     */
    public boolean isRevealPassword() {
        return revealPassword.get();
    }

    /**
     * Toggles password revealed state.
     */
    public void setRevealPassword(boolean state) {
        this.revealPassword.set(state);
    }

    /**
     * Property representing password revealed state.
     */
    public BooleanProperty revealPasswordProperty() {
        return revealPassword;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RevealablePasswordFieldSkin(this) {
            @Override
            public ObjectProperty<Node> leftProperty() {
                return RevealablePasswordField.this.leftProperty();
            }

            @Override
            public ObjectProperty<Node> rightProperty() {
                return RevealablePasswordField.this.rightProperty();
            }
        };
    }
}
