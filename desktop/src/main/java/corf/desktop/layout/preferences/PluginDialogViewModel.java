package corf.desktop.layout.preferences;

import backbonefx.di.Initializable;
import backbonefx.mvvm.ConsumerCommand;
import backbonefx.mvvm.RunnableCommand;
import backbonefx.mvvm.ViewModel;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionModel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.desktop.Async;
import corf.base.desktop.ChangeList;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.base.plugin.internal.PluginBox;
import corf.base.plugin.internal.PluginException;
import corf.base.plugin.internal.PluginManager;
import corf.base.plugin.internal.PluginState;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static corf.base.i18n.I18n.t;

@Singleton
public class PluginDialogViewModel implements Initializable, ViewModel {

    private final ApplicationPreferences preferences;
    private final PluginManager pluginManager;
    private final YAMLMapper yamlMapper;
    private final ExecutorService executorService;

    @Inject
    public PluginDialogViewModel(ApplicationPreferences preferences,
                                 PluginManager pluginManager,
                                 YAMLMapper yamlMapper,
                                 ExecutorService executorService) {
        this.preferences = preferences;
        this.pluginManager = pluginManager;
        this.yamlMapper = yamlMapper;
        this.executorService = executorService;
    }

    @Override
    public void init() {
        plugins.getFilteredList().setPredicate(p -> p.getState() != PluginState.UNINSTALLED);
        updatePluginsList(null);
    }

    private void updatePluginsList(@Nullable PluginBox pluginToSelect) {
        plugins.getItems().setAll(pluginManager.getAllPlugins());
        selectPlugin(pluginToSelect);
    }

    private void selectPlugin(@Nullable PluginBox pluginToSelect) {
        if (getSelectionModel() == null) { return; }

        if (!getPlugins().isEmpty()) {
            getSelectionModel().select(pluginToSelect != null ? pluginToSelect : plugins.getSortedList().get(0));
        }
    }

    private void savePreferences() {
        ApplicationPreferences.save(preferences, yamlMapper);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    // @formatter:off
    private final ChangeList<PluginBox> plugins = new ChangeList<>();
    public ObservableList<PluginBox> getPlugins() { return plugins.getSortedList(); }

    private final ObjectProperty<SelectionModel<PluginBox>> selectionModel = new SimpleObjectProperty<>();
    public void setSelectionModel(SelectionModel<PluginBox> model) {
        selectionModel.set(model);

        if (selectedPlugin.isBound()) { selectedPlugin.unbind(); }
        selectedPlugin.bind(model.selectedItemProperty());

        if (!plugins.getItems().isEmpty()) {
            getSelectionModel().select(plugins.getSortedList().get(0));
        }
    }
    public SelectionModel<PluginBox> getSelectionModel() { return selectionModel.get(); }

    private final ReadOnlyObjectWrapper<PluginBox> selectedPlugin = new ReadOnlyObjectWrapper<>();
    public ReadOnlyObjectProperty<PluginBox> selectedPluginProperty() { return selectedPlugin.getReadOnlyProperty(); }
    // @formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == togglePluginCommand ==

    public RunnableCommand togglePluginCommand() { return togglePluginCommand; }

    private final RunnableCommand togglePluginCommand = new RunnableCommand(
            this::togglePlugin, selectedPlugin.isNotNull()
    );

    private void togglePlugin() {
        PluginBox plugin = selectedPlugin.get();

        Runnable runnable = () -> {
            try {
                switch (plugin.getState()) {
                    case STARTED, FAILED -> {
                        pluginManager.disablePlugin(plugin.getPluginClass());
                        preferences.getDisabledPlugins().add(plugin.getPluginClassName());
                    }
                    case DISABLED -> {
                        pluginManager.enablePlugin(plugin.getPluginClass());
                        preferences.getDisabledPlugins().remove(plugin.getPluginClassName());
                    }
                    default -> { /* do nothing */ }
                }

                savePreferences();
            } catch (PluginException e) {
                // PluginException already contains internationalized message
                throw new AppException(StringUtils.defaultString(e.getMessage()), e);
            }
        };

        Async.with(runnable)
                .setOnSucceeded(unused -> {
                    selectionModel.get().clearSelection(); // ugly hack to trigger property update
                    updatePluginsList(plugin);
                })
                .setOnFailed(exception -> Events.fire(Notification.error(exception)))
                .start(executorService);
    }

    // ~

    // == installPluginCommand ==

    public ConsumerCommand<Path> installPluginCommand() { return installPluginCommand; }

    private final ConsumerCommand<Path> installPluginCommand = new ConsumerCommand<>(this::installPlugin);

    private void installPlugin(Path path) {
        Supplier<PluginBox> runnable = () -> pluginManager.installPlugin(path);
        Async.with(runnable)
                .setOnSucceeded(plugin -> {
                    updatePluginsList(plugin);
                    Env.setLastVisitedDir(path.toFile());

                    Events.fire(new ActionEvent<>(EventID.APP_RESTART_PENDING));
                    Events.fire(Notification.info(t(DM.PLUGIN_MSG_INSTALL_SUCCESS)));
                })
                .setOnFailed(exception -> {
                    Env.setLastVisitedDir(path.toFile());
                    Events.fire(Notification.error(exception));
                })
                .start(executorService);
    }

    // == uninstallPluginCommand ==

    public ConsumerCommand<Boolean> uninstallPluginCommand() { return uninstallPluginCommand; }

    private final ConsumerCommand<Boolean> uninstallPluginCommand = new ConsumerCommand<>(
            this::uninstallPlugin, selectedPlugin.isNotNull()
    );

    private void uninstallPlugin(Boolean deleteResources) {
        PluginBox plugin = selectedPlugin.get();
        PluginState originalState = plugin.getState();

        Runnable runnable = () -> {
            pluginManager.uninstallPlugin(plugin.getPluginClass(), deleteResources);
            if (originalState == PluginState.DISABLED) {
                preferences.getDisabledPlugins().remove(plugin.getPluginClassName());
                savePreferences();
            }
        };

        Async.with(runnable)
                .setOnSucceeded(unused -> {
                    updatePluginsList(null);
                    Events.fire(new ActionEvent<>(EventID.APP_RESTART_PENDING));
                    Events.fire(Notification.info(t(DM.PLUGIN_MSG_UNINSTALL_SUCCESS)));
                })
                .setOnFailed(exception -> Events.fire(Notification.error(exception)))
                .start(executorService);
    }
}
