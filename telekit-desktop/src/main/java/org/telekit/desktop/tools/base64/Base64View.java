package org.telekit.desktop.tools.base64;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.telekit.base.desktop.Component;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.controls.util.Controls;

import javax.inject.Singleton;

import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.MODE;
import static org.telekit.controls.i18n.ControlsMessages.TEXT;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.desktop.i18n.DesktopMessages.BASE64_AS_TEXT;
import static org.telekit.desktop.i18n.DesktopMessages.BASE64_LINE_BY_LINE;
import static org.telekit.desktop.tools.base64.ConvertTask.*;

@Singleton
public final class Base64View extends GridPane implements Initializable, Component {

    TextArea origText;

    Button encodeBtn;
    Button decodeBtn;
    ToggleGroup modeToggle;
    RadioButton lineModeCheck;
    RadioButton textModeCheck;

    ComboBox<CodecType> encoderChoice;
    TextArea base64Text;

    public Base64View() {
        createView();
    }

    private void createView() {
        // LEFT

        origText = Controls.create(TextArea::new, "monospace");

        // CENTER

        encodeBtn = new Button(">>");
        encodeBtn.setMinWidth(80);
        encodeBtn.setOnAction(e -> encode());

        decodeBtn = new Button("<<");
        decodeBtn.setMinWidth(80);
        decodeBtn.setOnAction(e -> decode());

        Label modeLabel = new Label(t(MODE));
        modeLabel.setPadding(new Insets(20, 0, 0, 0));

        modeToggle = new ToggleGroup();

        lineModeCheck = new RadioButton(t(BASE64_LINE_BY_LINE));
        lineModeCheck.setToggleGroup(modeToggle);
        lineModeCheck.setUserData(MODE_BY_LINE);

        textModeCheck = new RadioButton(t(BASE64_AS_TEXT));
        textModeCheck.setToggleGroup(modeToggle);
        textModeCheck.setUserData(MODE_AS_TEXT);

        VBox centerBox = vbox(5, Pos.CENTER_LEFT, Insets.EMPTY);
        centerBox.getChildren().setAll(
                encodeBtn,
                decodeBtn,
                modeLabel,
                lineModeCheck,
                textModeCheck
        );

        // RIGHT

        encoderChoice = new ComboBox<>(observableArrayList(CodecType.values()));

        HBox resultBox = hbox(10, Pos.CENTER_LEFT, Insets.EMPTY);
        resultBox.getChildren().setAll(
                new Label("Base64"),
                encoderChoice
        );

        base64Text = Controls.create(TextArea::new, "monospace");

        // GRID

        add(new Label(t(TEXT)), 0, 0);
        add(origText, 0, 1);

        add(centerBox, 1, 0, 1, REMAINING);

        add(resultBox, 2, 0);
        add(base64Text, 2, 1);

        getRowConstraints().addAll(VGROW_NEVER, VGROW_ALWAYS);
        getColumnConstraints().addAll(HGROW_ALWAYS, HGROW_NEVER, HGROW_ALWAYS);

        setVgap(5);
        setHgap(10);
        setPadding(new Insets(10));
        setId("base64-encoder");
    }

    @Override
    public void initialize() {
        lineModeCheck.setSelected(true);
        encoderChoice.getSelectionModel().select(CodecType.BASIC);
    }

    public void encode() {
        int mode = (int) modeToggle.getSelectedToggle().getUserData();
        String origStr = trim(origText.getText());
        CodecType codec = encoderChoice.getSelectionModel().getSelectedItem();
        if (isEmpty(origStr) || codec == null) { return; }

        ConvertTask task = createTask(origStr, OPERATION_ENCODE, mode, codec);
        task.setOnSucceeded(event -> Platform.runLater(() -> base64Text.setText(task.getValue())));
        new Thread(task).start();
    }

    public void decode() {
        int mode = (int) modeToggle.getSelectedToggle().getUserData();
        String base64Str = trim(base64Text.getText());
        CodecType codec = encoderChoice.getSelectionModel().getSelectedItem();
        if (isEmpty(base64Str) || codec == null) { return; }

        ConvertTask task = createTask(base64Str, OPERATION_DECODE, mode, codec);
        task.setOnSucceeded(event -> origText.setText(task.getValue()));

        new Thread(task).start();
    }

    private ConvertTask createTask(String sourceText, int operation, int mode, CodecType codec) {
        ConvertTask task = new ConvertTask(sourceText, operation, mode, codec);
        task.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            if (exception != null) { DefaultEventBus.getInstance().publish(Notification.error(exception)); }
        });
        return task;
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public Node getPrimaryFocusNode() { return origText; }
}
