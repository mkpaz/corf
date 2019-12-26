package corf.desktop.layout.preferences;

import atlantafx.base.theme.Theme;
import backbonefx.di.Initializable;
import backbonefx.mvvm.RunnableCommand;
import backbonefx.mvvm.ViewModel;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.desktop.Async;
import corf.base.desktop.Observables;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.base.net.ApacheHttpClient;
import corf.base.net.HttpClient.Request;
import corf.base.net.HttpConstants;
import corf.base.net.Scheme;
import corf.base.preferences.Proxy;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.base.preferences.internal.ManualProxy;
import corf.base.security.UsernamePasswordCredentials;
import corf.desktop.i18n.DM;
import corf.desktop.layout.MainStage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static corf.base.i18n.I18n.t;

@Singleton
public class PreferencesDialogViewModel implements Initializable, ViewModel {

    private final ApplicationPreferences preferences;
    private final MainStage mainStage;
    private final YAMLMapper yamlMapper;
    private final ExecutorService executorService;

    @Inject
    public PreferencesDialogViewModel(ApplicationPreferences preferences,
                                      MainStage mainStage,
                                      YAMLMapper yamlMapper,
                                      ExecutorService executorService) {
        this.preferences = preferences;
        this.mainStage = mainStage;
        this.yamlMapper = yamlMapper;
        this.executorService = executorService;
    }

    @Override
    public void init() {
        theme.set(preferences.getStyleTheme());
        setProxyProperties();
    }

    private void setProxyProperties() {
        setProxyProfile(preferences.getProxyPreferences().getActiveProfile());

        for (var proxy : preferences.getProxyPreferences().getProfiles()) {
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
        if (StringUtils.isBlank(proxyHost.get())) { return null; }

        URI uri;
        try {
            uri = new URI(proxyScheme.get().toString(), null, proxyHost.get(), proxyPort.get(), null, null, null);
        } catch (URISyntaxException e) {
            throw new AppException(StringUtils.defaultString(e.getMessage()), e);
        }

        List<String> exceptions = null;
        if (StringUtils.isNotBlank(proxyExceptions.get())) {
            exceptions = Arrays.asList(proxyExceptions.get().split("[,;]"));
        }

        UsernamePasswordCredentials credentials = null;
        if (StringUtils.isNotBlank(proxyUsername.get()) && StringUtils.isNotBlank(proxyPassword.get())) {
            credentials = UsernamePasswordCredentials.of(proxyUsername.get(), proxyPassword.get());
        }

        return new ManualProxy(uri, credentials, exceptions);
    }

    private String getProxyProfile() {
        return proxyEnabled.get() ? ManualProxy.ID : Proxy.OFF;
    }

    private void setProxyProfile(String id) {
        proxyEnabled.set(Objects.equals(id, ManualProxy.ID));
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isProxyProfile(String id) {
        return Objects.equals(getProxyProfile(), id);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();
    public ObjectProperty<Theme> themeProperty() { return theme; }

    private final BooleanProperty proxyEnabled = new SimpleBooleanProperty();
    public BooleanProperty proxyEnabledProperty() { return proxyEnabled; }

    private final ObjectProperty<Scheme> proxyScheme = new SimpleObjectProperty<>(Scheme.HTTP);
    public ObjectProperty<Scheme> proxySchemeProperty() { return proxyScheme; }

    private final StringProperty proxyHost = new SimpleStringProperty();
    public StringProperty proxyHostProperty() { return proxyHost; }

    private final ObjectProperty<Integer> proxyPort = new SimpleObjectProperty<>( 0);
    public ObjectProperty<Integer> proxyPortProperty() { return proxyPort; }

    private final StringProperty proxyUsername = new SimpleStringProperty();
    public StringProperty proxyUsernameProperty() { return proxyUsername; }

    private final StringProperty proxyPassword = new SimpleStringProperty();
    public StringProperty proxyPasswordProperty() { return proxyPassword; }

    private final StringProperty proxyExceptions = new SimpleStringProperty();
    public StringProperty proxyExceptionsProperty() { return proxyExceptions; }

    private final StringProperty proxyCheckUrl = new SimpleStringProperty();
    public StringProperty proxyCheckUrlProperty() { return proxyCheckUrl; }

    private final ReadOnlyBooleanWrapper proxyCheckPending = new ReadOnlyBooleanWrapper();
    public ReadOnlyBooleanProperty proxyCheckPendingProperty() { return proxyCheckPending.getReadOnlyProperty(); }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == applyCommand ==

    public RunnableCommand applyCommand() { return applyCommand; }

    private final RunnableCommand applyCommand = new RunnableCommand(this::saveAndApplyPreferences);

    private void saveAndApplyPreferences() {
        // THEME
        if (!Objects.equals(theme.get(), mainStage.getTheme())) {
            mainStage.setTheme(theme.get());
            preferences.setStyleTheme(theme.get());
        }

        // PROXY
        if (!Objects.equals(preferences.getProxyPreferences().getActiveProfile(), getProxyProfile())) {
            preferences.getProxyPreferences().setActiveProfile(getProxyProfile());
        }

        var manualProxy = createManualProxyFromProperties();
        if (manualProxy != null) {
            preferences.getProxyPreferences().addOrUpdateProxy(manualProxy);
        }

        Async.with(() -> ApplicationPreferences.save(preferences, yamlMapper))
                .setOnFailed(exception -> {
                    if (exception != null) {
                        Events.fire(Notification.error(exception));
                    }
                })
                .start(executorService);
    }

    // == checkProxyCommand ==

    public RunnableCommand checkProxyCommand() { return checkProxyCommand; }

    private final RunnableCommand checkProxyCommand = new RunnableCommand(this::checkProxy, Bindings.and(
            proxyCheckPending.not(), Observables.isNotBlank(proxyCheckUrl)
    ));

    private void checkProxy() {
        var url = proxyCheckUrl.get();
        var httpBuilder = ApacheHttpClient.builder().trustAllCertificates();

        try {
            if (isProxyProfile(ManualProxy.ID)) {
                var proxy = createManualProxyFromProperties();
                if (proxy == null) {
                    String errorUrl = String.format("%s://%s:%d", proxyScheme.get(), proxyHost.get(), proxyPort.get());
                    Events.fire(Notification.warning(t(DM.MSG_INVALID_PARAM, errorUrl.toLowerCase())));
                    return;
                } else {
                    httpBuilder.proxy(proxy);
                }
            }

            var http = httpBuilder.build();
            var request = new Request(HttpConstants.Method.GET, new URI(url), null, null);

            Async.with(() -> http.execute(request))
                    .setOnScheduled(() -> proxyCheckPending.set(true))
                    .setOnSucceeded(response -> {
                        proxyCheckPending.set(false);
                        if (response.isSucceeded() || response.isForwarded()) {
                            Events.fire(Notification.success(t(DM.PREFS_MSG_PROXY_CONNECTION_SUCCESSFUL, url)));
                        } else {
                            Events.fire(Notification.warning(t(DM.PREFS_MSG_PROXY_CONNECTION_FAILED, url)));
                        }
                    })
                    .setOnFailed(e -> proxyCheckPending.set(false))
                    .start(executorService);
        } catch (Exception e) {
            Events.fire(Notification.error(t(DM.PREFS_MSG_PROXY_CONNECTION_FAILED, url), e));
        }
    }
}
