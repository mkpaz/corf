package corf.desktop.tools.passgen;

import backbonefx.event.EventBus;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import corf.base.common.Initializer;
import corf.base.common.Lazy;
import corf.base.text.PasswordGenerator;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import static corf.base.i18n.I18n.t;

final class HexGenerator implements Generator {

    static final String NAME = "HEX";

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 128;
    private static final int DEFAULT_LENGTH = 16;

    private Slider lengthSlider;
    private final Lazy<VBox> view = new Lazy<>(this::createView);

    private final EventBus eventBus;

    HexGenerator(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String generate() {
        var len = view.initialized() ? (int) lengthSlider.getValue() : DEFAULT_LENGTH;
        return PasswordGenerator.hex(len);
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
                MIN_LENGTH, MAX_LENGTH, DEFAULT_LENGTH, len -> Math.round(len) + " " + t(DM.PASSGEN_CHARACTERS).toLowerCase()
        );
        lengthSlider = lengthBlock.getKey();

        var root = new VBox(Recommends.FORM_VGAP, lengthBlock.getValue());
        root.setFillWidth(true);

        return root;
    }
}
