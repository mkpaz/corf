package corf.desktop.layout;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import corf.base.event.Notification;
import corf.desktop.i18n.DM;

import java.util.Objects;
import java.util.function.Consumer;

import static corf.base.i18n.I18n.t;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

public final class Toast extends GridPane {

    public static final int MAX_MESSAGE_LEN = 150;
    public static final Duration DEFAULT_TIMEOUT = Duration.millis(100_000);

    public final Image IMAGE_INFO = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/dialog-info.png").getResourceAsStream()
    ));
    public final Image IMAGE_SUCCESS = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/dialog-success.png").getResourceAsStream()
    ));
    public final Image IMAGE_WARNING = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/dialog-warning.png").getResourceAsStream()
    ));
    public final Image IMAGE_ERROR = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/dialog-error.png").getResourceAsStream()
    ));

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
        super();

        this.notification = Objects.requireNonNull(notification);
        this.closeHandler = Objects.requireNonNull(closeHandler);
        this.expandHandler = expandHandler;
        this.timeline = createTimer(Objects.requireNonNull(timeout));

        createView();
    }

    private void createView() {
        var closeBtn = new FontIcon(Material2AL.CLOSE);
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnMouseClicked(e -> close());
        GridPane.setValignment(closeBtn, VPos.TOP);
        GridPane.setHalignment(closeBtn, HPos.RIGHT);

        var icon = switch (notification.getType()) {
            case SUCCESS -> new ImageView(IMAGE_SUCCESS);
            case WARNING -> new ImageView(IMAGE_WARNING);
            case ERROR -> new ImageView(IMAGE_ERROR);
            default -> new ImageView(IMAGE_INFO);
        };
        icon.setFitHeight(18);
        icon.setFitWidth(18);
        icon.getStyleClass().add("icon");

        var textFlow = new TextFlow();
        textFlow.getStyleClass().add("text");

        // setMaxHeight() isn't working with TextFlow,
        // so we have to clip notification text manually
        var clippedMsg = notification.getClippedText(Toast.MAX_MESSAGE_LEN);

        var text = new Text(clippedMsg);
        textFlow.getChildren().addAll(text);

        var expandBtn = new Hyperlink(t(DM.MORE));
        expandBtn.setOnAction(e -> expand());

        var forceExpandBtn = notification.getType() == Notification.Type.ERROR && notification.getThrowable() != null;
        if ((!forceExpandBtn && notification.getText().length() == clippedMsg.length()) || expandHandler == null) {
            expandBtn.setManaged(false);
        }

        add(icon, 0, 0);
        add(textFlow, 1, 0);
        add(closeBtn, 2, 0);
        add(expandBtn, 1, 3, REMAINING, 1);

        getColumnConstraints().addAll(
                new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, false),
                new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true),
                new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.RIGHT, false)
        );
        getRowConstraints().addAll(
                new RowConstraints(-1, -1, -1, Priority.ALWAYS, VPos.TOP, true),
                new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.TOP, false)
        );

        VBox.setVgrow(this, Priority.NEVER);
        getStyleClass().addAll("toast", notification.getType().name().toLowerCase());
    }

    private Timeline createTimer(Duration timeout) {
        var timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setDelay(timeout);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(1500), new KeyValue(opacityProperty(), 0.3))
        );
        timeline.setOnFinished(e -> close());
        return timeline;
    }

    public void startExpirationTimer() {
        timeline.play();
    }

    public Notification getNotification() {
        return notification;
    }

    private void close() {
        if (closeHandler != null) {
            closeHandler.accept(this);
        }
    }

    private void expand() {
        if (expandHandler != null) {
            expandHandler.accept(this);
        }
    }
}
