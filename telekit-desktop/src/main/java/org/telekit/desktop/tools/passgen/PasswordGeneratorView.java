package org.telekit.desktop.tools.passgen;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.desktop.Component;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.util.PasswordGenerator;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.Controls;
import org.telekit.controls.util.IntegerStringConverter;
import org.telekit.controls.util.Promise;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.telekit.base.Env.TEXTAREA_ROW_LIMIT;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.util.FileUtils.getParentPath;
import static org.telekit.base.util.PasswordGenerator.ASCII_LOWER_UPPER_DIGITS;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.desktop.i18n.DesktopMessages.*;
import static org.telekit.desktop.startup.config.Config.DESKTOP_MODULE_PATH;

@Singleton
public final class PasswordGeneratorView extends GridPane implements Initializable, Component {

    static final int TYPE_RANDOM = 0;
    static final int TYPE_KATAKANA = 1;
    static final int TYPE_XKCD = 2;
    static final String XKCD_DICT_PATH = DESKTOP_MODULE_PATH.concat("assets/dict/words_en").toString();
    static final List<Character> SIMILAR_CHARS = List.of('i', 'I', 'L', 'l', '1', '0', 'o', 'O');

    TitledPane randRoot;
    Spinner<Integer> randLengthSpinner;
    CheckBox randLowerCheck;
    CheckBox randUpperCheck;
    CheckBox randDigitsCheck;
    CheckBox randSpecialsCheck;
    CheckBox randSimilarCheck;

    TitledPane kataRoot;
    Spinner<Integer> kataLengthSpinner;

    TitledPane xkcdRoot;
    Spinner<Integer> xkcdLengthSpinner;

    Accordion accordion;
    TextArea generatedText;
    Button exportBtn;
    Button generateBtn;
    Spinner<Integer> countSpinner;

    private final List<Character> randAlphabet = new ArrayList<>();
    private final List<String> xkcdDict = new ArrayList<>();

    private final ObjectProperty<Supplier<String>> generator = new SimpleObjectProperty<>(this, "generator");
    private final ExecutorService threadPool;
    private Path lastVisitedDirectory;

    @Inject
    public PasswordGeneratorView(ExecutorService threadPool) {
        this.threadPool = threadPool;

        createView();
    }

    private void createView() {
        // LEFT

        VBox randBox = vbox(10, Pos.TOP_LEFT, new Insets(20));
        randRoot = new TitledPane(t(RANDOM), randBox);
        randRoot.setUserData(TYPE_RANDOM);

        HBox randSpinnerBox = hbox(10, Pos.CENTER_LEFT, Insets.EMPTY);
        randLengthSpinner = new Spinner<>(4, 64, 16, 1);
        randSpinnerBox.getChildren().setAll(randLengthSpinner, new Label(t(TOOLS_CHARACTERS)));

        randLowerCheck = new CheckBox("abcd");
        randLowerCheck.setSelected(true);
        randLowerCheck.setDisable(true);

        randUpperCheck = new CheckBox("ABCD");
        randUpperCheck.setSelected(true);

        randDigitsCheck = new CheckBox("1234");
        randDigitsCheck.setSelected(true);

        randSpecialsCheck = new CheckBox("$#@*");
        randSimilarCheck = new CheckBox(t(PASSGEN_EXCLUDE_SIMILAR_CHARS));

        Label randExampleLabel = Controls.create(() -> new Label(t(EXAMPLE)), "text-bold");
        randExampleLabel.setPadding(new Insets(20, 0, 0, 0));

        randBox.getChildren().setAll(
                randSpinnerBox,
                randLowerCheck,
                randUpperCheck,
                randDigitsCheck,
                randSpecialsCheck,
                randSimilarCheck,
                randExampleLabel,
                new Label(PasswordGenerator.random(16, ASCII_LOWER_UPPER_DIGITS))
        );

        // ~
        VBox kataBox = vbox(10, Pos.TOP_LEFT, new Insets(20));
        kataRoot = new TitledPane(t(PASSGEN_KATAKANA), kataBox);
        kataRoot.setUserData(TYPE_KATAKANA);

        HBox kataSpinnerBox = hbox(10, Pos.CENTER_LEFT, Insets.EMPTY);
        kataLengthSpinner = new Spinner<>(4, 64, 12, 1);
        kataSpinnerBox.getChildren().setAll(kataLengthSpinner, new Label(t(TOOLS_CHARACTERS)));

        Label kataExampleLabel = Controls.create(() -> new Label(t(EXAMPLE)), "text-bold");
        kataExampleLabel.setPadding(new Insets(20, 0, 0, 0));

        kataBox.getChildren().setAll(
                kataSpinnerBox,
                kataExampleLabel,
                new Label(PasswordGenerator.katakana(12, true))
        );

        // ~

        VBox xkcdBox = vbox(10, Pos.TOP_LEFT, new Insets(20));
        xkcdRoot = new TitledPane(t(PASSGEN_XKCD), xkcdBox);
        xkcdRoot.setUserData(TYPE_XKCD);

        HBox xkcdSpinnerBox = hbox(10, Pos.CENTER_LEFT, Insets.EMPTY);
        xkcdLengthSpinner = new Spinner<>(2, 10, 3, 1);
        xkcdSpinnerBox.getChildren().setAll(xkcdLengthSpinner, new Label(t(TOOLS_WORDS)));

        Label xkcdExampleLabel = Controls.create(() -> new Label(t(EXAMPLE)), "text-bold");
        xkcdExampleLabel.setPadding(new Insets(20, 0, 0, 0));

        xkcdBox.getChildren().setAll(
                xkcdSpinnerBox,
                xkcdExampleLabel,
                new Label("correct-horse-battery-staple")
        );

        // ~

        accordion = new Accordion();
        accordion.getPanes().addAll(randRoot, kataRoot, xkcdRoot);

        generateBtn = button(t(ACTION_GENERATE), Material2MZ.SHUFFLE, "large");
        generateBtn.setOnAction(e -> generate());

        countSpinner = new Spinner<>(100, 100000, 100, 100);
        countSpinner.setMinWidth(150);
        countSpinner.setEditable(true);
        IntegerStringConverter.createFor(countSpinner);

        HBox generateBox = hbox(10, Pos.CENTER_LEFT, new Insets(10, 0, 10, 0));
        generateBox.getChildren().setAll(
                generateBtn,
                countSpinner,
                new Label(t(TOOLS_ITEMS))
        );

        // RIGHT

        generatedText = Controls.create(TextArea::new, "monospace");
        generatedText.setEditable(false);

        exportBtn = new Button(t(ACTION_EXPORT));
        exportBtn.setOnAction(e -> export());
        exportBtn.disableProperty().bind(BindUtils.isBlank(generatedText.textProperty()));

        HBox exportBox = hbox(0, Pos.CENTER_LEFT, Insets.EMPTY);
        exportBox.getChildren().setAll(
                new Label(t(TOOLS_ONLY_FIRST_N_ROWS_WILL_BE_SHOWN, TEXTAREA_ROW_LIMIT)),
                horizontalSpacer(),
                exportBtn
        );

        // GRID

        add(new Label(t(PASSGEN_PASSWORD_TYPE)), 0, 0);
        add(accordion, 0, 1);

        add(new Label(t(PASSWORDS)), 1, 0);
        add(generatedText, 1, 1);

        add(exportBox, 1, 2);

        add(generateBox, 0, 3, REMAINING, 1);

        getRowConstraints().addAll(VGROW_NEVER, VGROW_ALWAYS, VGROW_NEVER);
        getColumnConstraints().addAll(columnConstraints(400, Priority.NEVER), HGROW_ALWAYS);

        setVgap(5);
        setHgap(10);
        setPadding(new Insets(10));
        setId("password-generator");
    }

    @Override
    public void initialize() {
        ChangeListener<Boolean> randAlphabetChangeListener = (obs, old, value) -> {
            randAlphabet.clear();

            //@formatter:off
            if (randDigitsCheck.isSelected())   { randAlphabet.addAll(PasswordGenerator.ASCII_DIGITS);        }
            if (randLowerCheck.isSelected())    { randAlphabet.addAll(PasswordGenerator.ASCII_LOWER);         }
            if (randUpperCheck.isSelected())    { randAlphabet.addAll(PasswordGenerator.ASCII_UPPER);         }
            if (randSpecialsCheck.isSelected()) { randAlphabet.addAll(PasswordGenerator.ASCII_SPECIAL_CHARS); }
            if (randSimilarCheck.isSelected())  { randAlphabet.removeAll(SIMILAR_CHARS);                      }
            //@formatter:on
        };

        randLowerCheck.selectedProperty().addListener(randAlphabetChangeListener);
        randUpperCheck.selectedProperty().addListener(randAlphabetChangeListener);
        randDigitsCheck.selectedProperty().addListener(randAlphabetChangeListener);
        randSpecialsCheck.selectedProperty().addListener(randAlphabetChangeListener);
        randSimilarCheck.selectedProperty().addListener(randAlphabetChangeListener);

        // update password supplier, so generated password type always matches currently expanded pane
        accordion.expandedPaneProperty().addListener((obs, old, value) -> {
            // make sure the accordion can never be completely collapsed
            boolean hasExpanded = accordion.getPanes().stream().anyMatch(TitledPane::isExpanded);
            if (!hasExpanded && old != null) { Platform.runLater(() -> accordion.setExpandedPane(old)); }

            if (value == null) { return; }

            // update password supplier
            int passwordType = (int) Objects.requireNonNull(value.getUserData());
            switch (passwordType) {
                case TYPE_RANDOM -> generator.set(
                        () -> PasswordGenerator.random(randLengthSpinner.getValue(), randAlphabet)
                );
                case TYPE_KATAKANA -> generator.set(
                        () -> PasswordGenerator.katakana(kataLengthSpinner.getValue())
                );
                case TYPE_XKCD -> generator.set(
                        () -> PasswordGenerator.xkcd(xkcdLengthSpinner.getValue(), "-", getOrLoadXkcdDict())
                );
            }
        });

        // set default opened pane
        accordion.setExpandedPane(randRoot);
    }

    private void generate() {
        final Supplier<String> passgen = Objects.requireNonNull(generator.get());
        final int count = countSpinner.getValue();

        Promise.supplyAsync(() -> {
            StringBuilder sb = new StringBuilder();
            int mark = 0;

            for (int i = 0; i < count; i++) {
                sb.append(passgen.get()).append("\n");
                if (i < TEXTAREA_ROW_LIMIT) { mark = sb.length(); }
            }

            return ImmutablePair.of(sb.toString(), mark);
        }).then(pair -> {
            String passwords = pair.getLeft();
            int mark = pair.getRight();

            generatedText.setText(passwords.substring(0, mark));
            generatedText.setUserData(passwords);
        }).start(threadPool);
    }

    private void export() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(lastVisitedDirectory)
                .initialFileName("passwords.txt")
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

    private List<String> getOrLoadXkcdDict() {
        if (xkcdDict.isEmpty()) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(XKCD_DICT_PATH)))
            );
            reader.lines().collect(Collectors.toCollection(() -> xkcdDict));
        }
        return xkcdDict;
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}
}
