package org.telekit.base.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class BaseMessagesBundleProvider {

    public static final String I18N_MESSAGES_PATH = "org.telekit.base.i18n.messages";

    public static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(I18N_MESSAGES_PATH, locale, BaseMessagesBundleProvider.class.getModule());
    }
}
