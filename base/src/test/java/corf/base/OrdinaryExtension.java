package corf.base;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import corf.base.i18n.M;
import corf.base.i18n.I18n;

public class OrdinaryExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        I18n.getInstance().register(M.getLoader());
        I18n.getInstance().reload();
    }
}