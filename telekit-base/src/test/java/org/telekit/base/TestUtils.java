package org.telekit.base;

import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.I18n;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class TestUtils {

    private static final Random RANDOM = new Random();

    public static void loadResourceBundle() {
        I18n.getInstance().register(BaseMessages.getLoader());
        I18n.getInstance().reload();
    }

    public static Path getTempDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    public static <T extends Enum<?>> T randomEnumValue(Class<T> clazz) {
        int x = RANDOM.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
