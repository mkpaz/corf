package corf.desktop.i18n;

import org.junit.jupiter.api.Test;
import corf.base.i18n.I18nKeysTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;

class DMTest {

    @Test
    public void ensureAllKeysArePresentInResourceBundle() {
        I18nKeysTest test = new I18nKeysTest(DM.class, DM.getLoader());
        assertThatCode(() -> test.run(Locale.US)).doesNotThrowAnyException();
    }
}
