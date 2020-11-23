package org.telekit.ui.tools.passgen;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.domain.ProgressIndicatorEvent;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.Dialogs;
import org.telekit.controls.util.ExtraBindings;
import org.telekit.controls.format.IntegerStringConverter;
import org.telekit.base.i18n.Messages;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.PasswordGenerator;
import org.telekit.ui.Launcher;
import org.telekit.ui.domain.ExceptionCaughtEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.telekit.base.ui.UIDefaults.TEXTAREA_ROW_LIMIT;
import static org.telekit.ui.MessageKeys.*;

public class RootController extends Controller {

    private static final String TYPE_RANDOM = "RANDOM";
    private static final String TYPE_KATAKANA = "KATAKANA";
    private static final String TYPE_XKCD = "XKCD";
    private static final String XKCD_DICT_PATH = "/assets/dict/words_en";

    private static final List<Character> SIMILAR_CHARS = List.of(
            'i', 'I', 'L', 'l', '1', '0', 'o', 'O'
    );
    private static final long MAX_SHOWN_RESULT_ROWS = 1000;

    public @FXML GridPane rootPane;
    public @FXML Accordion accordion;
    public @FXML TextArea taGeneratedPasswords;
    public @FXML Spinner<Integer> spnPasswordsCount;
    public @FXML Button btnSaveToFile;

    public @FXML TitledPane paneRandom;
    public @FXML Spinner<Integer> spnRandomLength;
    public @FXML CheckBox cbRandomLowercase;
    public @FXML CheckBox cbRandomUppercase;
    public @FXML CheckBox cbRandomDigits;
    public @FXML CheckBox cbRandomSpecialChars;
    public @FXML CheckBox cbRandomExcludeSimilar;
    public @FXML Label lbRandomExample;

    public @FXML TitledPane paneKatakana;
    public @FXML Spinner<Integer> spnKatakanaLength;
    public @FXML Label lbKatakanaExample;

    public @FXML TitledPane paneXKCD;
    public @FXML Label lbXKCDExample;
    public @FXML Spinner<Integer> spnXKCDWords;
    public @FXML Label lbRowLimit;

    // all collections are immutable
    private List<String> xkcdDict = null;
    private List<Character> randomDict = null;
    private String generatedPasswordsCache = "";

    @FXML
    public void initialize() {
        lbRowLimit.setText(Messages.get(TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN, TEXTAREA_ROW_LIMIT));

        paneRandom.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) onPaneExpanded(TYPE_RANDOM);
        });
        paneKatakana.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) onPaneExpanded(TYPE_KATAKANA);
        });
        paneXKCD.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) onPaneExpanded(TYPE_XKCD);
        });

        // init default opened pane
        accordion.setExpandedPane(paneRandom);
        updateRandomDictPlusExample();

        cbRandomLowercase.selectedProperty().addListener((observable, oldValue, newValue) -> updateRandomDictPlusExample());
        cbRandomUppercase.selectedProperty().addListener((observable, oldValue, newValue) -> updateRandomDictPlusExample());
        cbRandomDigits.selectedProperty().addListener((observable, oldValue, newValue) -> updateRandomDictPlusExample());
        cbRandomSpecialChars.selectedProperty().addListener((observable, oldValue, newValue) -> updateRandomDictPlusExample());
        cbRandomExcludeSimilar.selectedProperty().addListener((observable, oldValue, newValue) -> updateRandomDictPlusExample());
        cbRandomLowercase.setDisable(true);

        spnPasswordsCount.setEditable(true);
        IntegerStringConverter.createFor(spnPasswordsCount);
        btnSaveToFile.disableProperty().bind(ExtraBindings.isBlank(taGeneratedPasswords.textProperty()));
    }

    @FXML
    public void generate() {
        String passwordType = getPasswordTypeFromPaneID(accordion.getExpandedPane().getId());

        // update global settings
        randomDict = createRandomCharsSequence();
        if (xkcdDict == null) loadXKCDDict();

        int length = switch (passwordType) {
            case TYPE_RANDOM -> spnRandomLength.getValue();
            case TYPE_KATAKANA -> spnKatakanaLength.getValue();
            case TYPE_XKCD -> spnXKCDWords.getValue();
            default -> 12;
        };
        int passwordCount = spnPasswordsCount.getValue();

        GenerateTask task = new GenerateTask(passwordType, passwordCount, length);
        task.setOnSucceeded(event -> {
            toggleProgressIndicator(false);
            Result result = task.getValue();
            taGeneratedPasswords.setText(result.getDisplayed());
            generatedPasswordsCache = result.getTotal();
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
        DefaultEventBus.getInstance().publish(new ProgressIndicatorEvent(id, on));
    }

    @FXML
    public void saveToFile() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(Messages.get(FILE_DIALOG_TEXT), "*.txt")
                .initialFileName(FileUtils.sanitizeFileName("passwords.txt"))
                .build()
                .showSaveDialog(rootPane.getScene().getWindow());

        if (outputFile == null) return;

        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw)) {

            out.write(generatedPasswordsCache);
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }

    private void updateRandomDictPlusExample() {
        randomDict = createRandomCharsSequence();
        lbRandomExample.setText(generatePassword(TYPE_RANDOM, 16));
    }

    private void onPaneExpanded(String passwordType) {
        switch (passwordType) {
            case TYPE_RANDOM -> lbRandomExample.setText(generatePassword(passwordType, 16));
            case TYPE_KATAKANA -> lbKatakanaExample.setText(generatePassword(passwordType, 12));
            case TYPE_XKCD -> {
                if (xkcdDict == null) loadXKCDDict();
                lbXKCDExample.setText(generatePassword(passwordType, 2));
            }
        }
    }

    private String getPasswordTypeFromPaneID(String paneID) {
        return paneID.replaceAll("^pane", "").toUpperCase();
    }

    private String generatePassword(String passwordType, int length) {
        return switch (passwordType) {
            case TYPE_KATAKANA -> PasswordGenerator.katakana(length);
            case TYPE_XKCD -> PasswordGenerator.xkcd(length, "-", xkcdDict);
            default -> PasswordGenerator.random(length, randomDict);
        };
    }

    private List<Character> createRandomCharsSequence() {
        List<Character> chars = new ArrayList<>();
        if (cbRandomDigits.isSelected()) chars.addAll(PasswordGenerator.ASCII_DIGITS);
        if (cbRandomLowercase.isSelected()) chars.addAll(PasswordGenerator.ASCII_LOWER);
        if (cbRandomUppercase.isSelected()) chars.addAll(PasswordGenerator.ASCII_UPPER);
        if (cbRandomSpecialChars.isSelected()) chars.addAll(PasswordGenerator.ASCII_SPECIAL_CHARS);
        if (cbRandomExcludeSimilar.isSelected()) chars.removeAll(SIMILAR_CHARS);
        return Collections.unmodifiableList(chars);
    }

    private void loadXKCDDict() {
        this.xkcdDict = new BufferedReader(
                new InputStreamReader(Launcher.getResourceAsStream(XKCD_DICT_PATH)))
                .lines().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void reset() {}

    private class GenerateTask extends Task<Result> {

        private final String passwordType;
        private final int passwordCount;
        private final int length;

        public GenerateTask(String passwordType, int passwordCount, int length) {
            this.passwordType = passwordType;
            this.passwordCount = passwordCount;
            this.length = length;
        }

        @Override
        protected Result call() {
            StringBuilder result = new StringBuilder();
            StringBuilder displayedResult = new StringBuilder();

            for (int index = 0; index < passwordCount; index++) {
                String password = generatePassword(passwordType, length);
                result.append(password).append("\n");

                if (passwordCount > MAX_SHOWN_RESULT_ROWS && index < MAX_SHOWN_RESULT_ROWS) {
                    displayedResult.append(password).append("\n");
                }
            }

            return new Result(result.toString(), displayedResult.toString());
        }
    }

    private static class Result {

        private final String total;
        private final String displayed;

        public Result(String total, String displayed) {
            this.total = total;
            this.displayed = displayed;
        }

        public String getTotal() {
            return total;
        }

        public String getDisplayed() {
            return !displayed.isEmpty() ? displayed : total;
        }
    }
}
