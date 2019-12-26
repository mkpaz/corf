package corf.desktop.tools.passgen;

import backbonefx.event.EventBus;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import corf.base.common.Initializer;
import corf.base.common.Lazy;
import corf.base.text.PasswordGenerator;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import static corf.base.i18n.I18n.t;

final class KatakanaGenerator implements Generator {

    static final String NAME = t(DM.PASSGEN_KATAKANA);

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 128;
    private static final int DEFAULT_LENGTH = 16;

    private Slider lengthSlider;
    private ToggleGroup caseToggleGroup;

    private final Lazy<VBox> view = new Lazy<>(this::createView);
    private final EventBus eventBus;

    KatakanaGenerator(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String generate() {
        var len = view.initialized() ? (int) lengthSlider.getValue() : DEFAULT_LENGTH;
        boolean lowercase = false, capitalize = false, uppercase = false;

        if (view.initialized()
                && caseToggleGroup.getSelectedToggle() != null
                && caseToggleGroup.getSelectedToggle().getUserData() instanceof String type) {
            if (UPPERCASE.equals(type)) { uppercase = true; }
            if (TITLECASE.equals(type)) { capitalize = true; }
            if (LOWERCASE.equals(type)) { lowercase = true; }
        }

        var password = PasswordGenerator.katakana(len, capitalize);
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
                MIN_LENGTH, MAX_LENGTH, DEFAULT_LENGTH, len -> Math.round(len) + " " + t(DM.PASSGEN_CHARACTERS)
        );
        lengthSlider = lengthBlock.getKey();

        Pair<ToggleGroup, HBox> caseBlock = createCaseSwitcher();
        caseToggleGroup = caseBlock.getKey();

        var caseBox = new HBox(Recommends.FORM_HGAP, new Label(t(DM.PASSGEN_LETTER_CASE)), caseBlock.getValue());
        caseBox.setAlignment(Pos.CENTER_LEFT);

        var root = new VBox(
                Recommends.FORM_VGAP,
                lengthBlock.getValue(),
                caseBox
        );
        root.setFillWidth(true);

        return root;
    }
}
