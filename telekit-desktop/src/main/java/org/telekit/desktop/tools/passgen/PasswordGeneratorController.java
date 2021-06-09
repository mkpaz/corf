package org.telekit.desktop.tools.passgen;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.domain.event.TaskProgressEvent;
import org.telekit.base.i18n.I18n;
import org.telekit.base.util.FileUtils;
import org.telekit.base.util.PasswordGenerator;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.IntegerStringConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.telekit.base.Env.TEXTAREA_ROW_LIMIT;
import static org.telekit.desktop.i18n.DesktopMessages.*;
import static org.telekit.desktop.startup.config.Config.DESKTOP_MODULE_PATH;

@FxmlPath("/org/telekit/desktop/tools/passgen/_root.fxml")
public class PasswordGeneratorController implements Component {

    private static final String TYPE_RANDOM = "RANDOM";
    private static final String TYPE_KATAKANA = "KATAKANA";
    private static final String TYPE_XKCD = "XKCD";
    private static final String XKCD_DICT_PATH = DESKTOP_MODULE_PATH.concat("assets/dict/words_en").toString();

    private static final List<Character> SIMILAR_CHARS = List.of(
            'i', 'I', 'L', 'l', '1', '0', 'o', 'O'
    );

    public @FXML GridPane rootPane;
    public @FXML Accordion accordion;

    // random
    public @FXML TitledPane paneRandom;
    public @FXML Spinner<Integer> spnRandomLength;
    public @FXML CheckBox cbRandomLowercase;
    public @FXML CheckBox cbRandomUppercase;
    public @FXML CheckBox cbRandomDigits;
    public @FXML CheckBox cbRandomSpecialChars;
    public @FXML CheckBox cbRandomExcludeSimilar;
    public @FXML Label lbRandomExample;

    // katakana
    public @FXML TitledPane paneKatakana;
    public @FXML Spinner<Integer> spnKatakanaLength;
    public @FXML Label lbKatakanaExample;

    // xkcd
    public @FXML TitledPane paneXKCD;
    public @FXML Label lbXKCDExample;
    public @FXML Spinner<Integer> spnXKCDWords;
    public @FXML Label lbRowLimit;

    // control
    public @FXML TextArea taGeneratedPasswords;
    public @FXML Spinner<Integer> spnPasswordsCount;
    public @FXML Button btnSaveToFile;

    // all collections are unmodifiable
    private List<Character> alphabet = null; // alphabet to generate random passwords
    private List<String> xkcdDict = null;    // word dict to generate XKCD passwords
    private String totalResult = "";

    @FXML
    public void initialize() {
        lbRowLimit.setText(I18n.t(TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN, TEXTAREA_ROW_LIMIT));

        // update password when accordion pane expanded
        paneRandom.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) updateExample(TYPE_RANDOM);
        });
        paneKatakana.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) updateExample(TYPE_KATAKANA);
        });
        paneXKCD.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) updateExample(TYPE_XKCD);
        });

        // set default opened pane
        updateAlphabetAndExample();
        accordion.setExpandedPane(paneRandom);

        cbRandomLowercase.selectedProperty().addListener((obs, oldVal, newVal) -> updateAlphabetAndExample());
        cbRandomUppercase.selectedProperty().addListener((obs, oldVal, newVal) -> updateAlphabetAndExample());
        cbRandomDigits.selectedProperty().addListener((obs, oldVal, newVal) -> updateAlphabetAndExample());
        cbRandomSpecialChars.selectedProperty().addListener((obs, oldVal, newVal) -> updateAlphabetAndExample());
        cbRandomExcludeSimilar.selectedProperty().addListener((obs, oldVal, newVal) -> updateAlphabetAndExample());
        cbRandomLowercase.setDisable(true);

        spnPasswordsCount.setEditable(true);
        IntegerStringConverter.createFor(spnPasswordsCount);
        btnSaveToFile.disableProperty().bind(BindUtils.isBlank(taGeneratedPasswords.textProperty()));
    }

    @FXML
    public void generate() {
        String passwordType = getPasswordTypeFromPaneID(accordion.getExpandedPane().getId());

        // update global settings
        alphabet = updateAlphabet();
        if (xkcdDict == null) loadXKCDDict();

        int length = switch (passwordType) {
            case TYPE_RANDOM -> spnRandomLength.getValue();
            case TYPE_KATAKANA -> spnKatakanaLength.getValue();
            case TYPE_XKCD -> spnXKCDWords.getValue();
            default -> PasswordGenerator.DEFAULT_PASSWORD_LENGTH;
        };
        int passwordCount = spnPasswordsCount.getValue();

        GenerateTask task = new GenerateTask(passwordType, passwordCount, length);
        task.setOnSucceeded(event -> {
            toggleProgressIndicator(false);
            Result result = task.getValue();
            taGeneratedPasswords.setText(result.getDisplayed());
            totalResult = result.getTotal();
        });
        task.setOnFailed(event -> {
            toggleProgressIndicator(false);
            Throwable exception = event.getSource().getException();
            if (exception != null) {
                DefaultEventBus.getInstance().publish(Notification.error(exception));
            }
        });

        toggleProgressIndicator(true);
        new Thread(task).start();
    }

    private void toggleProgressIndicator(boolean on) {
        DefaultEventBus.getInstance().publish(new TaskProgressEvent(getClass().getCanonicalName(), on));
    }

    @FXML
    public void saveToFile() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(I18n.t(FILE_DIALOG_TEXT), "*.txt")
                .initialFileName(FileUtils.sanitizeFileName("passwords.txt"))
                .build()
                .showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;

        try {
            Files.writeString(outputFile.toPath(), totalResult);
        } catch (Exception e) {
            throw new TelekitException(I18n.t(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }

    private void updateAlphabetAndExample() {
        alphabet = updateAlphabet();
        lbRandomExample.setText(generatePassword(TYPE_RANDOM, 16));
    }

    private void updateExample(String passwordType) {
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
            default -> PasswordGenerator.random(length, alphabet);
        };
    }

    private List<Character> updateAlphabet() {
        List<Character> chars = new ArrayList<>();
        if (cbRandomDigits.isSelected()) chars.addAll(PasswordGenerator.ASCII_DIGITS);
        if (cbRandomLowercase.isSelected()) chars.addAll(PasswordGenerator.ASCII_LOWER);
        if (cbRandomUppercase.isSelected()) chars.addAll(PasswordGenerator.ASCII_UPPER);
        if (cbRandomSpecialChars.isSelected()) chars.addAll(PasswordGenerator.ASCII_SPECIAL_CHARS);
        if (cbRandomExcludeSimilar.isSelected()) chars.removeAll(SIMILAR_CHARS);
        return Collections.unmodifiableList(chars);
    }

    private void loadXKCDDict() {
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(XKCD_DICT_PATH)));
        xkcdDict = new BufferedReader(reader).lines().toList();
    }

    @Override
    public Region getRoot() { return rootPane; }

    @Override
    public void reset() {}

    ///////////////////////////////////////////////////////////////////////////

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
            // It's possible to remember text position instead of using two variables
            // for displayed and total text. But since strings are immutable there
            // will be no performance impact, because we still have to store displayed
            // text in the text area and total text in the controller variable.
            // TODO: Replace TextArea with ListView
            StringBuilder totalResult = new StringBuilder();
            StringBuilder displayedResult = new StringBuilder();

            for (int idx = 0; idx < passwordCount; idx++) {
                String password = generatePassword(passwordType, length);
                totalResult.append(password).append("\n");

                if (passwordCount > TEXTAREA_ROW_LIMIT && idx < TEXTAREA_ROW_LIMIT) {
                    displayedResult.append(password).append("\n");
                }
            }

            return new Result(totalResult.toString(), displayedResult.toString());
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
