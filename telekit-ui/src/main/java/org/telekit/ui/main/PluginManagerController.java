package org.telekit.ui.main;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.telekit.base.Env;
import org.telekit.base.EventBus;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.Dialogs;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.internal.PluginBox;
import org.telekit.base.plugin.internal.PluginException;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.plugin.internal.PluginState;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.util.DesktopUtils;
import org.telekit.base.util.TextBuilder;
import org.telekit.ui.domain.ApplicationEvent;
import org.telekit.ui.domain.ApplicationEvent.Type;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.telekit.base.plugin.internal.PluginState.*;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.ui.domain.ApplicationEvent.Type.PREFERENCES_CHANGED;
import static org.telekit.ui.main.MessageKeys.*;

public class PluginManagerController extends Controller {

    public @FXML GridPane rootPane;
    public @FXML ListView<PluginListItem> listPlugins;
    public @FXML Button btnUninstall;
    public @FXML TextArea taPluginDetails;
    public @FXML HBox panePluginControls;
    public @FXML Button btnPluginEnable;
    public @FXML Button btnPluginDisable;
    public @FXML Hyperlink lnkPluginDocs;

    private final ApplicationPreferences preferences;
    private final PluginManager pluginManager;
    private final ObservableList<PluginListItem> pluginListItems = FXCollections.observableArrayList();

    @Inject
    public PluginManagerController(ApplicationPreferences preferences, PluginManager pluginManager) {
        this.preferences = preferences;
        this.pluginManager = pluginManager;
    }

    @FXML
    public void initialize() {
        listPlugins.setItems(new FilteredList<>(pluginListItems, item ->
                item != null && item.getPluginBox().getState() != UNINSTALLED
        ));
        listPlugins.setCellFactory(lv -> new PluginListCell(PluginListItem::selectedProperty));
        listPlugins.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) updatePluginDetails(newValue.getPluginBox());
        });

        panePluginControls.visibleProperty()
                .bind(Bindings.isNotEmpty(pluginListItems));

        // unclickable Enable button for plugins in state ...
        btnPluginEnable.disableProperty()
                .bind(hasPluginState(EnumSet.of(STARTED, INSTALLED, FAILED)));

        // unclickable Disable button for plugins in state ...
        btnPluginDisable.disableProperty()
                .bind(hasPluginState(EnumSet.of(DISABLED, INSTALLED)));

        // unclickable Uninstall button
        btnUninstall.disableProperty()
                .bind(Bindings.isEmpty(pluginListItems));

        updatePluginsList(0);
    }

    public BooleanBinding hasPluginState(Set<PluginState> allowedStatus) {
        ReadOnlyObjectProperty<PluginListItem> selectedItemProperty = listPlugins.getSelectionModel().selectedItemProperty();
        return Bindings.createBooleanBinding(
                () -> selectedItemProperty != null && selectedItemProperty.get() != null &&
                        allowedStatus.contains(selectedItemProperty.get().getPluginBox().getState()),
                selectedItemProperty
        );
    }

    private void updatePluginsList(int defaultSelectedIndex) {
        taPluginDetails.clear();

        // plugins list utilizes FilteredList to hide some rows
        // don't create new, flush current one instead
        pluginListItems.clear();
        pluginManager.getAllPlugins()
                .forEach(pluginBox -> pluginListItems.add(new PluginListItem(pluginBox)));

        if (!pluginListItems.isEmpty()) {
            listPlugins.getSelectionModel().select(defaultSelectedIndex);
        }
    }

    private void updatePluginDetails(PluginBox pluginBox) {
        Plugin plugin = pluginBox.getPlugin();
        Metadata metadata = plugin.getMetadata();

        lnkPluginDocs.setVisible(pluginBox.doesPluginProvideDocs());

        final int padding = 10;

        final TextBuilder tb = new TextBuilder();
        tb.appendLine(rightPad("Name:", padding), metadata.getName());
        tb.appendLine(rightPad("Version:", padding), metadata.getVersion());
        tb.appendLine(rightPad("Author:", padding), metadata.getAuthor());
        tb.appendLine(rightPad("Homepage:", padding), metadata.getHomePage());
        tb.appendLine(rightPad("Status:", padding), pluginBox.getState().name());

        tb.newLine();
        tb.appendLine("Description:");
        tb.appendLine(metadata.getDescription());

        // don't display resources info for plugins in installation state
        // it's an intermediate state, some resources may reside in temp dirs, some may be already deleted
        if (pluginBox.getState() != INSTALLED && pluginBox.getState() != UNINSTALLED) {
            tb.newLine();
            tb.appendLine("Resources:");
            appendPluginResources(pluginBox, tb);
        }

        taPluginDetails.setText(tb.toString());
    }

    private void appendPluginResources(PluginBox pluginBox, TextBuilder tb) {
        Path pluginJarPath = pluginBox.getPluginJarPath();
        if (pluginJarPath != null) {
            tb.appendLine(Env.PLUGINS_DIR.getParent().relativize(pluginJarPath).toString());
        }

        Path pluginDocsPath = Env.getPluginDocsDir(pluginBox.getPluginClass());
        for (Path path : pluginBox.getPluginDataPaths()) {
            // don't show docs files
            if (path.startsWith(pluginDocsPath)) continue;

            tb.appendLine(Env.DATA_DIR.getParent().relativize(path).toString());
        }
    }

    @FXML
    public void installPlugin() {
        File zipFile = Dialogs.file()
                .addFilter(Messages.get(FILE_DIALOG_ZIP), "*.zip")
                .build()
                .showOpenDialog(rootPane.getScene().getWindow());

        if (zipFile == null) return;

        pluginManager.installPlugin(zipFile.toPath());

        updatePluginsList(0);
        Dialogs.info()
                .title(Messages.get(INFO))
                .content(Messages.get(PLUGIN_MANAGER_MSG_INSTALL_SUCCESS))
                .build()
                .showAndWait();

        EventBus.getInstance().publish(new ApplicationEvent(Type.RESTART_REQUIRED));
    }

    @FXML
    public void uninstallPlugin() {
        PluginListItem selectedListItem = listPlugins.getSelectionModel().getSelectedItem();
        if (selectedListItem == null) return;

        PluginBox pluginBox = selectedListItem.getPluginBox();
        Plugin plugin = pluginBox.getPlugin();
        PluginState originalPluginState = pluginBox.getState();

        boolean deleteResources = false;

        if (pluginBox.hasStoredData()) {
            Alert dialog = Dialogs.confirm()
                    .title(Messages.get(CONFIRMATION))
                    .content(Messages.get(PLUGIN_MANAGER_MSG_UNINSTALL_CONFIRM))
                    .setButtonTypes(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                    .build();
            Optional<ButtonType> confirmation = dialog.showAndWait();
            if (confirmation.isEmpty() || confirmation.get().equals(ButtonType.CANCEL)) {
                return;
            }

            deleteResources = confirmation.get().equals(ButtonType.YES);
        }

        pluginManager.uninstallPlugin(plugin.getClass(), deleteResources);

        updatePluginsList(0);
        Dialogs.info()
                .title(Messages.get(INFO))
                .content(Messages.get(PLUGIN_MANAGER_MSG_UNINSTALL_SUCCESS))
                .build()
                .showAndWait();

        if (originalPluginState == DISABLED) {
            preferences.getDisabledPlugins().remove(className(pluginBox.getPluginClass()));
            EventBus.getInstance().publish(new ApplicationEvent(PREFERENCES_CHANGED));
        }
        EventBus.getInstance().publish(new ApplicationEvent(Type.RESTART_REQUIRED));
    }

    @FXML
    public void close() {
        rootPane.getScene().getWindow().hide();
    }

    @FXML
    public void openDocs() {
        PluginListItem selectedListItem = listPlugins.getSelectionModel().getSelectedItem();
        if (selectedListItem == null) return;

        PluginBox pluginBox = selectedListItem.getPluginBox();
        pluginBox.getPluginDocsIndex(preferences.getLocale())
                .ifPresent(path -> DesktopUtils.openQuietly(path.toFile()));
    }

    @FXML
    public void enablePlugin() {
        togglePlugin(true);
    }

    @FXML
    public void disablePlugin() {
        togglePlugin(false);
    }

    private void togglePlugin(boolean enable) {
        PluginListItem selectedListItem = listPlugins.getSelectionModel().getSelectedItem();
        int selectedIndex = listPlugins.getSelectionModel().getSelectedIndex();
        if (selectedListItem == null) return;

        PluginBox pluginBox = selectedListItem.getPluginBox();

        try {
            if (enable) {
                pluginManager.enablePlugin(pluginBox.getPluginClass());
                preferences.getDisabledPlugins().remove(className(pluginBox.getPluginClass()));
            } else {
                pluginManager.disablePlugin(pluginBox.getPluginClass());
                preferences.getDisabledPlugins().add(className(pluginBox.getPluginClass()));
            }
        } catch (PluginException e) {
            throw new TelekitException(e.getMessage(), e);
        }

        updatePluginsList(selectedIndex);
        EventBus.getInstance().publish(new ApplicationEvent(PREFERENCES_CHANGED));
    }

    @Override
    public void reset() {}

    ///////////////////////////////////////////////////////////////////////////

    public static class PluginListItem {

        private final PluginBox pluginBox;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty();

        public PluginListItem(PluginBox pluginBox) {
            this.pluginBox = pluginBox;
        }

        public PluginBox getPluginBox() {
            return pluginBox;
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }
    }

    public static class PluginListCell extends CheckBoxListCell<PluginListItem> {

        public PluginListCell(Callback<PluginListItem, ObservableValue<Boolean>> getSelectedProperty) {
            super(getSelectedProperty);
        }

        @Override
        public void updateItem(PluginListItem listItem, boolean empty) {
            super.updateItem(listItem, empty);

            if (listItem == null) {
                setText(null);
            } else {
                PluginBox pluginBox = listItem.getPluginBox();
                Plugin plugin = pluginBox.getPlugin();
                Metadata metadata = plugin.getMetadata();

                setText(String.format("%s; v%s", metadata.getName(), metadata.getVersion()));

                CheckBox cb = (CheckBox) getGraphic();
                cb.setDisable(true);
                cb.setSelected(pluginBox.getState() == STARTED);
            }
        }
    }
}
