package telekit.base.i18n;

import org.junit.jupiter.api.Test;
import telekit.base.OrdinaryTest;

import static org.assertj.core.api.Assertions.assertThat;
import static telekit.base.i18n.I18n.t;

@OrdinaryTest
public class I18nTest {

    @Test
    public void testArgSubstitution() {
        String msg = t(BaseMessages.MSG_INVALID_PARAM, "12345");
        assertThat(msg).contains("12345");
    }
}