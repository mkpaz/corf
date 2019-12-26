package corf.desktop.layout;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.apache.commons.lang3.exception.ExceptionUtils;
import corf.base.Env;
import corf.base.desktop.AlertBuilder;
import corf.base.event.Notification;
import corf.desktop.i18n.DM;

import static corf.base.event.Notification.Type;
import static corf.base.i18n.I18n.t;

public final class ModalNotificationDialog {

    public static final int DIALOG_WIDTH = 440;

    private final Alert alert;

    public ModalNotificationDialog(Notification notification, Window owner) {
        Notification.Type type = notification.getType();

        alert = new AlertBuilder(getAlertType(type))
                .title(getTitle(type))
                .owner(owner)
                .icon(Env.APP_ICON)
                .build();

        alert.getDialogPane().setPrefWidth(DIALOG_WIDTH);
        alert.getDialogPane().setMaxWidth(DIALOG_WIDTH);
        alert.setResizable(false);

        if (type == Type.ERROR && notification.getThrowable() != null) {
            var contentTextArea = new TextArea();
            contentTextArea.setEditable(false);
            contentTextArea.setWrapText(false);
            contentTextArea.setMaxWidth(Double.MAX_VALUE);
            GridPane.setVgrow(contentTextArea, Priority.ALWAYS);
            GridPane.setHgrow(contentTextArea, Priority.ALWAYS);

            var expandableContent = new GridPane();
            expandableContent.setMaxWidth(Double.MAX_VALUE);
            expandableContent.add(contentTextArea, 0, 1);

            alert.getDialogPane().setExpanded(false);
            alert.getDialogPane().setExpandableContent(expandableContent);

            alert.setHeaderText(notification.getClippedText(Toast.MAX_MESSAGE_LEN));
            contentTextArea.setText(ExceptionUtils.getStackTrace(notification.getThrowable()));
        } else {
            alert.setHeaderText(null);
            alert.setContentText(notification.getText());
        }
    }

    private AlertType getAlertType(Type type) {
        return switch (type) {
            case ERROR -> AlertType.ERROR;
            case INFO, SUCCESS -> AlertType.INFORMATION;
            case WARNING -> AlertType.WARNING;
        };
    }

    private String getTitle(Type type) {
        return switch (type) {
            case ERROR -> t(DM.ERROR);
            case INFO, SUCCESS -> t(DM.INFO);
            case WARNING -> t(DM.WARNING);
        };
    }

    public void show() {
        Platform.runLater(() -> {
            if (!alert.isShowing()) { alert.showAndWait(); }
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            if (alert.isShowing()) { alert.hide(); }
        });
    }
}
