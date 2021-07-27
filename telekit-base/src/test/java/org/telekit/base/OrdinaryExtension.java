package org.telekit.base;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.I18n;

public class OrdinaryExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().reload();
    }
}