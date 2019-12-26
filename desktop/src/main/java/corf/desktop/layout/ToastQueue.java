package corf.desktop.layout;

import javafx.scene.layout.VBox;
import corf.base.event.Notification;

public final class ToastQueue extends VBox {

    // Size limitation serves two purposes.
    // - It protects from OOM, when application produces tons create recurring errors (shit happens).
    // - It prevents from overflowing main window height, because scrolling just looks too bad.
    private final int displaySize;

    public ToastQueue(int displaySize) {
        super();

        this.displaySize = displaySize;

        createView();
    }

    private void createView() {
        // fit height to content
        setMaxHeight(USE_PREF_SIZE);

        // make transparent to mouse events,
        // do not use setMouseTransparent(), because it applies to all children
        setPickOnBounds(false);
        setBackground(null);

        setId("notifications");
    }

    public synchronized void add(Notification notification) {
        // maintain display size
        if (getChildren().size() == displaySize) {
            removeToast((Toast) getChildren().get(0));
        }

        var toast = new Toast(
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
        if (getChildren().isEmpty()) {
            toBack();
        }
    }

    private void showModalNotificationDialog(Toast toast) {
        var dialog = new ModalNotificationDialog(toast.getNotification(), getScene().getWindow());
        dialog.show();
    }
}
