package org.telekit.ui.tools.import_file_builder;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.telekit.base.EventBus;
import org.telekit.base.ui.Controller;
import org.telekit.controls.util.ExtraBindings;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.ui.tools.import_file_builder.Param.Type;

import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

public class ParamModalController extends Controller {

    public @FXML GridPane rootPane;
    public @FXML TextField tfName;
    public @FXML ComboBox<Type> cmbType;
    public @FXML Spinner<Integer> spnLength;
    public @FXML Button btnApply;

    private Param param;
    private Set<String> usedParamNames;

    @FXML
    public void initialize() {
        BooleanBinding isNameNotUnique = isNameNotUnique(tfName.textProperty());

        // param name disallowed to use special characters
        Pattern pattern = Pattern.compile(PlaceholderReplacer.PLACEHOLDER_CHARACTERS);
        UnaryOperator<TextFormatter.Change> filter = event -> {
            if (isEmpty(event.getControlNewText()) || pattern.matcher(event.getControlNewText()).matches()) {
                return event;
            } else {
                return null;
            }
        };
        tfName.setTextFormatter(new TextFormatter<>(filter));

        btnApply.disableProperty().bind(
                Bindings.or(
                        ExtraBindings.isBlank(tfName.textProperty()),
                        isNameNotUnique
                ));

        cmbType.getItems().addAll(Type.values());
    }

    @FXML
    public void onParamTypeChanged() {
        Type selectedType = cmbType.getSelectionModel().getSelectedItem();
        switch (selectedType) {
            case PASSWORD, PASSWORD_BASE64 -> spnLength.setDisable(false);
            default -> spnLength.setDisable(true);
        }
    }

    public void setData(Set<String> usedParamNames) {
        this.usedParamNames = usedParamNames;
    }

    @FXML
    public void apply() {
        Type type = cmbType.getSelectionModel().getSelectedItem();
        param.setName(trim(tfName.getText()));
        param.setType(type);

        if (type == Type.PASSWORD || type == Type.PASSWORD_BASE64) {
            param.setLength(spnLength.getValue());
        } else {
            param.setLength(0);
        }

        rootPane.getScene().getWindow().hide();
        EventBus.getInstance().publish(new ParamUpdateEvent(new Param(param)));
    }

    @FXML
    public void cancel() {
        rootPane.getScene().getWindow().hide();
    }

    @Override
    public void reset() {
        this.param = new Param();

        tfName.setText(null);
        cmbType.getSelectionModel().select(Type.CONSTANT);
        spnLength.getValueFactory().setValue(Param.DEFAULT_PASSWORD_LENGTH);
        spnLength.setDisable(true);
    }

    private BooleanBinding isNameNotUnique(StringProperty textProperty) {
        return Bindings.createBooleanBinding(
                () -> {
                    if (textProperty == null || textProperty.get() == null || usedParamNames == null) {
                        return false;
                    }
                    String paramName = trim(textProperty.get());
                    return usedParamNames.contains(paramName) || paramName.startsWith("_");
                },
                textProperty
        );
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class ParamUpdateEvent {

        private final Param param;

        public ParamUpdateEvent(Param param) {
            this.param = param;
        }

        public Param getParam() {
            return param;
        }
    }
}
