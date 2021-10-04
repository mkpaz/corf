package telekit.desktop.views.layout;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import telekit.base.domain.event.Notification;
import telekit.controls.util.Containers;
import telekit.controls.util.Controls;

import java.util.Objects;
import java.util.function.Consumer;

import static telekit.base.i18n.I18n.t;
import static telekit.controls.i18n.ControlsMessages.ACTION_MORE;
import static telekit.controls.util.Containers.*;

public class Toast extends GridPane {

    public static final int MAX_MESSAGE_LEN = 150;
    public static final Duration DEFAULT_TIMEOUT = Duration.millis(10_000);

    private final Notification notification;
    private final Timeline timeline;
    private final Consumer<Toast> closeHandler;
    private final Consumer<Toast> expandHandler;

    public Toast(Notification notification,
                 Consumer<Toast> closeHandler,
                 Consumer<Toast> expandHandler) {
        this(notification, closeHandler, expandHandler, DEFAULT_TIMEOUT);
    }

    public Toast(Notification notification,
                 Consumer<Toast> closeHandler,
                 Consumer<Toast> expandHandler,
                 Duration timeout) {

        this.notification = Objects.requireNonNull(notification);
        this.closeHandler = Objects.requireNonNull(closeHandler);
        this.expandHandler = expandHandler;
        this.timeline = createTimer(Objects.requireNonNull(timeout));

        createView();
    }

    private void createView() {
        FontIcon closeBtn = Controls.fontIcon(Material2AL.CLOSE, "close-icon");
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnMouseClicked(e -> close());
        GridPane.setValignment(closeBtn, VPos.TOP);
        GridPane.setHalignment(closeBtn, HPos.RIGHT);

        TextFlow textFlow = Containers.create(TextFlow::new, "text");
        // sadly setMaxHeight() isn't working with TextFlow
        // so, we have to clip notification text manually
        Text text = new Text(notification.getClippedText(Toast.MAX_MESSAGE_LEN));
        textFlow.getChildren().addAll(text);

        Hyperlink expandBtn = new Hyperlink(t(ACTION_MORE));
        expandBtn.setOnAction(e -> expand());
        if (expandHandler == null) { expandBtn.setManaged(false); }

        add(textFlow, 0, 0);
        add(closeBtn, 1, 0);
        add(expandBtn, 0, 2, REMAINING, 1);

        getColumnConstraints().addAll(HGROW_ALWAYS, HGROW_NEVER);
        getRowConstraints().addAll(VGROW_ALWAYS, VGROW_NEVER);

        VBox.setVgrow(this, Priority.NEVER);
        getStyleClass().addAll("toast", notification.getType().name().toLowerCase());
    }

    private Timeline createTimer(Duration timeout) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setDelay(timeout);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(1500), new KeyValue(opacityProperty(), 0.3))
        );
        timeline.setOnFinished(e -> close());
        return timeline;
    }

    public void startExpirationTimer() { timeline.play(); }

    public Notification getNotification() { return notification; }

    private void close() { if (closeHandler != null) { closeHandler.accept(this); } }

    private void expand() { if (expandHandler != null) { expandHandler.accept(this); } }
}