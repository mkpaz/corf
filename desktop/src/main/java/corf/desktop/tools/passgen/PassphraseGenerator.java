package corf.desktop.tools.passgen;

import backbonefx.event.EventBus;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import corf.base.common.Initializer;
import corf.base.common.Lazy;
import corf.base.desktop.controls.HorizontalForm;
import corf.base.text.PasswordGenerator;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static corf.base.i18n.I18n.t;
import static corf.desktop.startup.Config.DESKTOP_MODULE;

final class PassphraseGenerator implements Generator {

    static final String NAME = "Passphrase";

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 32;
    private static final int DEFAULT_LENGTH = 7;
    private static final String DEFAULT_SEPARATOR = "-";
    private static final String DICT_PATH = DESKTOP_MODULE.concat("assets/dict/words.txt").toString();

    private Slider lengthSlider;
    private TextField separatorField;
    private ToggleGroup caseToggleGroup;

    private final Lazy<VBox> view = new Lazy<>(this::createView);
    private final List<String> dictionary = new ArrayList<>();

    private final EventBus eventBus;

    PassphraseGenerator(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String generate() {
        var len = view.initialized() ? (int) lengthSlider.getValue() : DEFAULT_LENGTH;
        var sep = view.initialized() ? separatorField.getText() : DEFAULT_SEPARATOR;
        boolean lowercase = false, capitalize = false, uppercase = false;
        loadDictionary();

        if (view.initialized()
                && caseToggleGroup.getSelectedToggle() != null
                && caseToggleGroup.getSelectedToggle().getUserData() instanceof String type) {
            if (UPPERCASE.equals(type)) { uppercase = true; }
            if (TITLECASE.equals(type)) { capitalize = true; }
            if (LOWERCASE.equals(type)) { lowercase = true; }
        }

        var password = PasswordGenerator.passphrase(len, sep, dictionary, capitalize);
        if (lowercase) { password = password.toLowerCase(); }
        if (uppercase) { password = password.toUpperCase(); }

        return password;
    }

    @Override
    public Node getView() {
        return view.get();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Initializer
    private VBox createView() {
        Pair<Slider, VBox> lengthBlock = createLengthSlider(
                MIN_LENGTH, MAX_LENGTH, DEFAULT_LENGTH, len -> Math.round(len) + " " + t(DM.PASSGEN_WORDS).toLowerCase()
        );
        lengthSlider = lengthBlock.getKey();

        Pair<ToggleGroup, HBox> caseBlock = createCaseSwitcher();
        caseToggleGroup = caseBlock.getKey();
        separatorField = new TextField(DEFAULT_SEPARATOR);
        separatorField.setPrefWidth(300);
        separatorField.textProperty().addListener((obs, old, val) -> eventBus.publish(new ChangeEvent()));

        var tuneForm = new HorizontalForm(Recommends.FORM_HGAP, Recommends.FORM_VGAP);
        tuneForm.add(t(DM.SEPARATOR), separatorField);
        tuneForm.add(t(DM.PASSGEN_LETTER_CASE), caseBlock.getValue());

        var root = new VBox(
                Recommends.FORM_VGAP,
                lengthBlock.getValue(),
                tuneForm
        );
        root.setFillWidth(true);

        return root;
    }

    @SuppressWarnings("ReturnValueIgnored")
    private void loadDictionary() {
        if (dictionary.isEmpty()) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(DICT_PATH)), UTF_8)
            );
            reader.lines().collect(Collectors.toCollection(() -> dictionary));
        }
    }
}
