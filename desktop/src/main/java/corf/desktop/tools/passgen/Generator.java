package corf.desktop.tools.passgen;

import backbonefx.event.EventBus;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.function.Function;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

interface Generator {

    String LOWERCASE = "lowercase";
    String TITLECASE = "TitleCase";
    String UPPERCASE = "UPPERCASE";

    String getName();

    String generate();

    Node getView();

    EventBus getEventBus();

    default Pair<Slider, VBox> createLengthSlider(double min, double max, double value,
                                                  Function<Double, String> lengthConverter) {
        var titleLabel = new Label(t(DM.LENGTH));

        var slider = new Slider(min, max, value);
        slider.setBlockIncrement(1);

        ChangeListener<Object> sliderListener = (obs, old, val) -> {
            // don't spam event when thumb is being dragged
            if (!slider.isValueChanging()) {
                getEventBus().publish(new ChangeEvent());
            }
        }; // we need to set two bindings, one for mouse and second for keyboard
        slider.valueChangingProperty().addListener(sliderListener);
        slider.valueProperty().addListener(sliderListener);

        var lengthLabel = new Label();
        lengthLabel.getStyleClass().addAll(TEXT_SUBTLE, TEXT_SMALL);
        lengthLabel.textProperty().bind(Bindings.createStringBinding(
                () -> lengthConverter.apply(slider.getValue()),
                slider.valueProperty()
        ));

        var lengthBox = new HBox(lengthLabel);
        lengthBox.setAlignment(Pos.CENTER_RIGHT);

        var root = new VBox(Recommends.CAPTION_MARGIN, titleLabel, slider, lengthBox);

        return new Pair<>(slider, root);
    }

    default Pair<ToggleGroup, HBox> createCaseSwitcher() {
        var toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val == null) { // at least one toggle must always be selected
                old.setSelected(true);
                return;
            }

            getEventBus().publish(new ChangeEvent());
        });

        var lowerToggle = new ToggleButton(LOWERCASE);
        lowerToggle.setToggleGroup(toggleGroup);
        lowerToggle.getStyleClass().add(LEFT_PILL);
        lowerToggle.setUserData(LOWERCASE);
        lowerToggle.setPrefWidth(120);

        var titleToggle = new ToggleButton(TITLECASE);
        titleToggle.setToggleGroup(toggleGroup);
        titleToggle.getStyleClass().add(CENTER_PILL);
        titleToggle.setUserData(TITLECASE);
        titleToggle.setPrefWidth(120);
        titleToggle.setSelected(true);

        var upperToggle = new ToggleButton(UPPERCASE);
        upperToggle.setToggleGroup(toggleGroup);
        upperToggle.getStyleClass().add(RIGHT_PILL);
        upperToggle.setUserData(UPPERCASE);
        upperToggle.setPrefWidth(120);

        var root = new HBox(lowerToggle, titleToggle, upperToggle);
        root.setAlignment(Pos.CENTER_LEFT);

        return new Pair<>(toggleGroup, root);
    }
}
