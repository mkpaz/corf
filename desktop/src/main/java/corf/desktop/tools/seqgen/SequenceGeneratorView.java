package corf.desktop.tools.seqgen;

import atlantafx.base.controls.Spacer;
import backbonefx.di.Initializable;
import corf.base.Env;
import corf.base.collection.SequenceGenerator;
import corf.base.collection.SequenceGenerator.Rule;
import corf.base.desktop.*;
import corf.base.desktop.controls.StringListView;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.base.text.PlaceholderReplacer;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

@SuppressWarnings("UnnecessaryLambda")
public final class SequenceGeneratorView extends VBox
        implements Component<SequenceGeneratorView>, Initializable, Focusable {

    private static final int TOOL_WIDTH = 800;
    private static final int MAX_TOTAL_RESULT_SIZE = 100_000;
    private static final BiFunction<String, Double, String> OUTPUT_CONVERTER = (id, v) -> String.valueOf(v.longValue());
    private static final String EXPORT_FILE_NAME = "sequence.txt";

    TextField patternText;
    Label lineCountLabel;
    StringListView generatedList;
    Button exportBtn;
    Button generateBtn;

    RuleInput inputA;
    RuleInput inputB;
    RuleInput inputC;
    RuleInput inputD;

    private final ExecutorService executorService;

    @Inject
    @SuppressWarnings("NullAway.Init")
    public SequenceGeneratorView(ExecutorService executorService) {
        super();

        this.executorService = executorService;

        createView();
    }

    private void createView() {
        setMinWidth(TOOL_WIDTH);
        setMaxWidth(TOOL_WIDTH);
        setAlignment(Pos.TOP_LEFT);
        setPadding(Recommends.TOOL_PADDING);
        setSpacing(40);
        getChildren().setAll(
                createPatternPane(),
                createResultPane()
        );
        setId("sequence-generator");
    }

    private VBox createPatternPane() {
        var titleLabel = new Label(t(DM.PATTERN));
        titleLabel.getStyleClass().addAll(TITLE_4);

        patternText = new TextField();
        patternText.getStyleClass().addAll(ExtraStyles.MONOSPACE, LEFT_PILL);
        patternText.setAlignment(Pos.CENTER);
        HBox.setHgrow(patternText, Priority.ALWAYS);

        generateBtn = new Button(t(DM.ACTION_GENERATE), new FontIcon(Material2MZ.SHUFFLE));
        generateBtn.getStyleClass().addAll(RIGHT_PILL);
        generateBtn.setDefaultButton(true);

        var patternBox = new HBox(generateBtn);
        patternBox.setAlignment(Pos.CENTER);
        patternBox.getChildren().setAll(patternText, generateBtn);

        inputA = new RuleInput("A");
        inputB = new RuleInput("B");
        inputC = new RuleInput("C");
        inputD = new RuleInput("D");

        var itemGrid = new GridPane();
        itemGrid.setHgap(10);
        itemGrid.setVgap(10);

        GridPane.setHgrow(inputA.startSpinner, Priority.ALWAYS);
        GridPane.setHgrow(inputB.startSpinner, Priority.ALWAYS);
        GridPane.setHgrow(inputC.startSpinner, Priority.ALWAYS);
        GridPane.setHgrow(inputD.startSpinner, Priority.ALWAYS);

        itemGrid.add(createGridLabel(t(DM.START)), 1, 0);
        itemGrid.add(createGridLabel(t(DM.STEP)), 2, 0);
        itemGrid.add(createGridLabel(t(DM.COUNT)), 3, 0);

        itemGrid.add(inputA.enabledCheck, 0, 1);
        itemGrid.add(inputA.startSpinner, 1, 1);
        itemGrid.add(inputA.stepSpinner, 2, 1);
        itemGrid.add(inputA.countSpinner, 3, 1);

        itemGrid.add(inputB.enabledCheck, 0, 2);
        itemGrid.add(inputB.startSpinner, 1, 2);
        itemGrid.add(inputB.stepSpinner, 2, 2);
        itemGrid.add(inputB.countSpinner, 3, 2);

        itemGrid.add(inputC.enabledCheck, 0, 3);
        itemGrid.add(inputC.startSpinner, 1, 3);
        itemGrid.add(inputC.stepSpinner, 2, 3);
        itemGrid.add(inputC.countSpinner, 3, 3);

        itemGrid.add(inputD.enabledCheck, 0, 4);
        itemGrid.add(inputD.startSpinner, 1, 4);
        itemGrid.add(inputD.stepSpinner, 2, 4);
        itemGrid.add(inputD.countSpinner, 3, 4);

        itemGrid.getColumnConstraints().setAll(
                new ColumnConstraints(60, -1, -1, Priority.NEVER, HPos.LEFT, false),
                new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true),
                new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, false),
                new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, false)
        );

        return new VBox(
                Recommends.CAPTION_MARGIN,
                titleLabel,
                patternBox,
                new Spacer(5, Orientation.VERTICAL),
                itemGrid
        );
    }

    private VBox createResultPane() {
        var titleLabel = new Label(t(DM.RESULT));
        titleLabel.getStyleClass().add(TITLE_4);

        lineCountLabel = new Label("0");
        lineCountLabel.getStyleClass().addAll(SMALL, TEXT_MUTED);

        var lineCountSuffixLabel = new Label(t(DM.TPL_LINES.toLowerCase()));
        lineCountSuffixLabel.getStyleClass().addAll(SMALL, TEXT_MUTED);

        var headerBox = new HBox(5);
        headerBox.setAlignment(Pos.BASELINE_LEFT);
        headerBox.getChildren().setAll(
                titleLabel,
                new Spacer(),
                lineCountLabel,
                lineCountSuffixLabel
        );

        generatedList = new StringListView();
        generatedList.getStyleClass().addAll(ExtraStyles.MONOSPACE, STRIPED, DENSE);
        generatedList.setPrefHeight(200);

        exportBtn = new Button(t(DM.ACTION_EXPORT), new FontIcon(Material2OutlinedMZ.SAVE_ALT));
        exportBtn.getStyleClass().addAll(FLAT);

        var exportBox = new HBox(exportBtn);
        exportBox.setAlignment(Pos.TOP_RIGHT);

        return new VBox(
                Recommends.CAPTION_MARGIN,
                headerBox,
                new VBox(Recommends.SUB_ITEM_MARGIN, generatedList, exportBox)
        );
    }

    @Override
    public void init() {
        lineCountLabel.textProperty().bind(generatedList.sizeProperty().asString());

        generateBtn.setOnAction(e -> generate());
        generateBtn.disableProperty().bind(Observables.or(
                Observables.isBlank(patternText.textProperty()),
                Bindings.not(Observables.or(
                        inputA.enabledProperty(),
                        inputB.enabledProperty(),
                        inputC.enabledProperty(),
                        inputD.enabledProperty()
                ))
        ));

        exportBtn.setOnAction(e -> export());
        exportBtn.disableProperty().bind(generatedList.sizeProperty().isEqualTo(0));

        // set initial data
        patternText.setText("${A}/${B}");
        inputA.setEnabled(true);
        inputB.setEnabled(true);
    }

    @Override
    public SequenceGeneratorView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public Node getPrimaryFocusNode() {
        return patternText;
    }

    private Label createGridLabel(String text) {
        var label = new Label(text);
        label.getStyleClass().addAll(TEXT_BOLD, TEXT_MUTED);
        return label;
    }

    private void generate() {
        final var pattern = patternText.getText();
        if (StringUtils.isBlank(pattern)) { return; }

        final var rules = new ArrayList<Rule<String>>();
        if (inputA.isEnabled()) { rules.add(inputA.createRule()); }
        if (inputB.isEnabled()) { rules.add(inputB.createRule()); }
        if (inputC.isEnabled()) { rules.add(inputC.createRule()); }
        if (inputD.isEnabled()) { rules.add(inputD.createRule()); }
        if (rules.isEmpty()) { return; }

        if (SequenceGenerator.getExpectedSize(rules) > MAX_TOTAL_RESULT_SIZE) {
            Events.fire(Notification.warning(t(DM.SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT, MAX_TOTAL_RESULT_SIZE)));
            return;
        }

        Async.with(() -> {
                    var generator = new SequenceGenerator<>(rules, OUTPUT_CONVERTER);
                    return generator.generate().stream()
                            .map(item -> PlaceholderReplacer.replace(pattern, item))
                            .collect(Collectors.toList());
                })
                .setOnSucceeded(result -> generatedList.setItems(FXCollections.observableArrayList(result)))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }

    private void export() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(Env.getLastVisitedDir())
                .initialFileName(EXPORT_FILE_NAME)
                .build()
                .showSaveDialog(getWindow());
        if (outputFile == null) { return; }

        Async.with(() -> {
                    try {
                        Files.writeString(outputFile.toPath(), String.join("\n", generatedList.getItems()));
                    } catch (Exception e) {
                        throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
                    }
                })
                .setOnSucceeded(nil -> Env.setLastVisitedDir(outputFile))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }
}
