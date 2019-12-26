package corf.desktop.layout.preferences;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.util.IntegerStringConverter;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.net.Scheme;
import corf.base.preferences.internal.ManualProxy;
import corf.base.desktop.controls.RevealablePasswordField;
import corf.desktop.i18n.DM;
import corf.base.desktop.controls.HorizontalForm;
import corf.desktop.layout.Recommends;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

public class ProxyPreferencesPane extends VBox {

    ToggleSwitch activeToggle;
    ChoiceBox<Scheme> schemeChoice;
    TextField hostText;
    Spinner<Integer> portSpinner;
    TextField usernameText;
    RevealablePasswordField passwordText;
    TextField exceptionsText;
    CustomTextField checkText;
    Button checkBtn;

    private final PreferencesDialogViewModel model;

    public ProxyPreferencesPane(PreferencesDialogViewModel model) {
        this.model = model;

        createView();
        init();
    }

    private void createView() {
        var titleLabel = new Label(t(DM.PROXY));
        titleLabel.getStyleClass().add(TEXT_CAPTION);

        activeToggle = new ToggleSwitch();

        var titleBox = new HBox(20, titleLabel, activeToggle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        schemeChoice = new ChoiceBox<>(FXCollections.observableArrayList(ManualProxy.SUPPORTED_SCHEMES));
        schemeChoice.getStyleClass().add(LEFT_PILL);
        schemeChoice.setPrefWidth(100);

        hostText = new TextField();
        hostText.getStyleClass().add(RIGHT_PILL);
        HBox.setHgrow(hostText, Priority.ALWAYS);

        var hostUrlBox = new HBox(schemeChoice, hostText);
        hostUrlBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hostUrlBox, Priority.ALWAYS);

        portSpinner = new Spinner<>(0, 65535, 3128);
        portSpinner.setPrefWidth(100);
        portSpinner.setEditable(true);
        IntegerStringConverter.createFor(portSpinner);

        var proxyUrlBox = new HBox(5);
        proxyUrlBox.getChildren().setAll(hostUrlBox, new Label(t(DM.PORT)), portSpinner);
        proxyUrlBox.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(proxyUrlBox, Priority.ALWAYS);

        usernameText = new TextField();
        GridPane.setHgrow(usernameText, Priority.ALWAYS);

        passwordText = new RevealablePasswordField();
        GridPane.setHgrow(passwordText, Priority.ALWAYS);

        exceptionsText = new CustomTextField();
        exceptionsText.setPromptText("*.domain.com, 192.168.*");
        GridPane.setHgrow(exceptionsText, Priority.ALWAYS);

        checkText = new CustomTextField();
        checkText.setLeft(new FontIcon(Material2OutlinedMZ.PUBLIC));
        checkText.setPromptText("http://example.com");
        checkText.getStyleClass().add(LEFT_PILL);
        HBox.setHgrow(checkText, Priority.ALWAYS);

        checkBtn = new Button(t(DM.PREFS_CHECK_CONNECTION));
        checkBtn.getStyleClass().addAll(SUCCESS, RIGHT_PILL);

        var checkGroupBox = new HBox(checkText, checkBtn);
        checkGroupBox.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(checkGroupBox, Priority.ALWAYS);

        // == FORM ==

        var form = new HorizontalForm();
        form.setHgap(Recommends.FORM_HGAP);
        form.setVgap(Recommends.FORM_VGAP);

        form.add("URL", proxyUrlBox);
        form.add(t(DM.USERNAME), usernameText);
        form.add(t(DM.PASSWORD), passwordText);
        form.add(t(DM.PREFS_PROXY_EXCEPTIONS), exceptionsText);
        form.add(checkGroupBox, 1, 4);

        setSpacing(Recommends.FORM_VGAP);
        getChildren().setAll(titleBox, form);
    }

    private void init() {
        var proxyOffBinding = activeToggle.selectedProperty().not();
        var proxyCheckBlockBinding = Bindings.not(model.checkProxyCommand().executableProperty());

        activeToggle.selectedProperty().bindBidirectional(model.proxyEnabledProperty());

        schemeChoice.valueProperty().bindBidirectional(model.proxySchemeProperty());
        schemeChoice.disableProperty().bind(proxyOffBinding);

        hostText.textProperty().bindBidirectional(model.proxyHostProperty());
        hostText.disableProperty().bind(proxyOffBinding);

        portSpinner.getValueFactory().valueProperty().bindBidirectional(model.proxyPortProperty());
        portSpinner.disableProperty().bind(proxyOffBinding);

        usernameText.textProperty().bindBidirectional(model.proxyUsernameProperty());
        usernameText.disableProperty().bind(proxyOffBinding);

        passwordText.textProperty().bindBidirectional(model.proxyPasswordProperty());
        passwordText.disableProperty().bind(proxyOffBinding);

        exceptionsText.textProperty().bindBidirectional(model.proxyExceptionsProperty());
        exceptionsText.disableProperty().bind(proxyOffBinding);

        checkText.textProperty().bindBidirectional(model.proxyCheckUrlProperty());
        checkText.disableProperty().bind(Bindings.or(proxyOffBinding, model.proxyCheckPendingProperty()));

        checkBtn.setOnAction(e -> model.checkProxyCommand().run());
        checkBtn.disableProperty().bind(Bindings.or(proxyOffBinding, proxyCheckBlockBinding));
    }
}
