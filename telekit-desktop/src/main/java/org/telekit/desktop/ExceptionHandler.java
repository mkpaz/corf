package org.telekit.desktop;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.event.Listener;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.i18n.ControlsMessages;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.util.CollectionUtils.getLast;
import static org.telekit.desktop.i18n.DesktopMessages.SYSTEM_MSG_ERROR_OCCURRED;

public final class ExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());

    private final UncaughtExceptionHandler uncaughtExceptionHandler =
            (thread, e) -> showErrorDialog(e);

    private final Stage primaryStage;
    private ExceptionDialog exceptionDialog;

    public ExceptionHandler(Stage primaryStage) {
        this.primaryStage = primaryStage;

        DefaultEventBus.getInstance().subscribe(Notification.class, this::handleExceptionEvent);
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() { return uncaughtExceptionHandler; }

    @Listener
    public void handleExceptionEvent(Notification event) {
        // TODO: Implement displaying notifications of other types
        // Move this listener to MainWindowModel and use toast or snackbar to display message
        if (event.getType() == Notification.Type.ERROR && event.getThrowable() != null) {
            event.getThrowable().printStackTrace();
            showErrorDialog(event.getThrowable());
        }
    }

    public synchronized void showErrorDialog(Throwable throwable) {
        LOGGER.severe(ExceptionUtils.getStackTrace(throwable));

        Platform.runLater(() -> {
            throwable.printStackTrace();

            exceptionDialog = getOrCreateExceptionDialog();
            Throwable cause = findExactCauseOrRootIfNotPresent(throwable, TelekitException.class);

            exceptionDialog.setHeaderText(ensureGrammar(
                    defaultIfBlank(cause.getMessage(), t(SYSTEM_MSG_ERROR_OCCURRED))
            ));

            exceptionDialog.setStackTrace(
                    getStackTrace(cause)
            );

            // this check is necessary to prevent from OOM on recurring errors
            if (!exceptionDialog.isShowing()) {
                exceptionDialog.showAndWait();
            }
        });
    }

    private ExceptionDialog getOrCreateExceptionDialog() {
        return this.exceptionDialog == null ? new ExceptionDialog() : this.exceptionDialog;
    }

    private Throwable findExactCauseOrRootIfNotPresent(Throwable throwable, Class<?> clazz) {
        List<Throwable> stackTrace = ExceptionUtils.getThrowableList(throwable);
        List<Throwable> matched = stackTrace.stream()
                .filter(e -> e.getClass() == clazz)
                .collect(Collectors.toList());
        return !matched.isEmpty() ? getLast(matched) : getLast(stackTrace);
    }

    private String ensureGrammar(String str) {
        char last = str.charAt(str.length() - 1);
        if (last != '?' & last != '!' & last != '.') {
            return str + ".";
        } else {
            return str;
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    // TODO: This should be moved to the main view
    private class ExceptionDialog {

        private static final int DIALOG_WIDTH = 440;

        private final Alert dialog;
        private final TextArea taStackTrace;

        public ExceptionDialog() {
            dialog = Dialogs.error()
                    .title(t(ControlsMessages.ERROR))
                    .header(null)
                    .owner(primaryStage)
                    .build();

            taStackTrace = new TextArea();
            taStackTrace.setEditable(false);
            taStackTrace.setWrapText(false);
            taStackTrace.setMaxWidth(Double.MAX_VALUE);
            taStackTrace.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(taStackTrace, Priority.ALWAYS);
            GridPane.setHgrow(taStackTrace, Priority.ALWAYS);

            GridPane expandableContent = new GridPane();
            expandableContent.setMaxWidth(Double.MAX_VALUE);
            expandableContent.add(taStackTrace, 0, 1);

            dialog.getDialogPane().setExpandableContent(expandableContent);
            dialog.getDialogPane().setPrefWidth(DIALOG_WIDTH);
            dialog.getDialogPane().setMaxWidth(DIALOG_WIDTH);
            dialog.setResizable(false);
        }

        public boolean isShowing() {
            return dialog.isShowing();
        }

        public void setHeaderText(String text) {
            dialog.setHeaderText(text);
        }

        public void setStackTrace(String text) {
            dialog.getDialogPane().setExpanded(false);
            taStackTrace.setText(text);
        }

        public void showAndWait() {
            this.dialog.showAndWait();
        }
    }
}
