package telekit.base.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

public final class CommonUtils {

    public static <T, R> R map(T t, Function<T, R> func) {
        return map(t, func, null);
    }

    public static <T, R> R map(T t, Function<T, R> func, R defaultValue) {
        return t != null ? func.apply(t) : defaultValue;
    }

    public static Properties loadProperties(File file) {
        return loadProperties(file, StandardCharsets.UTF_8);
    }

    public static Properties loadProperties(File file, Charset charset) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Catches and ignores any raised exceptions */
    public static void hush(Runnable runnable) {
        try {
            Objects.requireNonNull(runnable).run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
