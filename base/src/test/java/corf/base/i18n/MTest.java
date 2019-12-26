package corf.base.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;

public class MTest {

    @Test
    public void ensureAllKeysArePresentInResourceBundle() {
        var keysTest = new I18nKeysTest(M.class, M.getLoader());
        assertThatCode(() -> keysTest.run(Locale.US)).doesNotThrowAnyException();
    }
}