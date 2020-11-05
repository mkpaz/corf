package org.telekit.base;

import org.telekit.base.i18n.BaseMessagesBundleProvider;
import org.telekit.base.i18n.Messages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;

public class TestUtils {

    private static final Random RANDOM = new Random();

    public static void loadResourceBundle() {
        Messages.getInstance().load(
                BaseMessagesBundleProvider.getBundle(Locale.getDefault()),
                BaseMessagesBundleProvider.class.getName()
        );
    }

    public static Path getTempDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    public static <T extends Enum<?>> T randomEnumValue(Class<T> clazz){
        int x = RANDOM.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
