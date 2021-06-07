package org.telekit.desktop.tools.seqgen;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.event.ProgressIndicatorEvent;
import org.telekit.base.i18n.Messages;
import org.telekit.base.service.impl.SequenceGenerator;
import org.telekit.base.service.impl.SequenceGenerator.Item;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.controls.components.dialogs.Dialogs;
import org.telekit.controls.format.DoubleStringConverter;
import org.telekit.controls.format.IntegerStringConverter;
import org.telekit.controls.util.BindUtils;
import org.telekit.desktop.domain.ExceptionCaughtEvent;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.base.Env.TEXTAREA_ROW_LIMIT;
import static org.telekit.desktop.MessageKeys.*;

@FxmlPath("/org/telekit/desktop/tools/seqgen/_root.fxml")
public class SequenceGeneratorController implements Component {

    // item ID must be identical to the corresponding pattern placeholder
    private static final String ITEM_A = "A";
    private static final String ITEM_B = "B";
    private static final String ITEM_C = "C";
    private static final String ITEM_D = "D";

    private static final int MAX_STEP = 1_000;
    private static final int MAX_COUNT = 100_000;
    private static final int MAX_TOTAL_RESULT_SIZE = 100_000;

    // this tool is supposed to be capable to generate sequences of phone numbers,
    // the standard length of E164 phone number is 11 (w/o plus sign)
    private static final double MAX_START = 100_000_000_000L; // 12

    public @FXML GridPane rootPane;
    public @FXML TextField tfPattern;
    public @FXML TextArea taResult;
    public @FXML Label lbRowLimit;
    public @FXML Label lbLinesCount;
    public @FXML Button btnGenerate;
    public @FXML Button btnSaveToFile;

    public @FXML HBox boxA;
    public @FXML CheckBox cbA;
    public @FXML Spinner<Double> startA;
    public @FXML Spinner<Integer> stepA;
    public @FXML Spinner<Integer> countA;

    public @FXML HBox boxB;
    public @FXML CheckBox cbB;
    public @FXML Spinner<Double> startB;
    public @FXML Spinner<Integer> stepB;
    public @FXML Spinner<Integer> countB;

    public @FXML HBox boxC;
    public @FXML CheckBox cbC;
    public @FXML Spinner<Double> startC;
    public @FXML Spinner<Integer> stepC;
    public @FXML Spinner<Integer> countC;

    public @FXML HBox boxD;
    public @FXML CheckBox cbD;
    public @FXML Spinner<Double> startD;
    public @FXML Spinner<Integer> stepD;
    public @FXML Spinner<Integer> countD;

    private String totalResult = "";

    @FXML
    public void initialize() {
        lbRowLimit.setText(Messages.get(TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN, TEXTAREA_ROW_LIMIT));

        initItemControls();

        BooleanProperty[] selectedCheckboxes = {
                cbA.selectedProperty(), cbB.selectedProperty(), cbC.selectedProperty(), cbD.selectedProperty()
        };
        btnGenerate.disableProperty().bind(BindUtils.or(
                BindUtils.isBlank(tfPattern.textProperty()),
                Bindings.not(BindUtils.or(selectedCheckboxes))
        ));

        btnSaveToFile.disableProperty().bind(BindUtils.isBlank(taResult.textProperty()));
    }

    private void initItemControls() {
        startA.setEditable(true);
        startB.setEditable(true);
        startC.setEditable(true);
        startD.setEditable(true);

        startA.setValueFactory(new DoubleSpinnerValueFactory(0, MAX_START, 0, 1));
        startB.setValueFactory(new DoubleSpinnerValueFactory(0, MAX_START, 0, 1));
        startC.setValueFactory(new DoubleSpinnerValueFactory(0, MAX_START, 0, 1));
        startD.setValueFactory(new DoubleSpinnerValueFactory(0, MAX_START, 0, 1));

        DoubleStringConverter.createFor(startA);
        DoubleStringConverter.createFor(startB);
        DoubleStringConverter.createFor(startC);
        DoubleStringConverter.createFor(startD);

        stepA.setValueFactory(new IntegerSpinnerValueFactory(-MAX_STEP, MAX_STEP, 1, 1));
        stepB.setValueFactory(new IntegerSpinnerValueFactory(-MAX_STEP, MAX_STEP, 1, 1));
        stepC.setValueFactory(new IntegerSpinnerValueFactory(-MAX_STEP, MAX_STEP, 1, 1));
        stepD.setValueFactory(new IntegerSpinnerValueFactory(-MAX_STEP, MAX_STEP, 1, 1));

        countA.setValueFactory(new IntegerSpinnerValueFactory(2, MAX_COUNT, 10, 1));
        countB.setValueFactory(new IntegerSpinnerValueFactory(2, MAX_COUNT, 10, 1));
        countC.setValueFactory(new IntegerSpinnerValueFactory(2, MAX_COUNT, 10, 1));
        countD.setValueFactory(new IntegerSpinnerValueFactory(2, MAX_COUNT, 10, 1));

        IntegerStringConverter.createFor(countA);
        IntegerStringConverter.createFor(countB);
        IntegerStringConverter.createFor(countC);
        IntegerStringConverter.createFor(countD);

        cbA.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) toggleSpinners(boxA, newVal);
        });
        cbB.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) toggleSpinners(boxB, newVal);
        });
        cbC.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) toggleSpinners(boxC, newVal);
        });
        cbD.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) toggleSpinners(boxD, newVal);
        });

        cbA.setSelected(true);
        cbB.setSelected(false);
        cbC.setSelected(false);
        cbD.setSelected(false);

        toggleSpinners(boxA, true);
        toggleSpinners(boxB, false);
        toggleSpinners(boxC, false);
        toggleSpinners(boxD, false);
    }

    private void toggleSpinners(HBox parentPane, boolean enabled) {
        for (Node child : parentPane.getChildren()) {
            if (child instanceof Spinner) child.setDisable(!enabled);
        }
    }

    @FXML
    public void generate() {
        String pattern = tfPattern.getText();
        List<Item<String>> items = new ArrayList<>();

        if (cbA.isSelected()) {
            items.add(new Item<>(ITEM_A, startA.getValue(), stepA.getValue(), countA.getValue()));
        }
        if (cbB.isSelected()) {
            items.add(new Item<>(ITEM_B, startB.getValue(), stepB.getValue(), countB.getValue()));
        }
        if (cbC.isSelected()) {
            items.add(new Item<>(ITEM_C, startC.getValue(), stepC.getValue(), countC.getValue()));
        }
        if (cbD.isSelected()) {
            items.add(new Item<>(ITEM_D, startD.getValue(), stepD.getValue(), countD.getValue()));
        }

        // additional check, generate button is disabled if these conditions don't match
        if (isBlank(pattern) || items.isEmpty()) return;

        long sequenceSize = items.stream().
                map(item -> item.count)
                .reduce(1, (a, b) -> a * b);

        if (SequenceGenerator.expectedSize(items) > MAX_TOTAL_RESULT_SIZE) {
            Dialogs.warning()
                    .title(Messages.get(WARNING))
                    .content(Messages.get(TOOLS_SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT, MAX_TOTAL_RESULT_SIZE))
                    .owner(rootPane.getScene().getWindow())
                    .build()
                    .showAndWait();
            return;
        }

        GenerateTask task = new GenerateTask(pattern, items);
        task.setOnSucceeded(event -> {
            toggleProgressIndicator(false);
            Result result = task.getValue();
            lbLinesCount.setText(String.valueOf(result.getLinesCount()));
            totalResult = result.getTotal();
            taResult.setText(result.getDisplayed());
        });
        task.setOnFailed(event -> {
            toggleProgressIndicator(false);
            Throwable exception = event.getSource().getException();
            if (exception != null) {
                DefaultEventBus.getInstance().publish(new ExceptionCaughtEvent(exception));
            }
        });

        toggleProgressIndicator(true);
        new Thread(task).start();
    }

    private void toggleProgressIndicator(boolean on) {
        DefaultEventBus.getInstance().publish(new ProgressIndicatorEvent(on));
    }

    @FXML
    public void saveToFile() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(Messages.get(FILE_DIALOG_TEXT), "*.txt")
                .initialFileName(FileUtils.sanitizeFileName("sequence.txt"))
                .build()
                .showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;

        try {
            Files.writeString(outputFile.toPath(), totalResult);
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }

    @Override
    public Region getRoot() { return rootPane; }

    @Override
    public void reset() {}

    ///////////////////////////////////////////////////////////////////////////

    private static class GenerateTask extends Task<Result> {

        private final String pattern;
        private final List<Item<String>> items;

        private static final BiFunction<String, Double, String> CONVERTER =
                (id, value) -> String.valueOf(value.longValue());

        public GenerateTask(String pattern, List<Item<String>> items) {
            this.pattern = pattern;
            this.items = items;
        }

        @Override
        protected Result call() {

            SequenceGenerator<String, String> generator = new SequenceGenerator<>(items, CONVERTER);
            List<Map<String, String>> sequence = generator.generate();

            StringBuilder totalResult = new StringBuilder();
            StringBuilder displayedResult = new StringBuilder();

            int index = 0;
            for (Map<String, String> replacements : sequence) {
                String line = PlaceholderReplacer.format(pattern, replacements);
                totalResult.append(line).append("\n");

                if (sequence.size() > TEXTAREA_ROW_LIMIT && index < TEXTAREA_ROW_LIMIT) {
                    displayedResult.append(line).append("\n");
                }

                index++;
            }

            return new Result(totalResult.toString(), displayedResult.toString(), sequence.size());
        }
    }

    private static class Result {

        private final String total;
        private final String displayed;
        private final int linesCount;

        public Result(String total, String displayed, int linesCount) {
            this.total = total;
            this.displayed = displayed;
            this.linesCount = linesCount;
        }

        public String getTotal() {
            return total;
        }

        public String getDisplayed() {
            return !displayed.isEmpty() ? displayed : total;
        }

        public int getLinesCount() {
            return linesCount;
        }
    }
}
