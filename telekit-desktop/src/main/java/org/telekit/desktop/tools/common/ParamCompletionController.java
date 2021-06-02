package org.telekit.desktop.tools.common;

import org.telekit.base.domain.KeyValue;
import org.telekit.base.i18n.Messages;
import org.telekit.controls.widgets.FilterTable;

import static org.telekit.base.i18n.BaseMessageKeys.KEY;
import static org.telekit.base.i18n.BaseMessageKeys.VALUE;
import static org.telekit.controls.util.TableUtils.createTableColumn;

public class ParamCompletionController extends FilterTable<KeyValue<String, String>> {

    public ParamCompletionController() {
        setColumns(
                createTableColumn(Messages.get(KEY), "key"),
                createTableColumn(Messages.get(VALUE), "value")
        );
        setPredicate((filter, row) -> row.getKey().contains(filter));
    }
}
