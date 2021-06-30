package org.telekit.desktop.views.layout;

import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.telekit.base.domain.event.Notification;

public class ToastQueue extends VBox {

    // Size limitation serves two purposes.
    // - It protects from OOM, when application produces tons of recurring errors (shit happens).
    // - It prevents from overflowing main window height, because scrolling just looks too bad.
    private final int displaySize;

    private final Window window;

    public ToastQueue(int displaySize, Window window) {
        this.displaySize = displaySize;
        this.window = window;

        createView();
    }

    private void createView() {
        getStyleClass().add("system-notifications");

        // fit height to content
        setMaxHeight(USE_PREF_SIZE);

        // make transparent to mouse events
        // note: do not use setMouseTransparent(), because it also all children
        setPickOnBounds(false);
        setBackground(null);
    }

    public synchronized void add(Notification notification) {
        // maintain display size
        if (getChildren().size() == displaySize) {
            removeToast((Toast) getChildren().get(0));
        }

        Toast toast = new Toast(
                notification,
                this::removeToast,
                this::showModalNotificationDialog
        );

        getChildren().add(toast);
        toFront();
        toast.startExpirationTimer();
    }

    private synchronized void removeToast(Toast toast) {
        getChildren().remove(toast);
        if (getChildren().isEmpty()) { toBack(); }
    }

    private void showModalNotificationDialog(Toast toast) {
        ModalNotificationDialog.of(toast.getNotification(), window).show();
    }
}
