package org.telekit.base.util;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ArrayUtils {

    @SafeVarargs
    public static @Nullable <T> T[] merge(T[]... arrays) {
        int finalLength = 0;
        for (T[] array : arrays) {
            if (array == null || array.length == 0) continue;
            finalLength += array.length;
        }

        T[] dest = null;
        int destPos = 0;

        for (T[] array : arrays) {
            if (array == null || array.length == 0) continue;

            if (dest == null) {
                dest = Arrays.copyOf(array, finalLength);
                destPos = array.length;
            } else {
                System.arraycopy(array, 0, dest, destPos, array.length);
                destPos += array.length;
            }
        }
        return dest;
    }
}
