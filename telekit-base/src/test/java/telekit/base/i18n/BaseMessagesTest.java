package telekit.base.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;

public class BaseMessagesTest {

    @Test
    public void ensureAllKeysArePresentInResourceBundle() {
        I18nKeysTest test = new I18nKeysTest(BaseMessages.class, BaseMessages.getLoader());
        assertThatCode(() -> test.run(Locale.US)).doesNotThrowAnyException();
        assertThatCode(() -> test.run(new Locale("ru"))).doesNotThrowAnyException();
    }
}