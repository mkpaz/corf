package telekit.desktop.views.system;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import telekit.base.net.connection.Scheme;
import telekit.base.preferences.Proxy;
import telekit.base.preferences.internal.ManualProxy;
import telekit.controls.custom.RevealablePasswordField;
import telekit.controls.util.Containers;
import telekit.controls.util.Controls;
import telekit.controls.util.IntegerStringConverter;
import telekit.desktop.i18n.DesktopMessages;

import java.util.Optional;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.layout.GridPane.REMAINING;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static telekit.base.i18n.I18n.t;
import static telekit.controls.util.BindUtils.bindToggleGroup;
import static telekit.controls.util.Containers.columnConstraints;
import static telekit.controls.util.Containers.gridPane;
import static telekit.controls.util.Controls.gridLabel;
import static telekit.desktop.i18n.DesktopMessages.*;

public class ProxyPreferencesTab extends Tab {

    ToggleGroup proxyModeToggle;
    RadioButton noProxyToggle;
    RadioButton manualProxyToggle;

    ComboBox<Scheme> proxySchemeChoice;
    TextField proxyHostText;
    Spinner<Integer> proxyPortSpinner;
    TextField proxyUsernameText;
    RevealablePasswordField proxyPasswordText;
    TextField proxyExceptionsText;

    private final PreferencesView view;
    private final PreferencesViewModel model;

    private String lastEnteredUrl;

    public ProxyPreferencesTab(PreferencesView view, PreferencesViewModel model) {
        this.view = view;
        this.model = model;

        createView();
    }

    private void createView() {
        proxyModeToggle = new ToggleGroup();

        noProxyToggle = new RadioButton(t(PREFERENCES_NO_PROXY));
        noProxyToggle.setToggleGroup(proxyModeToggle);
        noProxyToggle.setUserData(Proxy.DISABLED);

        manualProxyToggle = new RadioButton(t(PREFERENCES_MANUAL_PROXY_CONFIGURATION));
        manualProxyToggle.setToggleGroup(proxyModeToggle);
        manualProxyToggle.setPadding(new Insets(0, 0, 5, 0));
        manualProxyToggle.setUserData(ManualProxy.ID);

        BooleanBinding manualProxyDisabledBinding = Bindings.createBooleanBinding(
                () -> proxyModeToggle.getSelectedToggle() == noProxyToggle, proxyModeToggle.selectedToggleProperty()
        );
        bindToggleGroup(proxyModeToggle, model.activeProxyProfileProperty());

        // MANUAL PROXY

        proxySchemeChoice = new ComboBox<>(FXCollections.observableArrayList(ManualProxy.SUPPORTED_SCHEMES));
        proxySchemeChoice.setPrefWidth(100);
        proxySchemeChoice.valueProperty().bindBidirectional(model.proxySchemeProperty());
        proxySchemeChoice.disableProperty().bind(manualProxyDisabledBinding);

        proxyHostText = new TextField();
        HBox.setHgrow(proxyHostText, Priority.ALWAYS);
        proxyHostText.textProperty().bindBidirectional(model.proxyHostProperty());
        proxyHostText.disableProperty().bind(manualProxyDisabledBinding);

        Label proxyPortLabel = new Label(t(PORT));
        proxyPortLabel.setPadding(new Insets(0, 5, 0, 5));

        proxyPortSpinner = new Spinner<>(0, 65535, 3128);
        proxyPortSpinner.setPrefWidth(100);
        proxyPortSpinner.setEditable(true);
        IntegerStringConverter.createFor(proxyPortSpinner);
        proxyPortSpinner.getValueFactory().valueProperty().bindBidirectional(model.proxyPortProperty());
        proxyPortSpinner.disableProperty().bind(manualProxyDisabledBinding);

        HBox proxyUrlBox = Containers.hbox(0, CENTER_LEFT, Insets.EMPTY);
        proxyUrlBox.getChildren().setAll(
                proxySchemeChoice,
                proxyHostText,
                proxyPortLabel,
                proxyPortSpinner
        );

        proxyUsernameText = new TextField();
        proxyUsernameText.textProperty().bindBidirectional(model.proxyUsernameProperty());
        proxyUsernameText.disableProperty().bind(manualProxyDisabledBinding);

        proxyPasswordText = Controls.passwordField();
        proxyPasswordText.textProperty().bindBidirectional(model.proxyPasswordProperty());
        proxyPasswordText.disableProperty().bind(manualProxyDisabledBinding);

        proxyExceptionsText = Controls.create(TextField::new);
        proxyExceptionsText.textProperty().bindBidirectional(model.proxyExceptionsProperty());
        proxyExceptionsText.disableProperty().bind(manualProxyDisabledBinding);

        Label proxyExceptionsExample = new Label(t(EXAMPLE) + ": *.domain.com, 192.168.*");

        Button checkProxyBtn = new Button(t(PREFERENCES_CHECK_CONNECTION));
        checkProxyBtn.setOnAction(e -> showCheckConnectionDialog());
        checkProxyBtn.disableProperty().bind(model.proxyCheckPendingProperty());

        // GRID

        GridPane grid = gridPane(20, 10, new Insets(10), "grid");

        grid.add(noProxyToggle, 0, 0, REMAINING, 1);
        grid.add(manualProxyToggle, 0, 1, REMAINING, 1);

        grid.add(gridLabel("URL", HPos.RIGHT, proxyHostText), 0, 2);
        grid.add(proxyUrlBox, 1, 2);

        grid.add(gridLabel(t(DesktopMessages.USERNAME), HPos.RIGHT, proxyUsernameText), 0, 3);
        grid.add(proxyUsernameText, 1, 3);

        grid.add(gridLabel(t(DesktopMessages.PASSWORD), HPos.RIGHT, proxyPasswordText), 0, 4);
        grid.add(proxyPasswordText.getParent(), 1, 4);

        grid.add(gridLabel(t(TOOLS_EXCEPTIONS), HPos.RIGHT, proxyExceptionsText), 0, 5);
        grid.add(proxyExceptionsText, 1, 5);
        grid.add(proxyExceptionsExample, 1, 6);

        grid.add(checkProxyBtn, 0, 7, REMAINING, 1);

        grid.getColumnConstraints().addAll(
                columnConstraints(120, Priority.NEVER), // property name
                columnConstraints(Priority.ALWAYS)              // property value
        );

        setText(t(PREFERENCES_PROXY));
        setContent(grid);
    }

    private void showCheckConnectionDialog() {
        TextInputDialog dialog = new TextInputDialog(defaultString(lastEnteredUrl, "http://"));
        dialog.setTitle(t(PREFERENCES_CHECK_PROXY_SETTINGS));
        dialog.setHeaderText(t(PREFERENCES_MSG_ENTER_ANY_URL_TO_CHECK));
        dialog.initOwner(view.getWindow());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(url -> {
            lastEnteredUrl = url;
            model.checkProxyCommand().execute(url);
        });
    }
}
