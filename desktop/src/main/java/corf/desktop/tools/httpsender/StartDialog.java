package corf.desktop.tools.httpsender;

import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import corf.base.desktop.controls.HorizontalForm;
import corf.base.desktop.controls.Message;
import corf.base.desktop.controls.ModalDialog;
import corf.base.desktop.controls.RevealablePasswordField;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.common.ReplacementCheckResult;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

final class StartDialog extends ModalDialog {

    private static final int DIALOG_WIDTH = 600;
    private static final int POLL_STEP = 100;

    VBox messageBox;
    ToggleSwitch enableAuthToggle;
    TextField usernameText;
    RevealablePasswordField passwordText;
    Slider pollTimeoutSlider;
    Label pollTimeoutLabel;
    Button runBtn;
    Button closeBtn;

    private final HttpSenderViewModel model;

    @SuppressWarnings("NullAway.Init")
    public StartDialog(HttpSenderViewModel model) {
        super();

        this.model = model;

        setContent(createContent());
        init();
    }

    private Content createContent() {
        messageBox = new VBox();

        // == AUTH ==

        enableAuthToggle = new ToggleSwitch(t(DM.HTTP_SENDER_ENABLE_BASIC_AUTHENTICATION));
        enableAuthToggle.getStyleClass().add(TEXT_CAPTION);

        usernameText = new TextField();
        usernameText.setPrefWidth(300);

        passwordText = new RevealablePasswordField();
        passwordText.setPrefWidth(300);

        var authBox = new HorizontalForm();
        authBox.setHgap(Recommends.FORM_HGAP);
        authBox.setVgap(Recommends.FORM_VGAP);
        authBox.add(enableAuthToggle, 0, 0, GridPane.REMAINING, 1);
        authBox.add(t(DM.USERNAME), true, usernameText);
        authBox.add(t(DM.PASSWORD), true, passwordText);

        // == TIMEOUT ==

        var pollTimeoutTitleLabel = new Label(t(DM.HTTP_SENDER_TIMEOUT_BETWEEN_REQUESTS));
        pollTimeoutTitleLabel.getStyleClass().add(TEXT_CAPTION);

        pollTimeoutLabel = new Label();
        pollTimeoutLabel.getStyleClass().addAll(TEXT_SUBTLE, TEXT_SMALL);

        pollTimeoutSlider = new Slider(POLL_STEP, 30_000, POLL_STEP);
        pollTimeoutSlider.setBlockIncrement(POLL_STEP);

        var pollTitleBox = new HBox(pollTimeoutTitleLabel, new Spacer(), pollTimeoutLabel);
        pollTitleBox.setAlignment(Pos.CENTER_LEFT);

        var pollTimeoutBox = new VBox(Recommends.FORM_VGAP, pollTitleBox, pollTimeoutSlider);

        // == BODY ==

        var body = new VBox(messageBox, authBox, pollTimeoutBox);
        body.setSpacing(Recommends.CONTENT_SPACING);
        body.setPrefWidth(DIALOG_WIDTH);

        // == FOOTER ==

        runBtn = new Button(t(DM.ACTION_RUN));
        runBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(Recommends.FORM_INLINE_SPACING, new Spacer(), runBtn, closeBtn);

        return Content.create(t(DM.CONFIRMATION), body, footer);
    }

    @SuppressWarnings("SimplifiableConditionalExpression")
    private void init() {
        var valid = Bindings.createBooleanBinding(
                () -> enableAuthToggle.isSelected()
                        ? StringUtils.isNotBlank(usernameText.getText()) && StringUtils.isNotBlank(passwordText.getText())
                        : true,
                usernameText.textProperty(),
                passwordText.textProperty(),
                enableAuthToggle.selectedProperty()
        );

        enableAuthToggle.selectedProperty().bindBidirectional(model.useBasicAuthProperty());

        usernameText.textProperty().bindBidirectional(model.usernameProperty());
        usernameText.disableProperty().bind(enableAuthToggle.selectedProperty().not());

        passwordText.textProperty().bindBidirectional(model.passwordProperty());
        passwordText.disableProperty().bind(enableAuthToggle.selectedProperty().not());

        pollTimeoutSlider.valueProperty().bindBidirectional(model.pollTimeoutProperty());
        pollTimeoutSlider.valueProperty().addListener((obs, old, val) -> {
            var roundedVal = Math.round(val.doubleValue() / POLL_STEP) * POLL_STEP;
            model.pollTimeoutProperty().setValue(roundedVal);
            pollTimeoutLabel.setText(String.format("%.1fs", roundedVal / 1000.0));
        });

        runBtn.setOnAction(e -> run());
        runBtn.disableProperty().bind(valid.not());

        closeBtn.setOnAction(e -> close());

        pollTimeoutSlider.setValue(1000);

        messageBox.managedProperty().bind(Bindings.size(messageBox.getChildren()).greaterThan(0));
    }

    void prepare() {
        messageBox.getChildren().clear();

        ReplacementCheckResult check = model.validate();
        if (!check.passed()) {
            var errors = check.getWarnings().stream()
                    .map(s -> "• " + s)
                    .collect(Collectors.joining("\n"));
            var message = new Message(Message.Type.WARNING, t(DM.WARNING), errors);
            messageBox.getChildren().setAll(message);

            Styles.addStyleClass(runBtn, DANGER, SUCCESS);
        } else {
            Styles.addStyleClass(runBtn, SUCCESS, DANGER);
        }
    }

    private void run() {
        close();
        model.startCommand().run();
    }
}
