package org.telekit.desktop.views.layout;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.telekit.base.domain.event.Notification;
import org.telekit.controls.dialogs.AlertBuilder;
import org.telekit.controls.i18n.ControlsMessages;
import org.telekit.desktop.IconCache;

import static org.telekit.base.domain.event.Notification.NotificationType;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.desktop.IconCache.ICON_APP;

public final class ModalNotificationDialog {

    public static final int DIALOG_WIDTH = 440;

    private final Alert alert;

    private ModalNotificationDialog(String headerText,
                                    String contentText,
                                    NotificationType notificationType,
                                    Throwable throwable,
                                    Window owner) {

        alert = new AlertBuilder(getAlertType(notificationType))
                .title(getTitle(notificationType))
                .owner(owner)
                .icon(IconCache.get(ICON_APP))
                .build();

        alert.getDialogPane().setPrefWidth(DIALOG_WIDTH);
        alert.getDialogPane().setMaxWidth(DIALOG_WIDTH);
        alert.setResizable(false);

        // header text should also be clipped, because it doesn't support text overrun
        alert.setHeaderText(headerText);

        // expandable content

        if ((contentText != null && headerText.length() != contentText.length()) || notificationType == NotificationType.ERROR) {
            TextArea contentTextArea = new TextArea();
            contentTextArea.setEditable(false);
            contentTextArea.setWrapText(false);
            contentTextArea.setMaxWidth(Double.MAX_VALUE);
            contentTextArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(contentTextArea, Priority.ALWAYS);
            GridPane.setHgrow(contentTextArea, Priority.ALWAYS);

            GridPane expandableContent = new GridPane();
            expandableContent.setMaxWidth(Double.MAX_VALUE);
            expandableContent.add(contentTextArea, 0, 1);

            alert.getDialogPane().setExpanded(false);
            alert.getDialogPane().setExpandableContent(expandableContent);

            if (notificationType == NotificationType.ERROR && throwable != null) {
                contentTextArea.setText(ExceptionUtils.getStackTrace(throwable));
            } else {
                contentTextArea.setText(contentText);
            }
        }
    }

    private AlertType getAlertType(NotificationType notificationType) {
        return switch (notificationType) {
            case ERROR -> AlertType.ERROR;
            case INFO, SUCCESS -> AlertType.INFORMATION;
            case WARNING -> AlertType.WARNING;
        };
    }

    private String getTitle(NotificationType notificationType) {
        return switch (notificationType) {
            case ERROR -> t(ControlsMessages.ERROR);
            case INFO, SUCCESS -> t(ControlsMessages.INFO);
            case WARNING -> t(ControlsMessages.WARNING);
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

    public static ModalNotificationDialog of(Notification notification, Window owner) {
        return new ModalNotificationDialog(
                notification.getClippedText(Toast.MAX_MESSAGE_LEN),
                notification.getText(),
                notification.getType(),
                notification.getThrowable(),
                owner
        );
    }
}