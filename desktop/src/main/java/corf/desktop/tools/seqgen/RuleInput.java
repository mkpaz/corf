package corf.desktop.tools.seqgen;

import atlantafx.base.util.DoubleStringConverter;
import atlantafx.base.util.IntegerStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import corf.base.collection.SequenceGenerator.Rule;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;

import static corf.base.i18n.I18n.t;

final class RuleInput {

    // this tool is supposed to be capable to generate sequences create phone numbers,
    // and standard length create E164 phone number is 11 (w/o plus sign)
    private static final double MAX_START = 100_000_000_000L; // 12
    private static final int MAX_STEP = 1_000;
    private static final int MAX_COUNT = 100_000;

    CheckBox enabledCheck;
    Spinner<Double> startSpinner;
    Spinner<Integer> stepSpinner;
    Spinner<Integer> countSpinner;

    final String itemName;

    public RuleInput(String itemName) {
        this.itemName = itemName;

        enabledCheck = new CheckBox("${" + itemName + "} ");
        enabledCheck.getStyleClass().add(ExtraStyles.MONOSPACE);

        startSpinner = new Spinner<>(0, MAX_START, 0, 1);
        startSpinner.setEditable(true);
        startSpinner.setMinWidth(300);
        startSpinner.setMaxWidth(Double.MAX_VALUE);
        startSpinner.setTooltip(new Tooltip(t(DM.START)));
        startSpinner.disableProperty().bind(enabledCheck.selectedProperty().not());
        DoubleStringConverter.createFor(startSpinner);

        stepSpinner = new Spinner<>(-MAX_STEP, MAX_STEP, 1, 1);
        stepSpinner.setPrefWidth(150);
        stepSpinner.setTooltip(new Tooltip(t(DM.STEP)));
        stepSpinner.disableProperty().bind(enabledCheck.selectedProperty().not());

        countSpinner = new Spinner<>(2, MAX_COUNT, 10, 1);
        countSpinner.setEditable(true);
        countSpinner.setPrefWidth(150);
        countSpinner.setTooltip(new Tooltip(t(DM.COUNT)));
        countSpinner.disableProperty().bind(enabledCheck.selectedProperty().not());
        IntegerStringConverter.createFor(countSpinner);
    }

    public boolean isEnabled() {
        return enabledCheck.isSelected();
    }

    public void setEnabled(boolean value) {
        enabledCheck.setSelected(value);
    }

    public BooleanProperty enabledProperty() {
        return enabledCheck.selectedProperty();
    }

    public Rule<String> createRule() {
        return new Rule<>(itemName, startSpinner.getValue(), stepSpinner.getValue(), countSpinner.getValue());
    }
}
