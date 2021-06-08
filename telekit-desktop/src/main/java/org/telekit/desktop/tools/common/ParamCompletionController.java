package org.telekit.desktop.tools.common;

import org.telekit.base.domain.KeyValue;
import org.telekit.base.i18n.I18n;
import org.telekit.controls.widgets.FilterTable;

import static org.telekit.controls.i18n.ControlsMessages.KEY;
import static org.telekit.controls.i18n.ControlsMessages.VALUE;
import static org.telekit.controls.util.TableUtils.createTableColumn;

public class ParamCompletionController extends FilterTable<KeyValue<String, String>> {

    public ParamCompletionController() {
        setColumns(
                createTableColumn(I18n.t(KEY), "key"),
                createTableColumn(I18n.t(VALUE), "value")
        );
        setPredicate((filter, row) -> row.getKey().contains(filter));
    }
}
