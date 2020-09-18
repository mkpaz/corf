package org.telekit.ui.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.telekit.base.EventBus;
import org.telekit.base.EventBus.Listener;
import org.telekit.base.Messages;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.fx.Dialogs;
import org.telekit.ui.Launcher;
import org.telekit.ui.domain.ExceptionCaughtEvent;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.telekit.base.util.CollectionUtils.getLast;
import static org.telekit.ui.main.AllMessageKeys.MAIN_MSG_ERROR_OCCURRED;

public class ExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getName());

    private ExceptionDialog exceptionDialog;

    public ExceptionHandler() {
        EventBus.getInstance().subscribe(ExceptionCaughtEvent.class, this::handleExceptionEvent);
    }

    @Listener
    public void handleExceptionEvent(ExceptionCaughtEvent event) {
        showErrorDialog(event.getCause());
    }

    public void showErrorDialog(Throwable throwable) {
        LOGGER.severe(ExceptionUtils.getStackTrace(throwable));

        Platform.runLater(() -> {
            throwable.printStackTrace();

            exceptionDialog = getOrCreateExceptionDialog();
            Throwable cause = findExactCauseOrRootIfNotPresent(throwable, TelekitException.class);

            exceptionDialog.setHeaderText(ensureGrammar(
                    defaultIfBlank(cause.getMessage(), Messages.get(MAIN_MSG_ERROR_OCCURRED))
            ));

            exceptionDialog.setStackTrace(
                    getStackTrace(cause)
            );

            exceptionDialog.showAndWait();
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

    private static class ExceptionDialog {

        private final Alert dialog;
        private final TextArea taStackTrace;

        public ExceptionDialog() {
            dialog = Dialogs.error()
                    .title("Error")
                    .header(null)
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
            dialog.getDialogPane().setMaxWidth(Launcher.MIN_WIDTH / 2.0);
        }

        public void setHeaderText(String text) {
            this.dialog.setHeaderText(text);
        }

        public void setStackTrace(String text) {
            this.taStackTrace.setText(text);
        }

        public void showAndWait() {
            this.dialog.showAndWait();
        }
    }
}
