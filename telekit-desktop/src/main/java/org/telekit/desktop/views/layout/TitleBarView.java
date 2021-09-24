package org.telekit.desktop.views.layout;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.telekit.base.Env;
import org.telekit.base.desktop.Dimension;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.util.DesktopUtils;
import org.telekit.base.util.FileSystemUtils;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.controls.widgets.OverlayBase;
import org.telekit.controls.widgets.OverlayDialog;
import org.telekit.desktop.event.CloseRequestEvent;
import org.telekit.desktop.i18n.DesktopMessages;
import org.telekit.desktop.startup.config.LogConfig;
import org.telekit.desktop.views.MainStage;
import org.telekit.desktop.views.system.PreferencesView;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.telekit.base.Env.APP_NAME;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.util.Containers.horizontalSpacer;
import static org.telekit.controls.util.Containers.setAnchors;
import static org.telekit.controls.util.Controls.circleIconButton;
import static org.telekit.controls.util.NodeUtils.isDoubleClick;
import static org.telekit.desktop.Launcher.RESTART_EXIT_CODE;

@Singleton
public class TitleBarView extends AnchorPane implements Initializable, View<TitleBarViewModel> {

    ToggleButton navDrawerBtn;
    Label titleLabel;
    Button restartPendingBtn;

    private final TitleBarViewModel model;
    private final MainStage mainStage;
    private final OverlayBase overlay;
    private final NavDrawerView navDrawer;
    private final LogConfig logConfig;

    private OverlayDialog preferencesDialog;

    @Inject
    public TitleBarView(TitleBarViewModel model,
                        MainStage mainStage,
                        Overlay overlay,
                        NavDrawerView navDrawer,
                        LogConfig logConfig) {
        this.model = model;
        this.mainStage = mainStage;
        this.overlay = (OverlayBase) overlay;
        this.navDrawer = navDrawer;
        this.logConfig = logConfig;

        createView();
    }

    private void createView() {
        // LEFT
        HBox leftBox = new HBox();
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setFillHeight(true);
        leftBox.setSpacing(10);

        navDrawerBtn = Controls.create(ToggleButton::new, "titlebar-icon-button");
        navDrawerBtn.setGraphic(Controls.fontIcon(Material2MZ.MENU));
        navDrawerBtn.setTooltip(new Tooltip("F2"));

        restartPendingBtn = Controls.iconButton(Material2MZ.RESTORE, "titlebar-icon-button", "restart-pending-icon");
        restartPendingBtn.setTooltip(new Tooltip(t(DesktopMessages.SYSTEM_RESTART_REQUIRED)));
        restartPendingBtn.setOnAction(e -> restartApplication());

        leftBox.getChildren().addAll(navDrawerBtn, restartPendingBtn);

        // CENTER
        HBox centerBox = new HBox();
        centerBox.setAlignment(Pos.CENTER_LEFT);
        centerBox.setFillHeight(true);

        titleLabel = Controls.create(() -> new Label(APP_NAME), "window-title");

        centerBox.getChildren().addAll(titleLabel);

        // RIGHT
        HBox rightBox = new HBox();
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setFillHeight(true);

        MenuButton menuBtn = Controls.menuIconButton(Material2MZ.MORE_VERT, "titlebar-icon-button");
        HBox.setMargin(menuBtn, new Insets(0, 16, 0, 0));

        MenuItem preferencesItem = new MenuItem(t(DesktopMessages.PREFERENCES));
        preferencesItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));
        preferencesItem.setOnAction(e -> showPreferences());

        MenuItem openDataDirItem = new MenuItem(t(DesktopMessages.SYSTEM_OPEN_DATA_DIR));
        openDataDirItem.setOnAction(e -> openDataDir());

        MenuItem openPluginsDirItem = new MenuItem(t(DesktopMessages.SYSTEM_OPEN_PLUGINS_DIR));
        openPluginsDirItem.setOnAction(e -> openPluginsDir());

        menuBtn.getItems().setAll(
                preferencesItem,
                new SeparatorMenuItem(),
                openDataDirItem,
                openPluginsDirItem
        );

        Path logFilePath = logConfig.getLogFilePath();
        if (FileSystemUtils.fileExists(logFilePath)) {
            MenuItem openLogFileItem = new MenuItem(t(DesktopMessages.SYSTEM_OPEN_LOG_FILE));
            openLogFileItem.setOnAction(e -> DesktopUtils.openQuietly(logFilePath.toFile()));
            menuBtn.getItems().add(openLogFileItem);
        }

        MenuItem restartItem = new MenuItem(t(DesktopMessages.ACTION_RESTART));
        restartItem.setOnAction(e -> restartApplication());
        menuBtn.getItems().addAll(new SeparatorMenuItem(), restartItem);

        rightBox.getChildren().addAll(menuBtn, createWindowControls());

        // ROOT
        HBox contentArea = Containers.create(HBox::new, "content");
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        contentArea.setAlignment(Pos.CENTER_LEFT);
        setAnchors(contentArea, new Insets(4));
        contentArea.getChildren().setAll(leftBox, horizontalSpacer(), centerBox, horizontalSpacer(), rightBox);

        setId("titlebar");
        getChildren().addAll(contentArea);
    }

    private HBox createWindowControls() {
        HBox box = Containers.create(HBox::new, "window-controls");
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setFillHeight(true);

        Button minBtn = circleIconButton(Material2MZ.MINIMIZE);
        minBtn.setOnAction(e -> mainStage.minimize());

        Button maxBtn = circleIconButton(Material2MZ.OPEN_IN_FULL);
        maxBtn.setOnAction(e -> mainStage.maximize());

        Button closeBtn = circleIconButton(Material2OutlinedAL.CLOSE);
        closeBtn.setOnAction(e -> DefaultEventBus.getInstance().publish(
                new CloseRequestEvent(Dimension.of(mainStage.getStage()))
        ));

        box.getChildren().addAll(minBtn, maxBtn, closeBtn);

        return box;
    }

    @Override
    public void initialize() {
        // drag window using title bar as drag handle
        mainStage.attachDragHandlers(this);

        // maximize window on double click
        setOnMouseClicked(e -> { if (isDoubleClick(e)) { mainStage.maximize(); } });

        navDrawerBtn.selectedProperty().addListener((obs, old, value) -> {
            if (value != null) { doToggleNavDrawer(value); }
        });

        restartPendingBtn.visibleProperty().bind(model.restartPendingProperty());

        titleLabel.textProperty().bind(model.appTitleProperty());

        // hiding nav drawer via title bar API is not always possible (e.g. auto-hiding overlay by ESC)
        // this listener restores correct toggle button state, if that happened
        overlay.onFrontProperty().addListener((obs, old, value) -> {
            if (!value && navDrawerBtn.isSelected()) { navDrawerBtn.setSelected(false); }
        });

        mainStage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (new KeyCodeCombination(KeyCode.F2).match(e)) {
                toggleNavDrawer();
                e.consume();
            }
        });
    }

    public void showNavDrawer() { navDrawerBtn.setSelected(true); }

    public void hideNavDrawer() { navDrawerBtn.setSelected(false); }

    public void toggleNavDrawer() { navDrawerBtn.setSelected(!navDrawerBtn.isSelected()); }

    // Do not call this method directly to toggle nav drawer visibility as it won't
    // update model properties. Use corresponding show/hide methods instead.
    private void doToggleNavDrawer(boolean enabled) {
        boolean navDrawerVisible = overlay.contains(navDrawer);
        if (enabled & !navDrawerVisible) {
            overlay.show(navDrawer, HPos.LEFT);
        }
        if (!enabled & navDrawerVisible) { overlay.hide(); }
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public TitleBarViewModel getViewModel() { return model; }

    @Override
    public Node getPrimaryFocusNode() { return null; }

    ///////////////////////////////////////////////////////////////////////////

    private void showPreferences() {
        if (preferencesDialog == null) {
            preferencesDialog = ViewLoader.load(PreferencesView.class);
            preferencesDialog.setOnCloseRequest(overlay::hide);
        }
        overlay.show(preferencesDialog);
    }

    private void restartApplication() {
        DefaultEventBus.getInstance().publish(
                new CloseRequestEvent(RESTART_EXIT_CODE, Dimension.of(Objects.requireNonNull(getWindow())))
        );
    }

    private void openDataDir() {
        DesktopUtils.openQuietly(Env.DATA_DIR.toFile());
    }

    private void openPluginsDir() {
        Path pluginsDir = Env.PLUGINS_DIR;
        if (Files.exists(pluginsDir)) {
            DesktopUtils.openQuietly(pluginsDir.toFile());
        }
    }
}
