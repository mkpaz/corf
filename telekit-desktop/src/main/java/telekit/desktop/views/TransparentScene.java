package telekit.desktop.views;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import static telekit.controls.util.Containers.setAnchors;

public class TransparentScene extends Scene {

    protected static final PseudoClass DECORATED = PseudoClass.getPseudoClass("decorated");

    // outer pane which shadow effect when window is not maximized
    protected final AnchorPane shadowPane;
    protected final Insets shadowInsets;

    // content root holds main view and thus the rest of the scene hierarchy
    protected final AnchorPane contentRoot;

    public TransparentScene(double width, double height, int shadowSize) {
        super(new AnchorPane(), width, height, Color.TRANSPARENT);

        shadowPane = (AnchorPane) getRoot();
        shadowPane.getStyleClass().add("transparent-scene");

        contentRoot = new AnchorPane();
        shadowInsets = new Insets(shadowSize);

        // FIXME: Stage flickers with black on resize and upon launch
        // The unwelcome effect can be minimized by setting scene bg close
        // to the root pane bg, but imho flickering with grayish colors is
        // worse than flickering with black. Btw, some Chromium based apps suffer
        // similar issue as well.
        //
        // This is OpenJFX bug with no workarounds:
        // https://bugs.openjdk.java.net/browse/JDK-8243939
        // https://bugs.openjdk.java.net/browse/JDK-8243939

        shadowPane.getChildren().setAll(contentRoot);
        shadowPane.setBackground(Background.EMPTY);
        shadowPane.setCache(true);
    }

    public Parent getContent() {
        if (contentRoot.getChildren().size() == 0) { return null; }
        return (Region) contentRoot.getChildren().get(0);
    }

    public void setContent(Parent content) {
        contentRoot.getChildren().setAll(content);
        setAnchors(content, Insets.EMPTY);
    }

    public void toggleShadow(boolean enabled) {
        // allows removing shadow and paddings when window is maximized
        shadowPane.pseudoClassStateChanged(DECORATED, enabled);
        if (!enabled) {
            setAnchors(contentRoot, Insets.EMPTY);
        } else {
            setAnchors(contentRoot, shadowInsets);
        }
    }
}
