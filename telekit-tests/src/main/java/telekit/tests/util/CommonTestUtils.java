package telekit.tests.util;

import java.util.Random;

public final class CommonTestUtils {

    private static final Random RANDOM = new Random();

    public static <T extends Enum<?>> T randomEnumValue(Class<T> clazz) {
        int x = RANDOM.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
