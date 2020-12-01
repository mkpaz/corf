package org.telekit.ui.main;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.telekit.base.domain.UsernamePasswordCredential;
import org.telekit.base.domain.exception.InvalidInputException;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.i18n.Messages;
import org.telekit.base.net.UriUtils;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.preferences.Language;
import org.telekit.base.domain.Proxy;
import org.telekit.base.ui.Controller;
import org.telekit.ui.domain.ApplicationEvent;

import javax.inject.Inject;
import java.net.URI;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.domain.Proxy.NO_PROXY;
import static org.telekit.base.i18n.BaseMessageKeys.MSG_INVALID_PARAM;
import static org.telekit.ui.domain.ApplicationEvent.Type.PREFERENCES_CHANGED;

public class PreferencesController extends Controller {

    private final ApplicationPreferences preferences;

    public @FXML VBox rootPane;
    public @FXML ComboBox<Language> cmbLanguage;
    public @FXML CheckBox cbSystemTray;
    public @FXML TextField tfProxyURL;
    public @FXML TextField tfProxyUsername;
    public @FXML PasswordField pfProxyPassword;

    @Inject
    public PreferencesController(ApplicationPreferences preferences) {
        this.preferences = preferences;
    }

    @FXML
    public void initialize() {
        cmbLanguage.getItems().addAll(Language.values());
        cmbLanguage.setConverter(new StringConverter<>() {
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
        cmbLanguage.getSelectionModel().select(preferences.getLanguage());
        cbSystemTray.setSelected(preferences.isSystemTray());

        Proxy proxy = preferences.getProxy();
        if (!NO_PROXY.equals(proxy)) {
            tfProxyURL.setText(proxy.getUri().toString());
            tfProxyUsername.setText(proxy.getUsername());
            pfProxyPassword.setText(proxy.getPasswordAsString());
        }
    }

    @FXML
    public void apply() {
        preferences.setLanguage(cmbLanguage.getSelectionModel().getSelectedItem());
        preferences.setSystemTray(cbSystemTray.isSelected());
        preferences.setProxy(getProxy());

        rootPane.getScene().getWindow().hide();
        DefaultEventBus.getInstance().publish(new ApplicationEvent(PREFERENCES_CHANGED));
    }

    private Proxy getProxy() {
        String proxyUrl = tfProxyURL.getText();
        if (isBlank(proxyUrl)) return null;

        URI uri;
        try {
            uri = UriUtils.parse(proxyUrl.trim());
        } catch (InvalidInputException e) {
            throw new TelekitException(Messages.get(MSG_INVALID_PARAM, proxyUrl));
        }

        UsernamePasswordCredential credential = null;
        String username = tfProxyUsername.getText();
        String password = pfProxyPassword.getText();
        if (isNotBlank(username) && isNotBlank(password)) {
            credential = UsernamePasswordCredential.of(username, password);
        }

        return Proxy.of(uri, credential);
    }

    @FXML
    public void cancel() {
        rootPane.getScene().getWindow().hide();
    }
}
