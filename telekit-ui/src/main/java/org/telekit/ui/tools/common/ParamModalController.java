package org.telekit.ui.tools.common;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.event.CancelEvent;
import org.telekit.base.event.SubmitEvent;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.Controller;
import org.telekit.controls.components.dialogs.Dialogs;
import org.telekit.base.ui.IconCache;
import org.telekit.base.ui.UILoader;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.controls.format.TextFormatters;
import org.telekit.controls.util.BooleanBindings;
import org.telekit.ui.domain.FXMLView;
import org.telekit.ui.tools.common.Param.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.ui.MessageKeys.TOOLS_ADD_PARAM;

public class ParamModalController extends Controller {

    public @FXML GridPane rootPane;
    public @FXML TextField tfName;
    public @FXML ComboBox<Type> cmbType;
    public @FXML Spinner<Integer> spnLength;
    public @FXML Button btnSubmit;

    private final Set<String> usedParamNames = new HashSet<>();
    private Param param;

    @FXML
    public void initialize() {
        param = createDefaultParam();
        // param name must not contain special characters
        tfName.setTextFormatter(TextFormatters.matches(Pattern.compile(PlaceholderReplacer.PLACEHOLDER_CHARACTERS)));
        btnSubmit.disableProperty().bind(BooleanBindings.or(
                BooleanBindings.isBlank(tfName.textProperty()),
                // "_" is reserved for internal params
                BooleanBindings.startsWith(tfName.textProperty(), "_", StringUtils::trim),
                BooleanBindings.contains(tfName.textProperty(), usedParamNames, StringUtils::trim)
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

    @FXML
    public void submit() {
        Type type = cmbType.getSelectionModel().getSelectedItem();
        param.setName(trim(tfName.getText()));
        param.setType(type);

        if (type == Type.PASSWORD || type == Type.PASSWORD_BASE64) {
            param.setLength(spnLength.getValue());
        } else {
            param.setLength(0);
        }

        eventBus.publish(new SubmitEvent<>(new Param(param)));
    }

    @FXML
    public void cancel() {
        eventBus.publish(new CancelEvent());
    }

    @Override
    public void reset() {
        param = createDefaultParam();
    }

    public void setData(Set<String> paramNames) {
        // usedParamNames is a part of boolean binding,
        // if link is updated binding should be updated too
        usedParamNames.clear();
        if (paramNames != null) usedParamNames.addAll(paramNames);
    }

    private Param createDefaultParam() {
        Param param = new Param();
        tfName.setText(null);
        cmbType.getSelectionModel().select(Type.CONSTANT);
        spnLength.getValueFactory().setValue(Param.DEFAULT_PASSWORD_LENGTH);
        spnLength.setDisable(true);
        return param;
    }

    public static ParamModalController create(Window window) {
        Controller controller = UILoader.load(FXMLView.COMMON_PARAM.getLocation(), Messages.getInstance());
        Dialogs.modal(controller.getParent(), window)
                .title(Messages.get(TOOLS_ADD_PARAM))
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build();
        return (ParamModalController) controller;
    }
}
