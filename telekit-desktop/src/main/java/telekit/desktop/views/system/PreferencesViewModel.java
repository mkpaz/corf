package telekit.desktop.views.system;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;
import telekit.base.desktop.mvvm.*;
import telekit.base.di.Initializable;
import telekit.base.domain.event.Notification;
import telekit.base.domain.exception.TelekitException;
import telekit.base.domain.security.UsernamePasswordCredentials;
import telekit.base.event.DefaultEventBus;
import telekit.base.net.ApacheHttpClient;
import telekit.base.net.HttpClient.Request;
import telekit.base.net.HttpClient.Response;
import telekit.base.net.connection.Scheme;
import telekit.base.plugin.internal.PluginBox;
import telekit.base.plugin.internal.PluginException;
import telekit.base.plugin.internal.PluginManager;
import telekit.base.plugin.internal.PluginState;
import telekit.base.preferences.Proxy;
import telekit.base.preferences.internal.ApplicationPreferences;
import telekit.base.preferences.internal.Language;
import telekit.base.preferences.internal.ManualProxy;
import telekit.controls.util.Promise;
import telekit.controls.util.TransformationListHandle;
import telekit.desktop.event.PendingRestartEvent;
import telekit.desktop.i18n.DesktopMessages;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.apache.commons.lang3.ClassUtils.getCanonicalName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static telekit.base.i18n.I18n.t;
import static telekit.base.net.HttpConstants.Method.GET;
import static telekit.base.plugin.internal.PluginState.DISABLED;
import static telekit.base.plugin.internal.PluginState.UNINSTALLED;
import static telekit.desktop.i18n.DesktopMessages.PREFERENCES_MSG_PROXY_CONNECTION_FAILED;
import static telekit.desktop.i18n.DesktopMessages.PREFERENCES_MSG_PROXY_CONNECTION_SUCCESSFUL;

@Singleton
public class PreferencesViewModel implements Initializable, ViewModel {

    private final ApplicationPreferences preferences;
    private final PluginManager pluginManager;
    private final YAMLMapper yamlMapper;
    private final ExecutorService threadPool;

    @Inject
    public PreferencesViewModel(ApplicationPreferences preferences,
                                PluginManager pluginManager,
                                YAMLMapper yamlMapper,
                                ExecutorService threadPool) {
        this.preferences = preferences;
        this.pluginManager = pluginManager;
        this.yamlMapper = yamlMapper;
        this.threadPool = threadPool;
    }

    @Override
    public void initialize() {
        language.set(preferences.getLanguage());
        setProxyProperties();

        plugins.getFilteredList().setPredicate(p -> p.getState() != UNINSTALLED);
        updatePluginsList();
    }

    private void setProxyProperties() {
        activeProxyProfile.set(preferences.getProxyPreferences().getActiveProfile());

        for (Proxy proxy : preferences.getProxyPreferences().getProfiles()) {
            if (proxy instanceof ManualProxy manualProxy) {
                proxyScheme.set(manualProxy.getScheme());
                proxyHost.set(manualProxy.getHost());
                proxyPort.set(manualProxy.getPort());
                proxyExceptions.set(String.join(";", manualProxy.getExceptions()));

                if (manualProxy.getCredentials() != null) {
                    proxyUsername.set(manualProxy.getCredentials().getUsername());
                    proxyPassword.set(manualProxy.getCredentials().getPasswordAsString());
                }
            }
        }
    }

    private @Nullable ManualProxy createManualProxyFromProperties() {
        if (isBlank(proxyHost.get())) { return null; }

        URI uri;
        try {
            uri = new URI(proxyScheme.get().toString(), null, proxyHost.get(), proxyPort.get(), null, null, null);
        } catch (URISyntaxException e) {
            throw new TelekitException(e.getMessage(), e);
        }

        List<String> exceptions = null;
        if (isNotBlank(proxyExceptions.get())) {
            exceptions = Arrays.asList(proxyExceptions.get().split("[,;]"));
        }

        UsernamePasswordCredentials credentials = null;
        if (isNotBlank(proxyUsername.get()) && isNotBlank(proxyPassword.get())) {
            credentials = UsernamePasswordCredentials.of(proxyUsername.get(), proxyPassword.get());
        }

        return new ManualProxy(uri, credentials, exceptions);
    }

    private void updatePluginsList() {
        plugins.getItems().setAll(pluginManager.getAllPlugins());
    }

    private void savePreferences() {
        ApplicationPreferences.save(preferences, yamlMapper);
        preferences.resetDirty();
    }

    private void checkProxy(String ipOrHostname) {
        ApacheHttpClient.Builder httpClientBuilder = ApacheHttpClient.builder()
                .trustAllCertificates();

        try {
            if (ManualProxy.ID.equals(activeProxyProfile.get())) {
                Proxy manualProxy = createManualProxyFromProperties();
                if (manualProxy == null) {
                    String errorUrl = String.format("%s://%s:%d", proxyScheme.get(), proxyHost.get(), proxyPort.get()).toLowerCase();
                    DefaultEventBus.getInstance().publish(Notification.warning(t(DesktopMessages.MSG_INVALID_PARAM, errorUrl)));
                    return;
                } else {
                    httpClientBuilder.proxy(manualProxy);
                }
            }

            ApacheHttpClient httpClient = httpClientBuilder.build();
            Response response = httpClient.execute(new Request(GET, new URI(ipOrHostname), null, null));

            if (response.isSucceeded() || response.isForwarded()) {
                DefaultEventBus.getInstance().publish(Notification.success(t(PREFERENCES_MSG_PROXY_CONNECTION_SUCCESSFUL, ipOrHostname)));
            } else {
                DefaultEventBus.getInstance().publish(Notification.warning(t(PREFERENCES_MSG_PROXY_CONNECTION_FAILED, ipOrHostname)));
            }
        } catch (Exception e) {
            DefaultEventBus.getInstance().publish(Notification.error(t(PREFERENCES_MSG_PROXY_CONNECTION_FAILED, ipOrHostname), e));
        }
    }

    private void togglePlugin() {
        PluginBox plugin = selectedPlugin.get();
        try {
            switch (plugin.getState()) {
                case STARTED, FAILED -> {
                    pluginManager.disablePlugin(plugin.getPluginClass());
                    preferences.getDisabledPlugins().add(getCanonicalName(plugin.getPluginClass()));
                    preferences.setDirty();
                }
                case DISABLED -> {
                    pluginManager.enablePlugin(plugin.getPluginClass());
                    preferences.getDisabledPlugins().remove(getCanonicalName(plugin.getPluginClass()));
                    preferences.setDirty();
                }
            }
        } catch (PluginException e) {
            // PluginException already contains internationalized message
            throw new TelekitException(e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
    private final ObjectProperty<Language> language = new SimpleObjectProperty<>(this, "language");
    public ObjectProperty<Language> languageProperty() { return language; }

    private final ObjectProperty<String> activeProxyProfile = new SimpleObjectProperty<>(this, "activeProxyProfile");
    public ObjectProperty<String> activeProxyProfileProperty() { return activeProxyProfile; }

    private final ObjectProperty<Scheme> proxyScheme = new SimpleObjectProperty<>(this, "proxyScheme", Scheme.HTTP);
    public ObjectProperty<Scheme> proxySchemeProperty() { return proxyScheme; }

    private final StringProperty proxyHost = new SimpleStringProperty(this, "proxyHost");
    public StringProperty proxyHostProperty() { return proxyHost; }

    private final ObjectProperty<Integer> proxyPort = new SimpleObjectProperty<>(this, "proxyPort", 0);
    public ObjectProperty<Integer> proxyPortProperty() { return proxyPort; }

    private final StringProperty proxyUsername = new SimpleStringProperty(this, "proxyUsername");
    public StringProperty proxyUsernameProperty() { return proxyUsername; }

    private final StringProperty proxyPassword = new SimpleStringProperty(this, "proxyPassword");
    public StringProperty proxyPasswordProperty() { return proxyPassword; }

    private final StringProperty proxyExceptions = new SimpleStringProperty(this, "proxyExceptions");
    public StringProperty proxyExceptionsProperty() { return proxyExceptions; }

    private final ReadOnlyBooleanWrapper proxyCheckPending = new ReadOnlyBooleanWrapper(this, "proxyCheckPending");
    public ReadOnlyBooleanProperty proxyCheckPendingProperty() { return proxyCheckPending.getReadOnlyProperty(); }

    private final TransformationListHandle<PluginBox> plugins = new TransformationListHandle<>();
    public ObservableList<PluginBox> getPlugins() { return plugins.getSortedList(); }

    private final ObjectProperty<PluginBox> selectedPlugin = new SimpleObjectProperty<>(this, "selectedPlugin");
    public ObjectProperty<PluginBox> selectedPluginProperty() { return selectedPlugin; }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    private final Command commitCommand = new CommandBase() {
        @Override
        protected void doExecute() {
            boolean bootParamChanged = false;

            if (!Objects.equals(preferences.getLanguage(), language.get())) {
                preferences.setLanguage(language.get());
                bootParamChanged = true;
            }

            Proxy manualProxy = createManualProxyFromProperties();
            if (manualProxy != null) { preferences.getProxyPreferences().addOrUpdateProxy(manualProxy); }
            preferences.getProxyPreferences().setActiveProfile(activeProxyProfile.get());

            final boolean restartRequired = bootParamChanged;
            Promise.runAsync(() -> savePreferences())
                    .then(() -> {
                        if (restartRequired) {
                            DefaultEventBus.getInstance().publish(new PendingRestartEvent());
                        }
                    }).start(threadPool);
        }
    };

    public Command commitCommand() { return commitCommand; }

    // ~

    private final ConsumerCommand<String> checkProxyCommand = new ConsumerCommandBase<>() {

        @Override
        protected void doExecute(String url) {
            proxyCheckPending.set(true);
            Promise.runAsync(() -> checkProxy(url))
                    .then(() -> proxyCheckPending.set(false))
                    .start(threadPool);
        }
    };

    public ConsumerCommand<String> checkProxyCommand() { return checkProxyCommand; }

    // ~

    private final Command togglePluginCommand = new CommandBase() {

        { executable.bind(selectedPlugin.isNotNull()); }

        @Override
        protected void doExecute() {
            Promise.runAsync(() -> {
                togglePlugin();
                if (preferences.isDirty()) {
                    savePreferences();
                }
            }).then(() -> updatePluginsList()).start(threadPool);
        }
    };

    // ~

    public Command togglePluginCommand() { return togglePluginCommand; }

    private final ConsumerCommand<Path> installPluginCommand = new ConsumerCommandBase<>() {

        @Override
        protected void doExecute(Path path) {
            Promise.runAsync(() -> pluginManager.installPlugin(path)).then(() -> {
                updatePluginsList();
                DefaultEventBus.getInstance().publish(new PendingRestartEvent());
            }).start(threadPool);
        }
    };

    public ConsumerCommand<Path> installPluginCommand() { return installPluginCommand; }

    // ~

    private final ConsumerCommand<Boolean> uninstallPluginCommand = new ConsumerCommandBase<>() {

        { executable.bind(selectedPlugin.isNotNull()); }

        @Override
        protected void doExecute(Boolean deleteResources) {
            PluginBox plugin = selectedPlugin.get();
            PluginState originalState = plugin.getState();

            Promise.runAsync(() -> {
                pluginManager.uninstallPlugin(plugin.getPluginClass(), deleteResources);
                if (originalState == DISABLED) {
                    preferences.getDisabledPlugins().remove(getCanonicalName(plugin.getPluginClass()));
                    savePreferences();
                }
            }).then(() -> {
                updatePluginsList();
                DefaultEventBus.getInstance().publish(new PendingRestartEvent());
            }).start(threadPool);
        }
    };

    public ConsumerCommand<Boolean> uninstallPluginCommand() { return uninstallPluginCommand; }
}
