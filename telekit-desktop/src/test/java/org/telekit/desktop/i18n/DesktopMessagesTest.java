package org.telekit.desktop.i18n;

import org.junit.jupiter.api.Test;
import org.telekit.base.i18n.I18nKeysTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;

class DesktopMessagesTest {

    @Test
    public void ensureAllKeysArePresentInResourceBundle() {
        I18nKeysTest test = new I18nKeysTest(DesktopMessages.class, DesktopMessages.getLoader());
        assertThatCode(() -> test.run(Locale.US)).doesNotThrowAnyException();
        assertThatCode(() -> test.run(new Locale("ru"))).doesNotThrowAnyException();
    }
}