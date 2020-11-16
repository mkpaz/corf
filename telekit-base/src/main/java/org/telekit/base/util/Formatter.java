package org.telekit.base.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import static org.telekit.base.util.NumberUtils.ensureRange;

public class Formatter {

    /**
     * Converts size in bytes to human readable string.
     * <pre>
     *             0:        0 B
     *            27:       27 B
     *           999:      999 B
     *          1000:     1.0 kB
     *          1023:     1.0 kB
     *          1024:     1.0 kB
     *          1728:     1.7 kB
     *        110592:   110.6 kB
     *       7077888:     7.1 MB
     *     452984832:   453.0 MB
     *   28991029248:    29.0 GB
     * 1855425871872:     1.9 TB
     * </pre>
     */
    public static String byteCountToDisplaySize(long bytes, int precision) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        String pattern = "%." + ensureRange(precision, 0, 10) + "f %cB";
        return String.format(pattern, bytes / 1000.0, ci.current());
    }
}
