package corf.desktop.tools.passgen;

import backbonefx.event.EventBus;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import corf.base.common.Initializer;
import corf.base.common.Lazy;
import corf.base.desktop.controls.HorizontalForm;
import corf.base.text.PasswordGenerator;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.ArrayList;
import java.util.List;

import static corf.base.i18n.I18n.t;

final class RandomCharactersGenerator implements Generator {

    static final String NAME = "Random Characters";

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 128;
    private static final int DEFAULT_LENGTH = 16;
    private static final int TOGGLE_WIDTH = 130;

    private Slider lengthSlider;
    private ToggleButton lowerToggle;
    private ToggleButton upperToggle;
    private ToggleButton digitToggle;
    private ToggleButton logogramToggle;
    private ToggleButton punctuationToggle;
    private ToggleButton quoteToggle;
    private ToggleButton slashToggle;
    private ToggleButton mathToggle;
    private ToggleButton braceToggle;
    private CheckBox similarCheck;
    private TextField includeText;
    private TextField excludeText;

    private final Lazy<VBox> view = new Lazy<>(this::createView);
    private final List<Character> characters = new ArrayList<>();
    private final EventBus eventBus;

    RandomCharactersGenerator(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String generate() {
        var len = view.initialized() ? (int) lengthSlider.getValue() : DEFAULT_LENGTH;
        var characterSet = view.initialized() ? characters : PasswordGenerator.ASCII_LOWER_UPPER_DIGITS;
        return PasswordGenerator.random(len, characterSet);
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
        ChangeListener<Object> characterSetListener = (obs, old, val) -> {
            if (!view.initialized()) { return; }
            updateCharacters();
            eventBus.publish(new ChangeEvent());
        };

        Pair<Slider, VBox> lengthBlock = createLengthSlider(
                MIN_LENGTH, MAX_LENGTH, DEFAULT_LENGTH, len -> Math.round(len) + " " + t(DM.PASSGEN_CHARACTERS)
        );
        lengthSlider = lengthBlock.getKey();

        // == Toggles ==

        var alphabetTitleLabel = new Label(t(DM.PASSGEN_CHARACTER_SET));

        lowerToggle = new ToggleButton("a-z");
        lowerToggle.selectedProperty().addListener(characterSetListener);
        lowerToggle.setSelected(true);
        lowerToggle.setPrefWidth(TOGGLE_WIDTH);
        lowerToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        upperToggle = new ToggleButton("A-Z");
        upperToggle.selectedProperty().addListener(characterSetListener);
        upperToggle.setSelected(true);
        upperToggle.setPrefWidth(TOGGLE_WIDTH);
        upperToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        digitToggle = new ToggleButton("0-9");
        digitToggle.selectedProperty().addListener(characterSetListener);
        digitToggle.setSelected(true);
        digitToggle.setPrefWidth(TOGGLE_WIDTH);
        digitToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        logogramToggle = new ToggleButton("#$%&@^`~");
        logogramToggle.selectedProperty().addListener(characterSetListener);
        logogramToggle.setSelected(true);
        logogramToggle.setPrefWidth(TOGGLE_WIDTH);
        logogramToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        mathToggle = new ToggleButton("<>*+!?=");
        mathToggle.selectedProperty().addListener(characterSetListener);
        mathToggle.setSelected(true);
        mathToggle.setPrefWidth(TOGGLE_WIDTH);
        mathToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        punctuationToggle = new ToggleButton(". , : ;");
        punctuationToggle.selectedProperty().addListener(characterSetListener);
        punctuationToggle.setPrefWidth(TOGGLE_WIDTH);
        punctuationToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        quoteToggle = new ToggleButton("\" '");
        quoteToggle.selectedProperty().addListener(characterSetListener);
        quoteToggle.setPrefWidth(TOGGLE_WIDTH);
        quoteToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        slashToggle = new ToggleButton("\\ / | - _");
        slashToggle.selectedProperty().addListener(characterSetListener);
        slashToggle.setPrefWidth(TOGGLE_WIDTH);
        slashToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        braceToggle = new ToggleButton("{} [] ()");
        braceToggle.selectedProperty().addListener(characterSetListener);
        braceToggle.setPrefWidth(TOGGLE_WIDTH);
        braceToggle.getStyleClass().addAll(ExtraStyles.MONOSPACE);

        // == Include / Exclude ==

        includeText = new TextField();
        includeText.setPrefWidth(300);
        includeText.textProperty().addListener(characterSetListener);

        excludeText = new TextField();
        excludeText.setPrefWidth(300);
        excludeText.textProperty().addListener(characterSetListener);

        var tuneForm = new HorizontalForm(Recommends.FORM_HGAP, Recommends.FORM_VGAP);
        tuneForm.add(t(DM.PASSGEN_INCLUDE), includeText);
        tuneForm.add(t(DM.PASSGEN_EXCLUDE), excludeText);

        similarCheck = new CheckBox(t(DM.PASSGEN_EXCLUDE_SIMILAR_CHARS));
        similarCheck.selectedProperty().addListener(characterSetListener);

        // update characters to match selection state
        updateCharacters();

        // ~

        var togglesPane = new TilePane(
                lowerToggle, upperToggle, digitToggle, logogramToggle, mathToggle,
                punctuationToggle, quoteToggle, slashToggle, braceToggle
        );
        togglesPane.setHgap(5);
        togglesPane.setVgap(5);
        togglesPane.setMaxWidth((TOGGLE_WIDTH * 5) + (5 * 5));
        togglesPane.setAlignment(Pos.CENTER_LEFT);

        var root = new VBox(
                Recommends.FORM_VGAP,
                lengthBlock.getValue(),
                new VBox(Recommends.CAPTION_MARGIN, alphabetTitleLabel, togglesPane),
                tuneForm,
                similarCheck
        );
        root.setFillWidth(true);

        return root;
    }

    private void updateCharacters() {
        characters.clear();

        //@formatter:off
        if (lowerToggle.isSelected())       { characters.addAll(PasswordGenerator.ASCII_LOWER);       }
        if (upperToggle.isSelected())       { characters.addAll(PasswordGenerator.ASCII_UPPER);       }
        if (digitToggle.isSelected())       { characters.addAll(PasswordGenerator.ASCII_DIGITS);      }
        if (logogramToggle.isSelected())    { characters.addAll(PasswordGenerator.LOGOGRAM_CHARS);    }
        if (punctuationToggle.isSelected()) { characters.addAll(PasswordGenerator.PUNCTUATION_CHARS); }
        if (quoteToggle.isSelected())       { characters.addAll(PasswordGenerator.QUOTE_CHARS);       }
        if (slashToggle.isSelected())       { characters.addAll(PasswordGenerator.SLASH_DASH_CHARS);  }
        if (mathToggle.isSelected())        { characters.addAll(PasswordGenerator.MATH_CHARS);        }
        if (braceToggle.isSelected())       { characters.addAll(PasswordGenerator.BRACE_CHARS);       }

        if (StringUtils.isNotEmpty(includeText.getText())) {
            characters.addAll(includeText.getText().chars().mapToObj(c -> (char) c).toList());
        }

        if (StringUtils.isNotEmpty(excludeText.getText())) {
            characters.removeAll(excludeText.getText().chars().mapToObj(c -> (char) c).toList());
        }

        if (similarCheck.isSelected()) {
            characters.removeAll(PasswordGenerator.SIMILAR_CHARS);
        }
        //@formatter:on
    }
}
