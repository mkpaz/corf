package org.telekit.desktop.views.system;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.Env;
import org.telekit.base.desktop.ModalController;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.internal.PluginBox;
import org.telekit.base.plugin.internal.PluginState;
import org.telekit.base.preferences.Language;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.desktop.i18n.DesktopMessages;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.plugin.internal.PluginState.*;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.desktop.i18n.DesktopMessages.*;

@Singleton
public class PreferencesView extends VBox implements Initializable, View<PreferencesViewModel>, ModalController {

    private static final String RESTART_MARK = "*";

    TabPane tabs;
    Button applyBtn;
    Button cancelBtn;

    ComboBox<Language> langChoice;
    TextField proxyUrlText;
    TextField proxyUsernameText;
    TextField proxyPasswordText;

    ListView<PluginBox> listPlugins;
    TextArea pluginInfo;
    Button installPluginBtn;
    MenuButton pluginMenuBtn;
    MenuItem pluginToggleItem;
    MenuItem pluginUninstallItem;
    MenuItem pluginDocsItem;

    private final PreferencesViewModel model;

    private Runnable closeHandler;

    @Inject
    public PreferencesView(PreferencesViewModel model) {
        this.model = model;

        createView();
    }

    private void createView() {
        tabs = Controls.create(TabPane::new, "no-menu-button");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        tabs.getTabs().addAll(
                createCommonTab(),
                createPluginsTab()
        );

        applyBtn = new Button(t(DesktopMessages.ACTION_APPLY));
        applyBtn.setDefaultButton(true);

        cancelBtn = new Button(t(DesktopMessages.ACTION_CANCEL));

        HBox controlsBox = Containers.create(HBox::new, "controls");
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(controlsBox, Priority.NEVER);
        controlsBox.getChildren().addAll(
                new Label(RESTART_MARK + " - " + t(DesktopMessages.PREFERENCES_REQUIRES_RESTART)),
                horizontalSpacer(),
                applyBtn,
                cancelBtn
        );

        getChildren().addAll(tabs, controlsBox);
        setId("preferences");
    }

    private Tab createCommonTab() {
        Label langLabel = new Label(t(DesktopMessages.LANGUAGE) + " " + RESTART_MARK);
        langChoice = new ComboBox<>();

        HBox proxyGroupHeader = Containers.create(HBox::new, "group-header");
        proxyGroupHeader.setAlignment(Pos.BASELINE_LEFT);
        proxyGroupHeader.getChildren().addAll(new Label(t(DesktopMessages.PREFERENCES_PROXY)), horizontalSeparator());

        Label proxyUrlLabel = new Label("URL");
        proxyUrlText = new TextField();

        Label proxyUsernameLabel = new Label(t(DesktopMessages.USERNAME));
        proxyUsernameText = new TextField();

        Label proxyPasswordLabel = new Label(t(DesktopMessages.PASSWORD));
        proxyPasswordText = new TextField();

        // GRID

        GridPane grid = Containers.create(GridPane::new, "grid");

        grid.add(langLabel, 0, 0, 2, 1);
        grid.add(langChoice, 2, 0);

        grid.add(proxyGroupHeader, 0, 1, GridPane.REMAINING, 1);
        grid.add(proxyUrlLabel, 1, 2);
        grid.add(proxyUrlText, 2, 2);
        grid.add(proxyUsernameLabel, 1, 3);
        grid.add(proxyUsernameText, 2, 3);
        grid.add(proxyPasswordLabel, 1, 4);
        grid.add(proxyPasswordText, 2, 4);

        grid.getColumnConstraints().addAll(
                columnConstraints(10, Priority.NEVER),  // imitates padding for nested properties
                columnConstraints(200, Priority.NEVER), // property name
                columnConstraints(Priority.ALWAYS)              // property value
        );

        // TAB

        Tab tab = new Tab(t(DesktopMessages.PREFERENCES_GENERAL));
        tab.setContent(grid);
        return tab;
    }

    private Tab createPluginsTab() {
        installPluginBtn = new Button(t(DesktopMessages.ACTION_INSTALL));
        installPluginBtn.setGraphic(Controls.fontIcon(Material2AL.GET_APP));
        installPluginBtn.setOnAction(e -> installPlugin());

        HBox installBox = new HBox();
        installBox.setSpacing(10);
        installBox.setAlignment(Pos.CENTER_LEFT);
        installBox.getChildren().addAll(
                installPluginBtn,
                horizontalSpacer()
        );

        listPlugins = Controls.create(ListView::new, "plugins");
        listPlugins.setCellFactory(lv -> new PluginListCell());

        pluginInfo = Controls.create(TextArea::new, "plugin-info", "monospace");

        pluginMenuBtn = Controls.menuIconButton(Material2MZ.SETTINGS, "flat-dropdown");

        pluginToggleItem = new MenuItem();
        pluginToggleItem.setOnAction(e -> togglePlugin());

        pluginUninstallItem = new MenuItem(t(DesktopMessages.ACTION_UNINSTALL));
        pluginUninstallItem.setOnAction(e -> uninstallPlugin());

        pluginDocsItem = new MenuItem(t(DesktopMessages.DOCUMENTATION));
        pluginDocsItem.setOnAction(e -> openDocs());

        pluginMenuBtn.getItems().setAll(
                pluginToggleItem,
                pluginUninstallItem,
                pluginDocsItem
        );

        AnchorPane pluginInfoBox = new AnchorPane();
        pluginInfoBox.getChildren().addAll(pluginInfo, pluginMenuBtn);
        setAnchors(pluginInfo, new Insets(0, 0, 0, 0));
        setAnchors(pluginMenuBtn, new Insets(1, 1, -1, -1));

        // GRID

        GridPane grid = Containers.create(GridPane::new, "grid");

        grid.add(listPlugins, 0, 0);
        grid.add(installBox, 0, 1, GridPane.REMAINING, 1);

        grid.add(pluginInfoBox, 1, 0);

        grid.getColumnConstraints().setAll(
                Containers.columnConstraints(Priority.NEVER),
                Containers.columnConstraints(Priority.ALWAYS)
        );
        grid.getColumnConstraints().get(0).setPercentWidth(40);

        // TAB

        Tab tab = new Tab(t(DesktopMessages.PLUGINS));
        tab.setContent(grid);
        return tab;
    }

    @Override
    public void initialize() {
        // full width tabs
        tabs.tabMinWidthProperty().bind(widthProperty().divide(tabs.getTabs().size()).subtract(20));

        // CONTROLS

        applyBtn.setOnAction(e -> {
            model.applyCommand().execute();
            close();
        });

        cancelBtn.setOnAction(e -> close());

        // GENERAL

        langChoice.getItems().addAll(Language.values());
        langChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Language lang) {
                return lang.getDisplayName();
            }

            @Override
            public Language fromString(String displayName) {
                return Arrays.stream(Language.values())
                        .filter(lang -> lang.getDisplayName().equals(displayName))
                        .findFirst()
                        .orElse(Language.EN);
            }
        });
        langChoice.valueProperty().bindBidirectional(model.languageProperty());

        proxyUrlText.textProperty().bindBidirectional(model.proxyUrlProperty());
        proxyUsernameText.textProperty().bindBidirectional(model.proxyUsernameProperty());
        proxyPasswordText.textProperty().bindBidirectional(model.proxyPasswordProperty());

        // PLUGINS

        listPlugins.setItems(model.getPlugins());
        model.selectedPluginProperty().bind(listPlugins.getSelectionModel().selectedItemProperty());
        model.selectedPluginProperty().addListener((obs, old, value) -> updatePluginDetails(value));

        pluginMenuBtn.visibleProperty().bind(model.selectedPluginProperty().isNotNull());
        pluginMenuBtn.setOnShowing(e -> {
            PluginBox plugin = model.selectedPluginProperty().get();
            if (plugin == null) { return; }

            PluginState state = plugin.getState();
            if (state == STARTED || state == FAILED) {
                // can be disabled when in STARTED of FAILED state
                pluginToggleItem.setText(t(ACTION_DISABLE));
                pluginToggleItem.setVisible(true);
            } else if (state == STOPPED || state == DISABLED) {
                // can be re-enable when in DISABLED or STOPPED state
                pluginToggleItem.setText(t(ACTION_ENABLE));
                pluginToggleItem.setVisible(true);
            } else {
                // toggle option is unavailable in LOADED, INSTALLED, UNINSTALLED
                pluginToggleItem.setVisible(false);
            }

            pluginDocsItem.setVisible(plugin.getPlugin().providesDocs());
        });
    }

    private void updatePluginDetails(PluginBox pluginBox) {
        if (pluginBox == null) {
            pluginInfo.setText("");
            return;
        }

        Plugin plugin = pluginBox.getPlugin();
        Metadata metadata = plugin.getMetadata();

        final int padding = 10;

        final TextBuilder tb = new TextBuilder();
        // TODO: i18n for plugin metadata
        tb.appendLine(rightPad("Name:", padding), metadata.getName());
        tb.appendLine(rightPad("Version:", padding), metadata.getVersion());
        tb.appendLine(rightPad("Author:", padding), metadata.getAuthor());
        tb.appendLine(rightPad("Homepage:", padding), metadata.getHomePage());
        tb.appendLine(rightPad("Status:", padding), pluginBox.getState().name());
        tb.appendLine(rightPad("Class:", padding), className(pluginBox.getPluginClass()));

        tb.newLine();
        tb.appendLine("Description:");
        tb.appendLine(metadata.getDescription());

        // don't display resources info for plugins in installation state
        // it's an intermediate state, some resources may reside in temp dirs, some may be already deleted
        if (pluginBox.getState() != INSTALLED && pluginBox.getState() != UNINSTALLED) {
            tb.newLine();
            tb.appendLine("Resources:");

            Path pluginDataDir = Env.getPluginDataDir(pluginBox.getPluginClass());
            Path pluginJarPath = pluginBox.getJarPath();

            if (pluginJarPath != null) {
                tb.appendLine(pluginDataDir.relativize(pluginJarPath).toString());
            }

            for (Path path : pluginBox.getConfigs()) {
                tb.appendLine(pluginDataDir.relativize(path).toString());
            }
        }

        pluginInfo.setText(tb.toString());
    }

    private void togglePlugin() {
        PluginBox plugin = model.selectedPluginProperty().get();
        int index = listPlugins.getSelectionModel().getSelectedIndex();
        if (plugin == null) { return; }

        model.togglePluginCommand().execute();
        listPlugins.getSelectionModel().select(index);
    }

    private void installPlugin() {
        File zipFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_ZIP), "*.zip")
                .build()
                .showOpenDialog(getWindow());

        if (zipFile == null) { return; }

        model.installPluginCommand().execute(zipFile.toPath());

        Dialogs.info()
                .title(t(INFO))
                .content(t(PLUGIN_MANAGER_MSG_INSTALL_SUCCESS))
                .owner(getWindow())
                .build()
                .showAndWait();
    }

    private void uninstallPlugin() {
        PluginBox plugin = model.selectedPluginProperty().get();
        if (plugin == null) { return; }

        boolean deleteResources = false;
        if (plugin.hasConfigs()) {
            Alert dialog = Dialogs.confirm()
                    .title(t(CONFIRMATION))
                    .content(t(PLUGIN_MANAGER_MSG_UNINSTALL_CONFIRM))
                    .setButtonTypes(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                    .owner(getWindow())
                    .build();
            Optional<ButtonType> confirmation = dialog.showAndWait();

            if (confirmation.isEmpty() || confirmation.get().equals(ButtonType.CANCEL)) {
                return;
            }

            deleteResources = confirmation.get().equals(ButtonType.YES);
        }

        model.uninstallPluginCommand().execute(deleteResources);

        Dialogs.info()
                .title(t(INFO))
                .content(t(PLUGIN_MANAGER_MSG_UNINSTALL_SUCCESS))
                .owner(getWindow())
                .build()
                .showAndWait();
    }

    private void openDocs() {
        PluginBox plugin = model.selectedPluginProperty().get();
        if (plugin != null && plugin.getPlugin().providesDocs()) {
            plugin.getPlugin().openDocs(Locale.getDefault());
        }
    }

    @Override
    public Region getRoot() {
        return this;
    }

    @Override
    public void reset() {}

    @Override
    public PreferencesViewModel getViewModel() {
        return model;
    }

    @Override
    public Runnable getOnCloseRequest() { return closeHandler; }

    @Override
    public void setOnCloseRequest(Runnable closeHandler) { this.closeHandler = closeHandler; }

    ///////////////////////////////////////////////////////////////////////////

    static class PluginListCell extends ListCell<PluginBox> {

        private static final PseudoClass DISABLED = PseudoClass.getPseudoClass("disabled");

        @Override
        public void updateItem(PluginBox pluginBox, boolean empty) {
            super.updateItem(pluginBox, empty);

            if (pluginBox == null) {
                setText(null);
            } else {
                Plugin plugin = pluginBox.getPlugin();
                Metadata metadata = plugin.getMetadata();
                setText(metadata.getName());

                pseudoClassStateChanged(DISABLED, pluginBox.getState() != STARTED);
            }
        }
    }
}
