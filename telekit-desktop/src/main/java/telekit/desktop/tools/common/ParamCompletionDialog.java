package telekit.desktop.tools.common;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import telekit.base.domain.KeyValue;
import telekit.controls.util.NodeUtils;
import telekit.controls.util.TableUtils;
import telekit.controls.widgets.FilterTable;
import telekit.controls.widgets.OverlayDialog;

import java.util.List;
import java.util.function.Consumer;

import static telekit.base.i18n.I18n.t;
import static telekit.controls.i18n.ControlsMessages.*;
import static telekit.controls.util.Controls.button;
import static telekit.desktop.i18n.DesktopMessages.TOOLS_CHOOSE_VALUE;

public class ParamCompletionDialog extends OverlayDialog {

    FilterTable<KeyValue<String, String>> filterTable;
    Button commitBtn;

    private Consumer<KeyValue<String, String>> onCommitCallback;

    public ParamCompletionDialog() {
        super();
        createContent();
    }

    private void createContent() {
        filterTable = new FilterTable<>();
        filterTable.setColumns(
                TableUtils.createColumn(t(KEY), "key"),
                TableUtils.createColumn(t(VALUE), "value")
        );
        filterTable.setPredicate((filter, row) -> row.getKey().contains(filter));
        filterTable.getDataTable().setOnMouseClicked((MouseEvent e) -> {
            if (NodeUtils.isDoubleClick(e)) { commit(); }
        });

        commitBtn = button(t(ACTION_OK), null, "form-action");
        commitBtn.setDefaultButton(true);
        commitBtn.setOnAction(e -> commit());
        commitBtn.disableProperty().bind(
                filterTable.getDataTable().getSelectionModel().selectedItemProperty().isNull()
        );

        footerBox.getChildren().add(1, commitBtn);

        setContent(filterTable);
        setTitle(t(TOOLS_CHOOSE_VALUE));
        setPrefWidth(400);
        setPrefHeight(400);
    }

    public void setData(List<KeyValue<String, String>> data) {
        filterTable.setData(data);
    }

    public void setOnCommit(Consumer<KeyValue<String, String>> handler) {
        this.onCommitCallback = handler;
    }

    private void commit() {
        KeyValue<String, String> kv = filterTable.getSelectedItem();
        if (onCommitCallback != null && kv != null) {
            onCommitCallback.accept(kv);
        }
    }
}
