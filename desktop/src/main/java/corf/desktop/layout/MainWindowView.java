package corf.desktop.layout;

import backbonefx.di.Initializable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import corf.base.Env;
import corf.base.desktop.Component;
import corf.base.desktop.Overlay;
import corf.base.desktop.controls.DrawerPane;
import corf.base.desktop.controls.FXHelpers;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.plugin.Plugin;
import corf.base.plugin.Tool;
import corf.base.plugin.internal.PluginManager;
import corf.desktop.EventID;

import java.util.Objects;

import static corf.base.plugin.internal.PluginState.STOPPED;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

@Singleton
public final class MainWindowView extends BorderPane implements Component<MainWindowView>, Initializable {

    public static final Image DEFAULT_TOOL_ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/tools/default_64.png").getResourceAsStream()
    ));
    public static final Image BLANK_TAB_ICON = new Image(Objects.requireNonNull(
            DESKTOP_MODULE.concat("assets/icons/tools/default_64.png").getResourceAsStream()
    ));

    public static final int TOAST_QUEUE_DISPLAY_SIZE = 4;

    private final MainStage mainStage;
    private final SidebarView sidebar;
    private final ToolTabPane toolTabPane;
    private final DrawerPane overlay;
    private final PluginManager pluginManager;

    private ToastQueue toastQueue;

    @Inject
    public MainWindowView(MainStage mainStage,
                          SidebarView sidebar,
                          Overlay overlay,
                          PluginManager pluginManager) {
        super();

        this.mainStage = mainStage;
        this.sidebar = sidebar;
        this.toolTabPane = new ToolTabPane();
        this.overlay = (DrawerPane) overlay;
        this.pluginManager = pluginManager;

        createView();
    }

    private void createView() {
        // we have to obtain window reference from the main stage,
        // because view.getWindow() is null at the time create creation
        toastQueue = new ToastQueue(TOAST_QUEUE_DISPLAY_SIZE);
        StackPane.setAlignment(toastQueue, Pos.TOP_RIGHT);

        var contentArea = new StackPane();
        contentArea.setId("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        contentArea.getChildren().setAll(toastQueue, overlay, toolTabPane);

        setLeft(sidebar);
        setCenter(contentArea);

        setId("main-window");
    }

    @Override
    public void init() {
        overlay.onFrontProperty().addListener((obs, old, val) -> {
            Node focusTarget = null;

            if (val) {
                focusTarget = overlay.getContent();
            } else {
                Tab selectedTab = toolTabPane.getSelectionModel().getSelectedItem();
                if (selectedTab != null) {
                    focusTarget = selectedTab.getContent();
                }
            }

            if (focusTarget != null) {
                FXHelpers.begForFocus(focusTarget, 3);
            }
        });

        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.TOOL_OPEN_IN_CURRENT_TAB) && e.getPayload() instanceof Tool<?> tool) {
                openTool(tool);
            }

            if (e.matches(EventID.TOOL_CREATE_NEW_TAB)) {
                toolTabPane.addTab();
            }

            if (e.matches(EventID.TOOL_CLOSE_CURRENT_TAB)) {
                toolTabPane.closeSelectedTab();
            }

            if (e.matches(EventID.APP_RESTART_PENDING)) {
                mainStage.setTitle("✱ " + Env.APP_NAME);
            }

            if (e.matches(EventID.APP_RESTART_PENDING)) {
                mainStage.setTitle("✱ " + Env.APP_NAME);
            }
        });

        Events.listen(Notification.class, this::displayNotification);

        pluginManager.addEventListener(e -> {
            if (e.getPluginState() == STOPPED) {
                closeTool(e.getPluginClass());
            }
        });
    }

    @Override
    public MainWindowView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    private void openTool(Tool<?> tool) {
        toolTabPane.setCurrentTabContent(tool);
        overlay.hide();
    }

    private void closeTool(Class<? extends Plugin> pluginClass) {
        pluginManager.find(pluginClass).ifPresent(
                plugin -> Platform.runLater(() -> toolTabPane.closePluginTabs(plugin))
        );
    }

    private void displayNotification(Notification notification) {
        // notification can be published from non-FX thread
        Platform.runLater(() -> toastQueue.add(notification));
    }
}
