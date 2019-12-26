package corf.desktop.tools.base64;

import atlantafx.base.controls.Popover;
import atlantafx.base.controls.Spacer;
import backbonefx.di.Initializable;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.common.Lazy;
import corf.base.desktop.Component;
import corf.base.desktop.Focusable;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.concurrent.ExecutorService;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

public final class Base64ConverterView extends VBox implements Component<Base64ConverterView>,
        Initializable,
        Focusable {

    private static final int TOOL_WIDTH = 800;

    TextArea origText;
    TextArea base64Text;
    Button encodeBtn;
    Button decodeBtn;
    Button settingsBtn;
    Lazy<Popover> settingsDialog = new Lazy<>(this::createSettingsDialog);
    ComboBox<Codec> encoderChoice;
    CheckBox lineModeCheck;

    private final ExecutorService executorService;

    @Inject
    public Base64ConverterView(ExecutorService executorService) {
        super();

        this.executorService = executorService;

        createView();
    }

    private void createView() {
        origText = new TextArea();
        origText.setPromptText("Text");
        origText.setPrefHeight(300);
        origText.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        var textLabel = new Label(t(DM.TEXT));
        textLabel.getStyleClass().addAll(TEXT_BOLD, TEXT_MUTED);
        AnchorPane.setTopAnchor(textLabel, Recommends.SUB_ITEM_MARGIN * 1.0);
        AnchorPane.setLeftAnchor(textLabel, 0d);

        base64Text = new TextArea();
        base64Text.setPromptText(t(DM.BASE64));
        base64Text.setPrefHeight(300);
        base64Text.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        var base64Label = new Label(DM.BASE64);
        base64Label.getStyleClass().addAll(TEXT_BOLD, TEXT_MUTED);

        var base64Box = new VBox(Recommends.SUB_ITEM_MARGIN, base64Text, base64Label);

        // == MIDDLE ==

        encodeBtn = new Button(t(DM.ACTION_ENCODE), new FontIcon(Material2MZ.SOUTH));
        encodeBtn.getStyleClass().addAll(LEFT_PILL, ACCENT);
        encodeBtn.setMinWidth(80);

        decodeBtn = new Button(t(DM.ACTION_DECODE), new FontIcon(Material2MZ.NORTH));
        decodeBtn.getStyleClass().addAll(RIGHT_PILL, ACCENT);
        decodeBtn.setContentDisplay(ContentDisplay.RIGHT);
        decodeBtn.setMinWidth(80);

        settingsBtn = new Button(t(DM.OPTIONS), new FontIcon(Material2OutlinedMZ.TUNE));
        settingsBtn.getStyleClass().addAll(FLAT);

        encoderChoice = new ComboBox<>(FXCollections.observableArrayList(Codec.values()));
        encoderChoice.setPrefWidth(150);

        lineModeCheck = new CheckBox(t(DM.BASE64_LINE_BY_LINE));

        var actionsBox = new HBox(
                encodeBtn,
                decodeBtn,
                new Spacer(Recommends.FORM_INLINE_SPACING),
                settingsBtn
        );
        actionsBox.setPadding(new Insets(10));
        actionsBox.setAlignment(Pos.CENTER);
        AnchorPane.setLeftAnchor(actionsBox, 0d);
        AnchorPane.setRightAnchor(actionsBox, 0d);

        var middlePane = new AnchorPane();
        middlePane.getChildren().setAll(actionsBox, textLabel);

        getChildren().setAll(origText, middlePane, base64Box);
        setMinWidth(TOOL_WIDTH);
        setMaxWidth(TOOL_WIDTH);
        setAlignment(Pos.TOP_LEFT);
        setPadding(Recommends.TOOL_PADDING);
        setId("base64-converter");
    }

    @Override
    public void init() {
        encodeBtn.setOnAction(e -> encode());

        decodeBtn.setOnAction(e -> decode());

        settingsBtn.setOnAction(e -> {
            var popover = settingsDialog.get();
            popover.show(settingsBtn);
        });

        lineModeCheck.setSelected(true);
        encoderChoice.getSelectionModel().select(Codec.BASIC);
    }

    @Override
    public Base64ConverterView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public Node getPrimaryFocusNode() {
        return origText;
    }

    private Popover createSettingsDialog() {
        var content = new GridPane();
        content.setHgap(Recommends.FORM_HGAP);
        content.setVgap(Recommends.FORM_VGAP);
        content.setPadding(new Insets(10));

        content.add(new Label(t(DM.BASE64_ALGORITHM)), 0, 0);
        content.add(encoderChoice, 1, 0);
        content.add(lineModeCheck, 0, 1, GridPane.REMAINING, 1);

        var popover = new Popover(content);
        popover.setArrowLocation(Popover.ArrowLocation.TOP_CENTER);
        popover.setDetachable(false);

        return popover;
    }

    public void encode() {
        String text = StringUtils.trim(origText.getText());
        Codec codec = encoderChoice.getSelectionModel().getSelectedItem();
        boolean lineByLine = lineModeCheck.isSelected();

        if (StringUtils.isEmpty(text) || codec == null) { return; }

        ConvertTask task = ConvertTask.forEncode(text, codec, lineByLine);
        task.setOnSucceeded(event -> base64Text.setText(task.getValue()));

        executorService.execute(task);
    }

    public void decode() {
        String base64 = StringUtils.trim(base64Text.getText());
        Codec codec = encoderChoice.getSelectionModel().getSelectedItem();
        boolean lineByLine = lineModeCheck.isSelected();

        if (StringUtils.isEmpty(base64) || codec == null) { return; }

        ConvertTask task = ConvertTask.forDecode(base64, codec, lineByLine);
        task.setOnSucceeded(event -> origText.setText(task.getValue()));

        executorService.execute(task);
    }
}
