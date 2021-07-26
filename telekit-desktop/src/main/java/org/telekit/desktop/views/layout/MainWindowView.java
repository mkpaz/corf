package org.telekit.desktop.views.layout;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.desktop.routing.Router;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.event.Listener;
import org.telekit.controls.util.Containers;
import org.telekit.controls.widgets.OverlayBase;
import org.telekit.desktop.views.MainStage;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainWindowView extends VBox implements Initializable, View<MainWindowViewModel> {

    public static final int TOAST_QUEUE_DISPLAY_SIZE = 4;

    private final MainWindowViewModel model;
    private final TitleBarView titleBar;
    private final StatusBarView statusBar;
    private final OverlayBase overlay;
    private final Router router;
    private final MainStage mainStage;
    private final FadeTransition routeTransition = new FadeTransition(Duration.millis(400));

    private ToastQueue toastQueue;

    @Inject
    public MainWindowView(MainWindowViewModel model,
                          TitleBarView titleBar,
                          StatusBarView statusBar,
                          Overlay overlay,
                          Router router,
                          MainStage mainStage) {

        this.model = model;
        this.titleBar = titleBar;
        this.statusBar = statusBar;
        this.overlay = (OverlayBase) overlay;
        this.router = router;
        this.mainStage = mainStage;

        createView();

        routeTransition.setNode(router.getRouterPane());
        routeTransition.setFromValue(0);
        routeTransition.setToValue(1);
    }

    private void createView() {
        // we have to obtain window reference from the main stage,
        // because view.getWindow() is null at the time of creation
        toastQueue = new ToastQueue(TOAST_QUEUE_DISPLAY_SIZE, mainStage.getStage());
        StackPane.setAlignment(toastQueue, Pos.TOP_RIGHT);

        // content area includes overlay and router pane
        // to switch between normal and pseudo-modal view
        StackPane contentArea = Containers.create(StackPane::new);
        contentArea.setId("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        contentArea.getChildren().setAll(toastQueue, overlay, router.getRouterPane());

        getChildren().addAll(titleBar, contentArea, statusBar);
        setId("main-window");
    }

    @Override
    public void initialize() {
        router.currentRouteProperty().addListener((obs, old, value) -> {
            titleBar.hideNavDrawer();
            routeTransition.playFromStart();
        });

        DefaultEventBus.getInstance().subscribe(Notification.class, this::displayNotification);
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public MainWindowViewModel getViewModel() { return model; }

    @Listener
    private void displayNotification(Notification notification) {
        // notification can be published from non-FX thread
        Platform.runLater(() -> toastQueue.add(notification));
    }
}
