package corf.base.desktop.controls;

import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.util.Objects;
import java.util.function.Consumer;

public class Message extends StackPane {

    private static final int ANIMATION_DURATION = 100;

    public enum Type {
        INFO, SUCCESS, WARNING, DANGER
    }

    private final Type type;
    private final @Nullable String header;
    private final String text;
    private Consumer<Message> closeHandler;
    private boolean animated = true;

    @SuppressWarnings("NullAway.Init")
    public Message(Type type, @Nullable String header, String text) {
        super();

        this.type = Objects.requireNonNull(type);
        this.header = header;
        this.text = Objects.requireNonNull(text);

        createView();
    }

    private void createView() {
        if (header != null) {
            var headerText = new Text(header);
            headerText.getStyleClass().addAll("header");
            StackPane.setMargin(headerText, new Insets(10, 10, 0, 15));
            getChildren().add(headerText);
        }

        var messageText = new TextFlow(new Text(text));
        if (header != null) {
            StackPane.setMargin(messageText, new Insets(40, 10, 10, 15));
        } else {
            StackPane.setMargin(messageText, new Insets(10, 10, 10, 15));
        }
        messageText.maxWidthProperty().bind(widthProperty().subtract(50));
        getChildren().add(messageText);

        var closeBtn = new Button("", new FontIcon(Material2AL.CLOSE));
        closeBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT);
        closeBtn.setOnAction(e -> handleClose());
        StackPane.setMargin(closeBtn, new Insets(2));
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        getChildren().add(closeBtn);

        parentProperty().addListener((obs, old, val) -> {
            if (val != null) { handleOpen(); }
        });

        getStyleClass().setAll("message", type.name().toLowerCase());
    }

    public Type getType() {
        return type;
    }

    public @Nullable String getHeader() {
        return header;
    }

    public String getText() {
        return text;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public void setCloseHandler(Consumer<Message> closeHandler) {
        this.closeHandler = closeHandler;
    }

    private void handleOpen() {
        if (!animated) { return; }
        var transition = new FadeTransition(new Duration(500), this);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();
    }

    private void handleClose() {
        if (!animated) { return; }

        var transition = new FadeTransition(new Duration(ANIMATION_DURATION), this);
        transition.setFromValue(1);
        transition.setToValue(0);
        transition.setOnFinished(e -> {
            if (getParent() != null && getParent() instanceof Pane pane) {
                pane.getChildren().remove(this);
            }
            if (closeHandler != null) {
                closeHandler.accept(this);
            }
        });
        transition.play();
    }
}
