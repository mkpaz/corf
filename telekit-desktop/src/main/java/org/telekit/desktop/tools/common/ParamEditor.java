package org.telekit.desktop.tools.common;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.TextFormatters;
import org.telekit.controls.widgets.OverlayDialog;
import org.telekit.desktop.tools.common.Param.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.controls.util.Controls.gridLabel;
import static org.telekit.desktop.i18n.DesktopMessages.TOOLS_ADD_PARAM;

public class ParamEditor extends OverlayDialog {

    TextField nameText;
    ComboBox<Type> typeChoice;
    Spinner<Integer> lengthSpinner;
    Button commitBtn;

    private Set<String> usedParamNames;
    private Param param;
    private Consumer<Param> onCommitCallback;

    public ParamEditor() {
        super();
    }

    @Override
    protected Region createContent() {
        // everything below initialized in parent constructor context
        usedParamNames = new HashSet<>();

        nameText = new TextField();
        nameText.setTextFormatter(
                // param name must not contain special characters
                TextFormatters.matches(Pattern.compile(PlaceholderReplacer.PLACEHOLDER_CHARACTERS))
        );

        typeChoice = new ComboBox<>(observableArrayList(Type.values()));
        GridPane.setHgrow(typeChoice, Priority.ALWAYS);
        typeChoice.setOnAction(e -> onParamTypeChanged());

        lengthSpinner = new Spinner<>(8, 32, 16, 1);
        lengthSpinner.setPrefWidth(200);

        commitBtn = button(t(ACTION_OK), null, "form-action");
        commitBtn.setDefaultButton(true);
        commitBtn.disableProperty().bind(createBooleanBinding(() -> {
            String text = trim(nameText.getText());
            // "_" is reserved for internal params
            return isEmpty(text) || text.startsWith("_") || usedParamNames.contains(text);
        }, nameText.textProperty()));
        commitBtn.setOnAction(e -> commit());

        footerBox.getChildren().add(1, commitBtn);
        bottomCloseBtn.setText(t(ACTION_CANCEL));

        // GRID

        GridPane grid = Containers.gridPane(20, 10, new Insets(20));

        grid.add(gridLabel("* " + t(NAME), HPos.RIGHT, nameText), 0, 0);
        grid.add(nameText, 1, 0);

        grid.add(gridLabel(t(TYPE), HPos.RIGHT, typeChoice), 0, 1);
        grid.add(typeChoice, 1, 1);

        grid.add(gridLabel(t(LENGTH), HPos.RIGHT, lengthSpinner), 0, 2);
        grid.add(lengthSpinner, 1, 2);

        grid.getColumnConstraints().addAll(columnConstraints(80, Priority.SOMETIMES), HGROW_ALWAYS);
        grid.getRowConstraints().addAll(VGROW_NEVER, VGROW_NEVER, VGROW_NEVER);

        setTitle(t(TOOLS_ADD_PARAM));
        setPrefWidth(300);

        return grid;
    }

    public void onParamTypeChanged() {
        Type selectedType = typeChoice.getSelectionModel().getSelectedItem();
        switch (selectedType) {
            case PASSWORD, PASSWORD_BASE64 -> lengthSpinner.setDisable(false);
            default -> lengthSpinner.setDisable(true);
        }
    }

    public void setData(Set<String> paramNames) {
        param = new Param();

        usedParamNames.clear();
        if (paramNames != null) { usedParamNames.addAll(paramNames); }

        nameText.setText(null);
        typeChoice.getSelectionModel().select(Type.CONSTANT);
        lengthSpinner.getValueFactory().setValue(Param.DEFAULT_PASSWORD_LENGTH);
        lengthSpinner.setDisable(true);
    }

    public void commit() {
        Type type = typeChoice.getSelectionModel().getSelectedItem();
        param.setName(trim(nameText.getText()));
        param.setType(type);

        if (type == Type.PASSWORD || type == Type.PASSWORD_BASE64) {
            param.setLength(lengthSpinner.getValue());
        } else {
            param.setLength(0);
        }

        if (onCommitCallback != null) { onCommitCallback.accept(new Param(param)); }
    }

    public void setOnCommit(Consumer<Param> handler) { this.onCommitCallback = handler; }
}
