package org.telekit.controls.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class ControlsMessagesBundleProvider {

    public static final String I18N_MESSAGES_PATH = "org.telekit.controls.i18n.messages";

    public static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(I18N_MESSAGES_PATH, locale, ControlsMessagesBundleProvider.class.getModule());
    }
}
