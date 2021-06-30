package org.telekit.desktop.views.layout;

import javafx.animation.FadeTransition;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.desktop.routing.Router;
import org.telekit.base.di.Initializable;
import org.telekit.controls.util.Containers;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainWindowView extends VBox implements Initializable, View<MainWindowViewModel> {

    private final MainWindowViewModel model;
    private final TitleBarView titleBar;
    private final StatusBarView statusBar;
    private final OverlayBase overlay;
    private final Router router;
    private final FadeTransition routeTransition = new FadeTransition(Duration.millis(400));

    @Inject
    public MainWindowView(MainWindowViewModel model,
                          TitleBarView titleBar,
                          StatusBarView statusBar,
                          Overlay overlay,
                          Router router) {
        this.model = model;
        this.titleBar = titleBar;
        this.statusBar = statusBar;
        this.overlay = (OverlayBase) overlay;
        this.router = router;

        createView();

        routeTransition.setNode(router.getRouterPane());
        routeTransition.setFromValue(0);
        routeTransition.setToValue(1);
    }

    private void createView() {
        // content area includes overlay and router pane
        // to switch between normal and pseudo-modal view
        StackPane contentArea = Containers.create(StackPane::new);
        contentArea.setId("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        contentArea.getChildren().setAll(overlay, router.getRouterPane());

        getChildren().addAll(titleBar, contentArea, statusBar);
        setId("main-window");
    }

    @Override
    public void initialize() {
        router.currentRouteProperty().addListener((obs, old, value) -> {
            titleBar.hideNavDrawer();
            routeTransition.playFromStart();
        });
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public MainWindowViewModel getViewModel() { return model; }
}
