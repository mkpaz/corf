package org.telekit.desktop.tools.common;

import org.telekit.base.desktop.ModalController;
import org.telekit.base.domain.KeyValue;
import org.telekit.base.i18n.I18n;
import org.telekit.controls.widgets.FilterTable;

import static org.telekit.controls.i18n.ControlsMessages.KEY;
import static org.telekit.controls.i18n.ControlsMessages.VALUE;
import static org.telekit.controls.util.Controls.tableColumn;

public class ParamCompletionController extends FilterTable<KeyValue<String, String>> implements ModalController {

    public ParamCompletionController() {
        setColumns(
                tableColumn(I18n.t(KEY), "key"),
                tableColumn(I18n.t(VALUE), "value")
        );
        setPredicate((filter, row) -> row.getKey().contains(filter));
    }

    @Override
    public Runnable getOnCloseRequest() { return onCancelCallback; }

    @Override
    public void setOnCloseRequest(Runnable handler) { this.onCancelCallback = handler; }
}
