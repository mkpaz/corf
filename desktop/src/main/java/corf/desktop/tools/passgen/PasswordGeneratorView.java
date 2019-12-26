package corf.desktop.tools.passgen;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Spacer;
import backbonefx.di.Initializable;
import backbonefx.event.DefaultEventBus;
import backbonefx.event.EventBus;
import corf.base.common.Lazy;
import corf.base.desktop.*;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import jakarta.inject.Inject;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.scoring.Result;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

public final class PasswordGeneratorView extends VBox
        implements Component<PasswordGeneratorView>, Initializable, Focusable {

    private static final int TOOL_WIDTH = 800;

    ComboBox<String> typeChoice;
    CustomTextField passwordText;
    FontIcon updateBtn;
    FontIcon copyBtn;
    Button bulkBtn;
    Label qualityLabel;
    Label entropyLabel;
    ProgressBar strengthBar;
    VBox generatorPane;

    private final StringProperty activeGenerator = new SimpleStringProperty();
    private final Map<String, Generator> generators = new LinkedHashMap<>();
    private final EventBus eventBus = new DefaultEventBus();
    private final Nbvcxz nbvcxz = new Nbvcxz();
    private final Overlay overlay;
    private final Lazy<BulkDialog> bulkDialog;

    @Inject
    @SuppressWarnings("NullAway.Init")
    public PasswordGeneratorView(Overlay overlay, ExecutorService executorService) {
        super();

        this.overlay = overlay;

        generators.put(RandomCharactersGenerator.NAME, new RandomCharactersGenerator(eventBus));
        generators.put(PassphraseGenerator.NAME, new PassphraseGenerator(eventBus));
        generators.put(HexGenerator.NAME, new HexGenerator(eventBus));
        generators.put(KatakanaGenerator.NAME, new KatakanaGenerator(eventBus));

        bulkDialog = new Lazy<>(() -> {
            var dialog = new BulkDialog(executorService);
            dialog.setOnCloseRequest(overlay::hide);
            return dialog;
        });

        createView();
    }

    private void createView() {
        var titleLabel = new Label(t(DM.PASSGEN_PASSWORD));
        titleLabel.getStyleClass().add(TITLE_4);

        typeChoice = new ComboBox<>();
        typeChoice.getStyleClass().add(LEFT_PILL);
        typeChoice.setPrefWidth(200);

        updateBtn = new FontIcon(Material2OutlinedMZ.REFRESH);
        updateBtn.setCursor(Cursor.HAND);

        copyBtn = new FontIcon(Material2OutlinedAL.CONTENT_COPY);
        copyBtn.setCursor(Cursor.HAND);

        var inlineBox = new HBox(10, updateBtn, copyBtn);
        inlineBox.setAlignment(Pos.CENTER_LEFT);

        passwordText = new CustomTextField();
        passwordText.setEditable(false);
        passwordText.getStyleClass().addAll(ExtraStyles.MONOSPACE, CENTER_PILL);
        passwordText.setRight(inlineBox);
        passwordText.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(passwordText, Priority.ALWAYS);

        bulkBtn = new Button(t(DM.MORE), new FontIcon(Material2OutlinedAL.ADD_BOX));
        bulkBtn.getStyleClass().addAll(SUCCESS, RIGHT_PILL);

        var passwordGroupBox = new HBox(typeChoice, passwordText, bulkBtn);
        passwordGroupBox.setAlignment(Pos.CENTER_LEFT);

        strengthBar = new ProgressBar(0);
        strengthBar.getStyleClass().add(SMALL);
        strengthBar.setMaxWidth(Double.MAX_VALUE);

        qualityLabel = new Label();
        qualityLabel.setGraphic(new FontIcon(Material2OutlinedMZ.SECURITY));
        qualityLabel.getStyleClass().addAll(TEXT_SUBTLE, SMALL);

        entropyLabel = new Label();
        entropyLabel.getStyleClass().addAll(TEXT_SUBTLE, SMALL);

        var qualityBox = new HBox(qualityLabel, new Spacer(), entropyLabel);
        qualityBox.setAlignment(Pos.CENTER_LEFT);

        generatorPane = new VBox();
        generatorPane.setFillWidth(true);

        setMinWidth(TOOL_WIDTH);
        setMaxWidth(TOOL_WIDTH);
        setAlignment(Pos.TOP_LEFT);
        setPadding(Recommends.TOOL_PADDING);
        setSpacing(40);
        getChildren().setAll(
                new VBox(
                        Recommends.CAPTION_MARGIN,
                        titleLabel,
                        passwordGroupBox,
                        strengthBar,
                        qualityBox
                ),
                generatorPane
        );
        setId("password-generator");
    }

    @Override
    public void init() {
        typeChoice.setItems(FXCollections.observableArrayList(generators.keySet()));

        activeGenerator.bind(typeChoice.valueProperty());
        activeGenerator.addListener((obs, old, val) -> {
            if (val == null) { return; }

            Generator generator = generators.get(val);
            if (generator == null) { return; }

            generatorPane.getChildren().setAll(generator.getView());
            passwordText.setText(generator.generate());
        });

        strengthBar.progressProperty().addListener((obs, old, val) -> {
            var progress = val != null ? val.doubleValue() : 0;

            if (progress < 0.25) {
                strengthBar.setStyle("-color-progress-bar-fill:-color-danger-emphasis;");
            } else if (progress < 0.5) {
                strengthBar.setStyle("-color-progress-bar-fill:-color-warning-emphasis;");
            } else if (progress < 0.75) {
                strengthBar.setStyle("-color-progress-bar-fill:-color-success-muted;");
            } else {
                strengthBar.setStyle("-color-progress-bar-fill:-color-success-emphasis;");
            }
        });

        passwordText.textProperty().addListener((obs, old, val) -> {
            double entropy = getEntropy(val);
            strengthBar.setProgress(entropy / 100);
            entropyLabel.setText(String.format(t(DM.PASSGEN_ENTROPY) + ": %.2f bit", entropy));
            if (entropy < 25) {
                qualityLabel.setText(t(DM.PASSGEN_POOR));
            } else if (entropy < 50) {
                qualityLabel.setText(t(DM.PASSGEN_WEAK));
            } else if (entropy < 75) {
                qualityLabel.setText(t(DM.PASSGEN_MODERATE));
            } else if (entropy < 100) {
                qualityLabel.setText(t(DM.PASSGEN_GOOD));
            } else {
                qualityLabel.setText(t(DM.PASSGEN_EXCELLENT));
            }
        });

        eventBus.subscribe(ChangeEvent.class, e -> updatePassword());

        updateBtn.setOnMouseClicked(e -> {
            createIconAnimation(updateBtn).playFromStart();
            updatePassword();
        });

        copyBtn.setOnMouseClicked(e -> {
            createIconAnimation(copyBtn).playFromStart();
            OS.setClipboard(passwordText.getText());
        });

        bulkBtn.setOnAction(e -> {
            var generator = getActiveGenerator();
            if (generator != null) {
                var dialog = bulkDialog.get();
                dialog.setGenerator(generator);
                overlay.show(dialog, Pos.TOP_CENTER, Recommends.MODAL_WINDOW_MARGIN);
            }
        });

        // must be the last string after all listeners have been set
        typeChoice.getSelectionModel().selectFirst();
    }

    @Override
    public PasswordGeneratorView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public Node getPrimaryFocusNode() {
        return passwordText;
    }

    private @Nullable Generator getActiveGenerator() {
        if (activeGenerator.get() == null) { return null; }
        return generators.get(activeGenerator.get());
    }

    private void updatePassword() {
        Generator generator = getActiveGenerator();
        if (generator != null) {
            var password = generator.generate();
            passwordText.setText(password);
        }
    }

    private double getEntropy(@Nullable String password) {
        if (StringUtils.isBlank(password)) { return 0; }
        Result result = nbvcxz.estimate(password);
        return result.getEntropy();
    }

    private Timeline createIconAnimation(FontIcon icon) {
        var animation = Animations.fadeIn(icon, Duration.millis(50));
        animation.setOnFinished(x -> icon.setDisable(false));
        icon.setDisable(true);
        return animation;
    }
}
