package org.telekit.desktop.views.layout;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.telekit.base.Env;
import org.telekit.base.desktop.Dimension;
import org.telekit.base.desktop.ModalDialog;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.util.DesktopUtils;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.controls.util.NodeUtils;
import org.telekit.desktop.IconCache;
import org.telekit.desktop.event.CloseRequestEvent;
import org.telekit.desktop.i18n.DesktopMessages;
import org.telekit.desktop.views.MainStage;
import org.telekit.desktop.views.system.PreferencesView;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.telekit.base.Env.APP_NAME;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.PREFERENCES;
import static org.telekit.controls.util.Containers.horizontalSpacer;
import static org.telekit.controls.util.Containers.setAnchors;
import static org.telekit.controls.util.Controls.circleIconButton;
import static org.telekit.controls.util.NodeUtils.isDoubleClick;
import static org.telekit.desktop.IconCache.ICON_APP;
import static org.telekit.desktop.startup.Launcher.RESTART_EXIT_CODE;

@Singleton
public class TitleBarView extends AnchorPane implements Initializable, View<TitleBarViewModel> {

    ToggleButton navDrawerBtn;
    Label titleLabel;
    Button restartPendingBtn;

    private final TitleBarViewModel model;
    private final MainStage mainStage;
    private final Overlay overlay;
    private final NavDrawerView navDrawer;

    private ModalDialog<PreferencesView> preferencesDialog;

    @Inject
    public TitleBarView(TitleBarViewModel model,
                        MainStage mainStage,
                        Overlay overlay,
                        NavDrawerView navDrawer) {
        this.model = model;
        this.mainStage = mainStage;
        this.overlay = overlay;
        this.navDrawer = navDrawer;

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

        restartPendingBtn = Controls.iconButton(Material2MZ.REFRESH, "titlebar-icon-button", "restart-pending-icon");
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
        preferencesItem.setOnAction(e -> showPreferences());

        MenuItem restartItem = new MenuItem(t(DesktopMessages.ACTION_RESTART));
        restartItem.setOnAction(e -> restartApplication());

        MenuItem openDataDirItem = new MenuItem(t(DesktopMessages.SYSTEM_OPEN_DATA_DIR));
        openDataDirItem.setOnAction(e -> openDataDir());

        MenuItem openPluginsDirItem = new MenuItem(t(DesktopMessages.SYSTEM_OPEN_PLUGINS_DIR));
        openPluginsDirItem.setOnAction(e -> openPluginsDir());

        menuBtn.getItems().addAll(
                preferencesItem,
                new SeparatorMenuItem(),
                openDataDirItem,
                openPluginsDirItem,
                new SeparatorMenuItem(),
                restartItem
        );

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
            if (value != null) { toggleNavDrawer(value); }
        });

        restartPendingBtn.visibleProperty().bind(model.restartPendingProperty());

        titleLabel.textProperty().bind(model.appTitleProperty());

        // hiding nav drawer via title bar API is not always possible (e.g. auto-hiding overlay by ESC)
        // this listener restores correct toggle button state, if that happened
        overlay.onFrontProperty().addListener((obs, old, value) -> {
            if (!value && navDrawerBtn.isSelected()) { navDrawerBtn.setSelected(false); }
        });
    }

    public void hideNavDrawer() { navDrawerBtn.setSelected(false); }

    private void toggleNavDrawer(boolean enabled) {
        boolean navDrawerVisible = overlay.contains(navDrawer);
        if (enabled & !navDrawerVisible) {
            overlay.setContent(navDrawer, HPos.LEFT);
            overlay.toFront();
            NodeUtils.begForFocus(navDrawer, 3);
        }
        if (!enabled & navDrawerVisible) {
            overlay.removeContent();
            overlay.toBack();
        }
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public TitleBarViewModel getViewModel() { return model; }

    ///////////////////////////////////////////////////////////////////////////

    private void showPreferences() {
        if (preferencesDialog == null) {
            PreferencesView view = ViewLoader.load(PreferencesView.class);
            preferencesDialog = ModalDialog.builder(view, getWindow())
                    .title(t(PREFERENCES))
                    .inheritStyles()
                    .icon(IconCache.get(ICON_APP))
                    .resizable(false)
                    .build();
            view.setOnCloseRequest(() -> preferencesDialog.hide());
        }
        preferencesDialog.showAndWait();
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
