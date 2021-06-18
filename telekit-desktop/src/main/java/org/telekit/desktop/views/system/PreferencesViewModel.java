package org.telekit.desktop.views.system;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import org.telekit.base.desktop.mvvm.*;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.Proxy;
import org.telekit.base.domain.UsernamePasswordCredential;
import org.telekit.base.domain.exception.InvalidInputException;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.i18n.I18n;
import org.telekit.base.net.UriUtils;
import org.telekit.base.plugin.internal.PluginBox;
import org.telekit.base.plugin.internal.PluginException;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.plugin.internal.PluginState;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.preferences.Language;
import org.telekit.controls.util.TransformationListHandle;
import org.telekit.desktop.event.PendingRestartEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.domain.Proxy.NO_PROXY;
import static org.telekit.base.i18n.BaseMessages.MSG_INVALID_PARAM;
import static org.telekit.base.plugin.internal.PluginState.DISABLED;
import static org.telekit.base.plugin.internal.PluginState.UNINSTALLED;
import static org.telekit.base.util.CommonUtils.className;

@Singleton
public class PreferencesViewModel implements Initializable, ViewModel {

    private final ApplicationPreferences preferences;
    private final PluginManager pluginManager;
    private final YAMLMapper yamlMapper;

    @Inject
    public PreferencesViewModel(ApplicationPreferences preferences,
                                PluginManager pluginManager,
                                YAMLMapper yamlMapper) {
        this.preferences = preferences;
        this.pluginManager = pluginManager;
        this.yamlMapper = yamlMapper;
    }

    @Override
    public void initialize() {
        language.set(preferences.getLanguage());

        Proxy proxy = preferences.getProxy();
        if (!NO_PROXY.equals(proxy)) {
            proxyUrl.set(preferences.getProxy().getUri().toString());
            proxyUsername.set(preferences.getProxy().getUsername());
            proxyPassword.set(preferences.getProxy().getPasswordAsString());
        }

        plugins.getFilteredList().setPredicate(p -> p.getState() != UNINSTALLED);
        updatePluginsList();
    }

    private Proxy getProxyFromProperties() {
        String proxyUrl = proxyUrlProperty().get();
        if (isBlank(proxyUrl)) { return null; }

        URI uri;
        try {
            uri = UriUtils.parse(proxyUrl.trim());
        } catch (InvalidInputException e) {
            throw new TelekitException(I18n.t(MSG_INVALID_PARAM, proxyUrl));
        }

        UsernamePasswordCredential credential = null;
        String username = proxyUsername.get();
        String password = proxyPassword.get();
        if (isNotBlank(username) && isNotBlank(password)) {
            credential = UsernamePasswordCredential.of(username, password);
        }

        return Proxy.of(uri, credential);
    }

    private void updatePluginsList() {
        plugins.getItems().setAll(pluginManager.getAllPlugins());
    }

    private void savePreferences() {
        ApplicationPreferences.save(preferences, yamlMapper);
        preferences.resetDirty();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
    private final SimpleObjectProperty<Language> language = new SimpleObjectProperty<>(this, "language");
    public SimpleObjectProperty<Language> languageProperty() { return language; }

    private final SimpleStringProperty proxyUrl = new SimpleStringProperty(this, "proxyUrl");
    public SimpleStringProperty proxyUrlProperty() { return proxyUrl; }

    private final SimpleStringProperty proxyUsername = new SimpleStringProperty(this, "proxyUsername");
    public SimpleStringProperty proxyUsernameProperty() { return proxyUsername; }

    private final SimpleStringProperty proxyPassword = new SimpleStringProperty(this, "proxyPassword");
    public SimpleStringProperty proxyPasswordProperty() { return proxyPassword; }

    private final TransformationListHandle<PluginBox> plugins = new TransformationListHandle<>();
    public ObservableList<PluginBox> getPlugins() { return plugins.getSortedList(); }

    private final SimpleObjectProperty<PluginBox> selectedPlugin = new SimpleObjectProperty<>(this, "selectedPlugin");
    public SimpleObjectProperty<PluginBox> selectedPluginProperty() { return selectedPlugin; }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    private final Command commitCommand = new CommandBase() {
        @Override
        protected void doExecute() {
            boolean restartRequired = false;

            if (!Objects.equals(preferences.getLanguage(), language.get())) {
                preferences.setLanguage(language.get());
                restartRequired = true;
            }

            preferences.setProxy(getProxyFromProperties());

            savePreferences();

            if (restartRequired) { DefaultEventBus.getInstance().publish(new PendingRestartEvent()); }
        }
    };

    public Command commitCommand() { return commitCommand; }

    private final Command togglePluginCommand = new CommandBase() {

        { executable.bind(selectedPlugin.isNotNull()); }

        @Override
        protected void doExecute() {
            PluginBox plugin = selectedPlugin.get();

            try {
                switch (plugin.getState()) {
                    case STARTED, FAILED -> {
                        pluginManager.disablePlugin(plugin.getPluginClass());
                        preferences.getDisabledPlugins().add(className(plugin.getPluginClass()));
                        preferences.setDirty();
                    }
                    case DISABLED -> {
                        pluginManager.enablePlugin(plugin.getPluginClass());
                        preferences.getDisabledPlugins().remove(className(plugin.getPluginClass()));
                        preferences.setDirty();
                    }
                }
            } catch (PluginException e) {
                // PluginException already contains internationalized message
                throw new TelekitException(e.getMessage(), e);
            }

            if (preferences.isDirty()) { savePreferences(); }

            updatePluginsList();
        }
    };

    public Command togglePluginCommand() { return togglePluginCommand; }

    private final ConsumerCommand<Path> installPluginCommand = new ConsumerCommandBase<>() {

        @Override
        protected void doExecute(Path path) {
            pluginManager.installPlugin(path);
            updatePluginsList();
            DefaultEventBus.getInstance().publish(new PendingRestartEvent());
        }
    };

    public ConsumerCommand<Path> installPluginCommand() { return installPluginCommand; }

    private final ConsumerCommand<Boolean> uninstallPluginCommand = new ConsumerCommandBase<>() {

        { executable.bind(selectedPlugin.isNotNull()); }

        @Override
        protected void doExecute(Boolean deleteResources) {
            PluginBox plugin = selectedPlugin.get();
            PluginState originalState = plugin.getState();

            pluginManager.uninstallPlugin(plugin.getPluginClass(), deleteResources);
            updatePluginsList();

            if (originalState == DISABLED) {
                preferences.getDisabledPlugins().remove(className(plugin.getPluginClass()));
                savePreferences();
            }

            DefaultEventBus.getInstance().publish(new PendingRestartEvent());
        }
    };

    public ConsumerCommand<Boolean> uninstallPluginCommand() { return uninstallPluginCommand; }
}
