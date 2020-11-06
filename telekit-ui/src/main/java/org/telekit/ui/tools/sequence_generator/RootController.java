package org.telekit.ui.tools.sequence_generator;

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
import org.telekit.base.EventBus;
import org.telekit.base.domain.ProgressIndicatorEvent;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.service.impl.SequenceGenerator;
import org.telekit.base.service.impl.SequenceGenerator.Item;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.Dialogs;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.controls.format.DoubleStringConverter;
import org.telekit.controls.format.IntegerStringConverter;
import org.telekit.controls.util.ExtraBindings;
import org.telekit.ui.domain.ExceptionCaughtEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.telekit.base.ui.UIDefaults.TEXTAREA_ROW_LIMIT;
import static org.telekit.ui.main.MessageKeys.*;

public class RootController extends Controller {

    // item ID must be the same as corresponding pattern placeholder
    private static final String ITEM_A = "A";
    private static final String ITEM_B = "B";
    private static final String ITEM_C = "C";
    private static final String ITEM_D = "D";

    // this utils is supposed to be used to generate sequences of phone numbers,
    // the standard length of E.164 phone number is 11
    private static final double MAX_START = 100_000_000_000D;
    private static final int MAX_STEP = 1_000;
    private static final int MAX_COUNT = 100_000;
    private static final long MAX_SEQUENCE_SIZE = 100_000;  // even 100k rows is huge for TextArea, it consumes ~800MB RAM
    private static final long MAX_SHOWN_RESULT_ROWS = 1000; // so only first MAX_SHOWN_RESULT_ROWS will be shown

    private String resultCache = "";

    public @FXML GridPane rootPane;
    public @FXML TextField tfPattern;
    public @FXML TextArea taResult;
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
    public @FXML Label lbRowLimit;

    @FXML
    public void initialize() {
        lbRowLimit.setText(Messages.get(TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN, TEXTAREA_ROW_LIMIT));

        startA.setEditable(true);

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

        cbA.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) toggleSpinners(boxA, newValue);
        });
        cbB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) toggleSpinners(boxB, newValue);
        });
        cbC.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) toggleSpinners(boxC, newValue);
        });
        cbD.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) toggleSpinners(boxD, newValue);
        });

        cbA.setSelected(true);
        cbB.setSelected(false);
        cbC.setSelected(false);
        cbD.setSelected(false);

        toggleSpinners(boxA, true);
        toggleSpinners(boxB, false);
        toggleSpinners(boxC, false);
        toggleSpinners(boxD, false);

        BooleanProperty[] selectedCheckboxes = {
                cbA.selectedProperty(), cbB.selectedProperty(), cbC.selectedProperty(), cbD.selectedProperty()
        };
        btnGenerate.disableProperty().bind(Bindings.or(
                ExtraBindings.isBlank(tfPattern.textProperty()),
                Bindings.not(ExtraBindings.or(selectedCheckboxes))
        ));

        btnSaveToFile.disableProperty().bind(ExtraBindings.isBlank(taResult.textProperty()));
    }

    private void toggleSpinners(HBox parentPane, boolean enabled) {
        for (Node child : parentPane.getChildren()) {
            if (child instanceof Spinner) child.setDisable(!enabled);
        }
    }

    @FXML
    public void generate() {
        String pattern = tfPattern.getText();
        List<Item> items = new ArrayList<>();

        if (cbA.isSelected()) {
            items.add(new Item(ITEM_A, startA.getValue(), stepA.getValue(), countA.getValue()));
        }
        if (cbB.isSelected()) {
            items.add(new Item(ITEM_B, startB.getValue(), stepB.getValue(), countB.getValue()));
        }
        if (cbC.isSelected()) {
            items.add(new Item(ITEM_C, startC.getValue(), stepC.getValue(), countC.getValue()));
        }
        if (cbD.isSelected()) {
            items.add(new Item(ITEM_D, startD.getValue(), stepD.getValue(), countD.getValue()));
        }

        // additional check, generate button is disabled if these conditions don't match
        if (isBlank(pattern) || items.isEmpty()) return;

        long sequenceSize = items.stream().map(item -> item.count).reduce(1, (a, b) -> a * b);
        if (sequenceSize > MAX_SEQUENCE_SIZE) {
            Dialogs.warning()
                    .title(Messages.get(WARNING))
                    .content(Messages.get(TOOLS_SEQGEN_MSG_SEQUENCE_SIZE_EXCEEDS_LIMIT, MAX_SEQUENCE_SIZE))
                    .owner(rootPane.getScene().getWindow())
                    .build()
                    .showAndWait();
        }

        GenerateTask task = new GenerateTask(pattern, items);
        task.setOnSucceeded(event -> {
            toggleProgressIndicator(false);
            Result result = task.getValue();
            lbLinesCount.setText(String.valueOf(result.getLineCount()));
            resultCache = result.getTotal();
            taResult.setText(result.getDisplayed());
        });
        task.setOnFailed(event -> {
            toggleProgressIndicator(false);
            Throwable exception = event.getSource().getException();
            if (exception != null) {
                EventBus.getInstance().publish(new ExceptionCaughtEvent(exception));
            }
        });

        toggleProgressIndicator(true);
        new Thread(task).start();
    }

    private void toggleProgressIndicator(boolean on) {
        EventBus.getInstance().publish(new ProgressIndicatorEvent(id, on));
    }

    @FXML
    public void saveToFile() {
        File outputFile = Dialogs.file()
                .addFilter(Messages.get(FILE_DIALOG_TEXT), "*.txt")
                .initialFilename(FileUtils.sanitizeFileName("sequence.txt"))
                .build()
                .showSaveDialog(rootPane.getScene().getWindow());

        if (outputFile == null) return;

        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw)) {

            out.write(resultCache);
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }

    @Override
    public void reset() {}

    private static class GenerateTask extends Task<Result> {

        private final String pattern;
        private final List<Item> items;

        public GenerateTask(String pattern, List<Item> items) {
            this.pattern = pattern;
            this.items = items;
        }

        @Override
        protected Result call() {
            SequenceGenerator generator = new SequenceGenerator(items);
            List<Map<String, String>> sequence = generator.generate();

            StringBuilder result = new StringBuilder();
            StringBuilder displayedResult = new StringBuilder();

            int index = 0;
            for (Map<String, String> replacements : sequence) {
                String line = PlaceholderReplacer.format(pattern, replacements);
                result.append(line).append("\n");

                if (sequence.size() > MAX_SHOWN_RESULT_ROWS && index < MAX_SHOWN_RESULT_ROWS) {
                    displayedResult.append(line).append("\n");
                }

                index++;
            }

            return new Result(result.toString(), displayedResult.toString(), sequence.size());
        }
    }

    private static class Result {

        private final String total;
        private final String displayed;
        private final int lineCount;

        public Result(String total, String displayed, int lineCount) {
            this.total = total;
            this.displayed = displayed;
            this.lineCount = lineCount;
        }

        public String getTotal() {
            return total;
        }

        public String getDisplayed() {
            return !displayed.isEmpty() ? displayed : total;
        }

        public int getLineCount() {
            return lineCount;
        }
    }
}
