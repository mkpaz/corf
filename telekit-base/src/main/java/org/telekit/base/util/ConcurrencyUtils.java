package org.telekit.base.util;

import java.time.Duration;

public final class ConcurrencyUtils {

    public static void sleep(long millis) {
        sleep(Duration.ofMillis(millis));
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
