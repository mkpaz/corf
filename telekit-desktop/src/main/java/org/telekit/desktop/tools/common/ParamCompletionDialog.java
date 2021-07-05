package org.telekit.desktop.tools.common;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.telekit.base.domain.KeyValue;
import org.telekit.controls.util.NodeUtils;
import org.telekit.controls.util.TableUtils;
import org.telekit.controls.widgets.FilterTable;
import org.telekit.controls.widgets.OverlayDialog;

import java.util.List;
import java.util.function.Consumer;

import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.desktop.i18n.DesktopMessages.TOOLS_CHOOSE_VALUE;

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
        if (onCommitCallback != null) { onCommitCallback.accept(filterTable.getSelectedItem()); }
    }
}
