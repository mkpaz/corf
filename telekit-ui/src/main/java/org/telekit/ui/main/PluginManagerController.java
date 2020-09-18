package org.telekit.ui.main;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.telekit.base.EventBus;
import org.telekit.base.Messages;
import org.telekit.base.Settings;
import org.telekit.base.fx.Controller;
import org.telekit.base.fx.Dialogs;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.util.TextBuilder;
import org.telekit.ui.domain.ApplicationEvent;
import org.telekit.ui.domain.ApplicationEvent.Type;
import org.telekit.ui.domain.PluginContainer;
import org.telekit.ui.domain.PluginContainer.Status;
import org.telekit.ui.service.PluginManager;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.telekit.base.util.CommonUtils.canonicalName;
import static org.telekit.ui.domain.ApplicationEvent.Type.PLUGINS_STATE_CHANGED;
import static org.telekit.ui.domain.ApplicationEvent.Type.PREFERENCES_CHANGED;
import static org.telekit.ui.main.AllMessageKeys.*;

public class PluginManagerController extends Controller {

    public @FXML GridPane rootPane;
    public @FXML ListView<PluginContainer> listPlugins;
    public @FXML Button btnUninstall;
    public @FXML TextArea taPluginDetails;
    public @FXML HBox panePluginControls;
    public @FXML Button btnPluginEnable;
    public @FXML Button btnPluginDisable;
    public @FXML Hyperlink lnkPluginDocs;

    private Settings settings;
    private PluginManager pluginManager;

    @Inject
    public PluginManagerController(Settings settings, PluginManager pluginManager) {
        this.settings = settings;
        this.pluginManager = pluginManager;
    }

    @FXML
    public void initialize() {
        listPlugins.setCellFactory(lv -> new ListViewCell(PluginContainer::selectedProperty));
        listPlugins.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) updatePluginDetails(newValue);
        });

        panePluginControls.visibleProperty().bind(
                Bindings.isNotEmpty(listPlugins.getItems())
        );

        btnPluginEnable.disableProperty().bind(hasStatus(
                listPlugins.getSelectionModel().selectedItemProperty(),
                EnumSet.of(Status.ENABLED, Status.INACTIVE, Status.UNINSTALLED)
        ));

        btnPluginDisable.disableProperty().bind(hasStatus(
                listPlugins.getSelectionModel().selectedItemProperty(),
                EnumSet.of(Status.DISABLED, Status.INACTIVE, Status.UNINSTALLED)
        ));

        btnUninstall.disableProperty().bind(Bindings.or(
                Bindings.isEmpty(listPlugins.getItems()),
                hasStatus(listPlugins.getSelectionModel().selectedItemProperty(),
                          EnumSet.of(Status.INACTIVE, Status.UNINSTALLED)
                )
        ));

        updatePluginsList(0);
    }

    public BooleanBinding hasStatus(ReadOnlyObjectProperty<PluginContainer> property, Set<Status> statuses) {
        return Bindings.createBooleanBinding(
                () -> property != null && property.get() != null && statuses.contains(property.get().getStatus()),
                property
        );
    }

    private void updatePluginsList(int defaultSelectedIndex) {
        taPluginDetails.clear();
        ObservableList<PluginContainer> plugins = FXCollections.observableArrayList(
                pluginManager.getPlugins(status -> status != Status.UNINSTALLED)
        );
        listPlugins.getItems().clear();
        listPlugins.getItems().addAll(plugins);

        if (plugins.size() > 0) {
            listPlugins.getSelectionModel().select(defaultSelectedIndex);
        }
    }

    private void updatePluginDetails(PluginContainer container) {
        Plugin plugin = container.getPlugin();
        Metadata metadata = plugin.getMetadata();

        lnkPluginDocs.setVisible(plugin.providesDocs());

        final int padding = 10;

        final TextBuilder tb = new TextBuilder();
        tb.appendLine(rightPad("Name:", padding), metadata.getName());
        tb.appendLine(rightPad("Version:", padding), metadata.getVersion());
        tb.appendLine(rightPad("Author:", padding), metadata.getAuthor());
        tb.appendLine(rightPad("Homepage:", padding), metadata.getHomePage());
        tb.appendLine(rightPad("Status:", padding), container.getStatus().name());

        tb.newLine();
        tb.appendLine("Description:");
        tb.appendLine(metadata.getDescription());

        if (EnumSet.of(Status.ENABLED, Status.DISABLED).contains(container.getStatus())) {
            tb.newLine();
            tb.appendLine("Resources:");
            appendPluginResources(plugin, tb);
        }

        taPluginDetails.setText(tb.toString());
    }

    private void appendPluginResources(Plugin plugin, TextBuilder tb) {
        try {
            URL location = plugin.getLocation();
            if (location != null) {
                final Path pluginsDir = Settings.PLUGINS_DIR;
                tb.appendLine(
                        pluginsDir.getParent().relativize(Paths.get(location.toURI())).toString()
                );
            }

            final Path dataDir = Settings.getPluginDataDir(plugin.getClass());
            if (Files.exists(dataDir)) {
                Files.walk(dataDir)
                        .filter(path -> !path.equals(dataDir))
                        .forEach(path -> tb.appendLine(dataDir.getParent().getParent().relativize(path).toString()));
            }
        } catch (Exception ignored) {
        }
    }

    @FXML
    public void installPlugin() {
        File zipFile = Dialogs.file()
                .addFilter(Messages.get(FILE_DIALOG_ZIP), "*.zip")
                .build()
                .showOpenDialog(rootPane.getScene().getWindow());

        if (zipFile == null) return;

        pluginManager.installFromZip(zipFile.toPath());

        updatePluginsList(0);
        Dialogs.info()
                .title(Messages.get(INFO))
                .content(Messages.get(PLUGMAN_MSG_INSTALL_SUCCESS))
                .build()
                .showAndWait();
        EventBus.getInstance().publish(new ApplicationEvent(Type.RESTART_REQUIRED));
    }

    @FXML
    public void uninstallPlugin() {
        PluginContainer container = listPlugins.getSelectionModel().getSelectedItem();
        if (container == null) return;

        Plugin plugin = container.getPlugin();
        boolean deleteResources = false;

        if (container.hasResources()) {
            Alert dialog = Dialogs.confirm()
                    .title(Messages.get(CONFIRMATION))
                    .content(Messages.get(PLUGMAN_MSG_UNINSTALL_CONFIRM))
                    .setButtonTypes(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                    .build();
            Optional<ButtonType> confirmation = dialog.showAndWait();
            if (confirmation.isEmpty() || confirmation.get().equals(ButtonType.CANCEL)) {
                return;
            }

            deleteResources = confirmation.get().equals(ButtonType.YES);
        }

        pluginManager.uninstall(plugin.getClass(), deleteResources);
        settings.getPreferences().getDisabledPlugins().remove(canonicalName(plugin));

        updatePluginsList(0);
        Dialogs.info()
                .title(Messages.get(INFO))
                .content(Messages.get(PLUGMAN_MSG_UNINSTALL_SUCCESS))
                .build()
                .showAndWait();

        EventBus.getInstance().publish(new ApplicationEvent(Type.PLUGINS_STATE_CHANGED));
        EventBus.getInstance().publish(new ApplicationEvent(PREFERENCES_CHANGED));
        EventBus.getInstance().publish(new ApplicationEvent(Type.RESTART_REQUIRED));
    }

    @FXML
    public void close() {
        rootPane.getScene().getWindow().hide();
    }

    @FXML
    public void openDocs() {
        PluginContainer container = listPlugins.getSelectionModel().getSelectedItem();
        if (container == null) return;
        container.getPlugin().openDocs();
    }

    @FXML
    public void enablePlugin() {
        changePluginStatus(Status.ENABLED);
    }

    @FXML
    public void disablePlugin() {
        changePluginStatus(Status.DISABLED);
    }

    private void changePluginStatus(Status status) {
        PluginContainer container = listPlugins.getSelectionModel().getSelectedItem();
        int selectedIndex = listPlugins.getSelectionModel().getSelectedIndex();
        if (container == null) return;

        Plugin plugin = container.getPlugin();
        String canonicalName = canonicalName(plugin);
        pluginManager.setStatus(Set.of(canonicalName), status);

        if (status == Status.ENABLED) {
            settings.getPreferences().getDisabledPlugins().remove(canonicalName);
        }

        if (status == Status.DISABLED) {
            settings.getPreferences().getDisabledPlugins().add(canonicalName);
        }

        updatePluginsList(selectedIndex);

        EventBus.getInstance().publish(new ApplicationEvent(PLUGINS_STATE_CHANGED));
        EventBus.getInstance().publish(new ApplicationEvent(PREFERENCES_CHANGED));
    }

    @Override
    public void reset() {}

    private static class ListViewCell extends CheckBoxListCell<PluginContainer> {

        public ListViewCell(Callback<PluginContainer, ObservableValue<Boolean>> getSelectedProperty) {
            super(getSelectedProperty);
        }

        @Override
        public void updateItem(PluginContainer container, boolean empty) {
            super.updateItem(container, empty);

            if (container == null) {
                setText(null);
            } else {
                Plugin plugin = container.getPlugin();
                Metadata metadata = plugin.getMetadata();

                setText(String.format(" %s; v.%s", metadata.getName(), metadata.getVersion()));

                CheckBox cb = (CheckBox) getGraphic();
                cb.setDisable(true);
                cb.setSelected(container.getStatus() == Status.ENABLED);
            }
        }
    }
}
