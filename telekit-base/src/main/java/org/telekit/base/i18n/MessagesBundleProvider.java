package org.telekit.base.i18n;

import org.telekit.base.i18n.Messages;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessagesBundleProvider {

    public static final String I18N_MESSAGES_PATH = "org.telekit.base.i18n.messages";

    public static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(I18N_MESSAGES_PATH, locale, Messages.class.getModule());
    }
}
