package org.telekit.desktop.tools.common;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.desktop.*;
import org.telekit.base.i18n.I18n;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.TextFormatters;
import org.telekit.desktop.IconCache;
import org.telekit.desktop.tools.common.Param.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.desktop.IconCache.ICON_APP;
import static org.telekit.desktop.i18n.DesktopMessages.TOOLS_ADD_PARAM;

@FxmlPath("/org/telekit/desktop/tools/common/param.fxml")
public class ParamController implements Component, ModalController {

    public @FXML GridPane rootPane;
    public @FXML TextField tfName;
    public @FXML ComboBox<Type> cmbType;
    public @FXML Spinner<Integer> spnLength;
    public @FXML Button btnSubmit;

    private final Set<String> usedParamNames = new HashSet<>();
    private Param param;
    private Consumer<Param> onSubmitCallback;
    private Runnable onCancelCallback;

    @FXML
    public void initialize() {
        param = createDefaultParam();
        // param name must not contain special characters
        tfName.setTextFormatter(TextFormatters.matches(Pattern.compile(PlaceholderReplacer.PLACEHOLDER_CHARACTERS)));
        btnSubmit.disableProperty().bind(BindUtils.or(
                BindUtils.isBlank(tfName.textProperty()),
                // "_" is reserved for internal params
                BindUtils.startsWith(tfName.textProperty(), "_", StringUtils::trim),
                BindUtils.contains(tfName.textProperty(), usedParamNames, StringUtils::trim)
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

        if (onSubmitCallback != null) { onSubmitCallback.accept(new Param(param)); }
    }

    @FXML
    public void cancel() {
        if (onCancelCallback != null) { onCancelCallback.run(); }
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

    @Override
    public Region getRoot() { return rootPane; }

    public void setOnSubmit(Consumer<Param> handler) { this.onSubmitCallback = handler; }

    @Override
    public Runnable getOnCloseRequest() { return onCancelCallback; }

    @Override
    public void setOnCloseRequest(Runnable handler) { this.onCancelCallback = handler; }

    public static ModalDialog<ParamController> createDialog(Window window) {
        ParamController controller = ViewLoader.load(ParamController.class);
        return ModalDialog.builder(controller, window.getScene().getWindow())
                .title(I18n.t(TOOLS_ADD_PARAM))
                .inheritStyles()
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build();
    }
}
