package org.telekit.ui.tools.common;

import org.telekit.base.domain.KeyValue;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.UILoader;
import org.telekit.controls.views.FilterTable;

import static org.telekit.base.i18n.BaseMessageKeys.KEY;
import static org.telekit.base.i18n.BaseMessageKeys.VALUE;
import static org.telekit.controls.util.ControlUtils.createTableColumn;

public final class Controllers {

    @SuppressWarnings("unchecked")
    public static FilterTable<KeyValue<String, String>> paramCompletionController() {
        FilterTable<KeyValue<String, String>> controller = UILoader.load(FilterTable.class);
        controller.setColumns(
                createTableColumn(Messages.get(KEY), "key"),
                createTableColumn(Messages.get(VALUE), "value")
        );
        controller.setPredicate((filter, row) -> row.getKey().contains(filter));
        return controller;
    }
}
