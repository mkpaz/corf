package org.telekit.desktop.tools.seqgen;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.desktop.Component;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.service.impl.SequenceGenerator;
import org.telekit.base.service.impl.SequenceGenerator.Item;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_LEFT;
import static org.telekit.base.Env.TEXTAREA_ROW_LIMIT;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.util.FileUtils.getParentPath;
import static org.telekit.controls.i18n.ControlsMessages.START;
import static org.telekit.controls.i18n.ControlsMessages.STEP;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.desktop.i18n.DesktopMessages.*;

@Singleton
public final class SequenceGeneratorView extends GridPane implements Initializable, Component {

    static final int MAX_STEP = 1_000;
    static final int MAX_COUNT = 100_000;
    static final int MAX_TOTAL_RESULT_SIZE = 100_000;

    // this tool is supposed to be capable to generate sequences of phone numbers,
    // and standard length of E164 phone number is 11 (w/o plus sign)
    static final double MAX_START = 100_000_000_000L; // 12

    static final BiFunction<String, Double, String> OUTPUT_CONVERTER =
            (id, value) -> String.valueOf(value.longValue());

    TextField patternText;
    TextArea generatedText;
    Label lineCountLabel;
    Button generateBtn;
    Button exportBtn;

    ItemControlGroup groupA;
    ItemControlGroup groupB;
    ItemControlGroup groupC;
    ItemControlGroup groupD;

    private final ExecutorService threadPool;
    private Path lastVisitedDirectory;

    @Inject
    public SequenceGeneratorView(ExecutorService threadPool) {
        this.threadPool = threadPool;

        createView();
    }

    private void createView() {
        // LEFT

        patternText = new TextField();
        patternText.setAlignment(Pos.CENTER);

        Label paramsLabel = new Label(t(SEQGEN_PARAMETERS_HEADER));
        paramsLabel.setPadding(new Insets(10, 0, 0, 0));

        groupA = new ItemControlGroup("A");
        groupB = new ItemControlGroup("B");
        groupC = new ItemControlGroup("C");
        groupD = new ItemControlGroup("D");

        generateBtn = button(t(ACTION_GENERATE), Material2MZ.SHUFFLE, "large");
        generateBtn.setOnAction(e -> generate());

        HBox generateBox = hbox(0, TOP_LEFT, new Insets(10, 0, 0, 0));
        generateBox.getChildren().setAll(generateBtn);

        // RIGHT

        lineCountLabel = new Label("0");

        HBox rightHeaderBox = hbox(5, CENTER_LEFT, Insets.EMPTY);
        rightHeaderBox.getChildren().setAll(
                new Label(t(RESULT)),
                horizontalSpacer(),
                lineCountLabel,
                new Label(t(TOOLS_LINES))
        );

        generatedText = Controls.create(TextArea::new, "monospace");
        generatedText.setEditable(false);

        exportBtn = new Button(t(ACTION_EXPORT));
        exportBtn.setOnAction(e -> export());
        exportBtn.disableProperty().bind(BindUtils.isBlank(generatedText.textProperty()));

        HBox exportBox = hbox(0, Pos.CENTER_LEFT, Insets.EMPTY);
        exportBox.getChildren().setAll(
                new Label(t(TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN, TEXTAREA_ROW_LIMIT)),
                horizontalSpacer(),
                exportBtn)
        ;

        // GRID

        add(new Label(t(PATTERN)), 0, 0);
        add(patternText, 0, 1);
        add(paramsLabel, 0, 2);
        add(groupA, 0, 4);
        add(groupB, 0, 5);
        add(groupC, 0, 6);
        add(groupD, 0, 7);
        add(generateBox, 0, 8);

        add(rightHeaderBox, 1, 0);
        add(generatedText, 1, 1, 1, 8);
        add(exportBox, 1, 9);

        getRowConstraints().addAll(
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_ALWAYS,
                VGROW_NEVER
        );
        getColumnConstraints().addAll(
                new ColumnConstraints(400, 400, 400, Priority.NEVER, HPos.LEFT, true),
                HGROW_ALWAYS
        );

        setVgap(5);
        setHgap(10);
        setPadding(new Insets(10));
        setId("sequence-generator");
    }

    @Override
    public void initialize() {
        exportBtn.disableProperty().bind(BindUtils.isBlank(generatedText.textProperty()));

        generateBtn.disableProperty().bind(BindUtils.or(
                BindUtils.isBlank(patternText.textProperty()),
                Bindings.not(BindUtils.or(
                        groupA.enabledProperty(),
                        groupB.enabledProperty(),
                        groupC.enabledProperty(),
                        groupD.enabledProperty()
                ))
        ));

        // set initial data
        patternText.setText("%(A)");
        groupA.setEnabled(true);
    }

    private void generate() {
        String pattern = patternText.getText();

        List<Item<String>> items = new ArrayList<>();
        if (groupA.isEnabled()) { items.add(groupA.getItem()); }
        if (groupB.isEnabled()) { items.add(groupB.getItem()); }
        if (groupC.isEnabled()) { items.add(groupC.getItem()); }
        if (groupD.isEnabled()) { items.add(groupD.getItem()); }

        if (SequenceGenerator.expectedSize(items) > MAX_TOTAL_RESULT_SIZE) {
            DefaultEventBus.getInstance().publish(
                    Notification.warning(t(SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT, MAX_TOTAL_RESULT_SIZE))
            );
            return;
        }

        Promise.supplyAsync(() -> {
            SequenceGenerator<String, String> generator = new SequenceGenerator<>(items, OUTPUT_CONVERTER);
            List<Map<String, String>> sequence = generator.generate();
            StringBuilder sb = new StringBuilder();

            int index = 0;
            int mark = 0;

            for (Map<String, String> replacements : sequence) {
                sb.append(PlaceholderReplacer.format(pattern, replacements)).append("\n");
                if (index < TEXTAREA_ROW_LIMIT) { mark = sb.length(); }
                index++;
            }

            return new GeneratedData(sb.toString(), sequence.size(), mark);
        }).then(g -> {
            lineCountLabel.setText(String.valueOf(g.size()));
            generatedText.setText(g.text().substring(0, g.mark()));
            generatedText.setUserData(g.text());
        }).start(threadPool);
    }

    private void export() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(lastVisitedDirectory)
                .initialFileName(FileUtils.sanitizeFileName("sequence.txt"))
                .build()
                .showSaveDialog(getWindow());
        if (outputFile == null) { return; }

        lastVisitedDirectory = getParentPath(outputFile);
        Promise.runAsync(() -> {
            try {
                Files.writeString(outputFile.toPath(), (String) generatedText.getUserData());
            } catch (Exception e) {
                throw new TelekitException(t(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
            }
        }).start(threadPool);
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    ///////////////////////////////////////////////////////////////////////////

    static record GeneratedData(String text, int size, int mark) {}

    static class ItemControlGroup extends HBox {

        final String itemName;

        CheckBox enabledCheck;
        Spinner<Double> startSpinner;
        Spinner<Integer> stepSpinner;
        Spinner<Integer> countSpinner;

        public ItemControlGroup(String itemName) {
            this.itemName = itemName;

            enabledCheck = new CheckBox("%(" + itemName + ")");
            enabledCheck.setMinWidth(60);

            startSpinner = new Spinner<>(0, MAX_START, 0, 1);
            startSpinner.setEditable(true);
            startSpinner.disableProperty().bind(enabledCheck.selectedProperty().not());
            startSpinner.setPrefWidth(150);
            DoubleStringConverter.createFor(startSpinner);
            startSpinner.setTooltip(new Tooltip(t(START)));

            stepSpinner = new Spinner<>(-MAX_STEP, MAX_STEP, 1, 1);
            stepSpinner.disableProperty().bind(enabledCheck.selectedProperty().not());
            stepSpinner.setPrefWidth(100);
            stepSpinner.setTooltip(new Tooltip(t(STEP)));

            countSpinner = new Spinner<>(2, MAX_COUNT, 10, 1);
            countSpinner.setEditable(true);
            countSpinner.setPrefWidth(80);
            countSpinner.disableProperty().bind(enabledCheck.selectedProperty().not());
            IntegerStringConverter.createFor(countSpinner);
            countSpinner.setTooltip(new Tooltip(t(COUNT)));

            setSpacing(10);
            setAlignment(CENTER_LEFT);
            getChildren().setAll(enabledCheck, startSpinner, stepSpinner, countSpinner);
        }

        public boolean isEnabled() { return enabledCheck.isSelected(); }

        public void setEnabled(boolean value) { enabledCheck.setSelected(value); }

        public BooleanProperty enabledProperty() {
            return enabledCheck.selectedProperty();
        }

        public Item<String> getItem() {
            return new Item<>(itemName, startSpinner.getValue(), stepSpinner.getValue(), countSpinner.getValue());
        }
    }
}
