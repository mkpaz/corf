package telekit.controls.i18n;

import org.junit.jupiter.api.Test;
import telekit.base.i18n.I18nKeysTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;

public class ControlsMessagesTest {

    @Test
    public void ensureAllKeysArePresentInResourceBundle() {
        I18nKeysTest test = new I18nKeysTest(ControlsMessages.class, ControlsMessages.getLoader());
        assertThatCode(() -> test.run(Locale.US)).doesNotThrowAnyException();
        assertThatCode(() -> test.run(new Locale("ru"))).doesNotThrowAnyException();
    }
}