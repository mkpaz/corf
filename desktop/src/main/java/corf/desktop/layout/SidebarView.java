package corf.desktop.layout;

import atlantafx.base.controls.Spacer;
import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.Injector;
import corf.base.common.Lazy;
import corf.base.desktop.Overlay;
import corf.base.desktop.controls.DrawerPane;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;
import corf.desktop.layout.preferences.PluginDialogView;
import corf.desktop.layout.preferences.PreferencesDialogView;

import static atlantafx.base.theme.Styles.BUTTON_ICON;
import static atlantafx.base.theme.Styles.FLAT;
import static corf.base.i18n.I18n.t;

@Singleton
public final class SidebarView extends VBox implements View<SidebarView, SidebarViewModel>,
                                                       Initializable {

    Button navDrawerBtn;
    Button pluginsBtn;
    Button settingsBtn;

    private final SidebarViewModel model;
    private final DrawerPane overlay;
    private final NavDrawerView navDrawer;
    private final Lazy<PreferencesDialogView> preferencesDialog;
    private final Lazy<PluginDialogView> pluginsDialog;

    @Inject
    public SidebarView(SidebarViewModel model,
                       Overlay overlay,
                       NavDrawerView navDrawer) {
        super();

        this.model = model;
        this.overlay = (DrawerPane) overlay;
        this.navDrawer = navDrawer;

        pluginsDialog = new Lazy<>(() -> {
            var dialog = Injector.getInstance().getBean(PluginDialogView.class);
            dialog.setOnCloseRequest(overlay::hide);
            return dialog;
        });

        preferencesDialog = new Lazy<>(() -> {
            var dialog = Injector.getInstance().getBean(PreferencesDialogView.class);
            dialog.setOnCloseRequest(overlay::hide);
            return dialog;
        });

        createView();
    }

    private void createView() {
        navDrawerBtn = createSidebarButton(Material2OutlinedAL.APPS, t(DM.TOOLS));
        pluginsBtn = createSidebarButton(Material2OutlinedAL.CONSTRUCTION, t(DM.PLUGINS));
        settingsBtn = createSidebarButton(Material2OutlinedMZ.SETTINGS, t(DM.PREFERENCES));

        getChildren().setAll(
                navDrawerBtn,
                new Spacer(Orientation.VERTICAL),
                pluginsBtn,
                settingsBtn
        );

        setId("sidebar");
        setPrefWidth(Region.USE_COMPUTED_SIZE);
        setMaxWidth(Region.USE_COMPUTED_SIZE);
    }

    private Button createSidebarButton(Ikon iconCode, String tooltip) {
        var button = new Button("", new FontIcon(iconCode));
        button.getStyleClass().addAll(BUTTON_ICON, FLAT);
        if (tooltip != null) {
            button.setTooltip(new Tooltip(tooltip));
        }
        return button;
    }

    @Override
    public void init() {
        navDrawerBtn.setOnAction(e -> model.toggleNavDrawerCommand().run());

        pluginsBtn.setOnAction(e -> showPluginsDialog());

        settingsBtn.setOnAction(e -> showSettingsDialog());

        model.navDrawerOpenedProperty().addListener((obs, old, val) -> toggleNavDrawer(val));
        overlay.onFrontProperty().addListener((obs, old, val) -> {
            if (!val && model.navDrawerOpenedProperty().get()) {
                model.hideNavDrawerCommand().run();
            }
        });

        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.APP_SHOW_NAVIGATION) && !isNavDrawerVisible()) {
                model.toggleNavDrawerCommand().run();
                return;
            }
            if (e.matches(EventID.APP_HIDE_NAVIGATION) && isNavDrawerVisible()) {
                model.hideNavDrawerCommand().run();
            }
        });
    }

    @Override
    public SidebarView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public SidebarViewModel getViewModel() {
        return model;
    }

    private boolean isNavDrawerVisible() {
        return overlay.contains(navDrawer);
    }

    private void toggleNavDrawer(boolean open) {
        boolean navDrawerVisible = isNavDrawerVisible();
        if (open && !navDrawerVisible) {
            overlay.show(navDrawer, Pos.TOP_LEFT);
            navDrawer.prepareToOpen();
        }
        if (!open && navDrawerVisible) { overlay.hide(); }
    }

    private void showPluginsDialog() {
        var dialog = pluginsDialog.get();
        overlay.show(dialog, Pos.CENTER, Recommends.MODAL_WINDOW_MARGIN);
    }

    private void showSettingsDialog() {
        var dialog = preferencesDialog.get();
        overlay.show(dialog, Pos.CENTER, Recommends.MODAL_WINDOW_MARGIN);
    }
}
