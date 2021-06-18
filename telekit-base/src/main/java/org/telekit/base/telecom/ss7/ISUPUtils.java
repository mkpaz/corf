package org.telekit.base.telecom.ss7;

import org.telekit.base.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public final class ISUPUtils {

    public static final int MIN_CIC = 1;
    public static final int MAX_CIC = 1 << 12; // 2 ^ 12 = 4096

    public static List<Integer> getCICRange(int e1num) {
        if (e1num < 0) { return Collections.emptyList(); }
        return CollectionUtils.generate(e1num * 32 + 1, e1num * 32 + 31);
    }

    public static int findE1ByCIC(int cic) {
        if (cic < MIN_CIC || cic > MAX_CIC) { return -1; }
        return cic / 32;
    }

    public static int findTimeslotByCIC(int cic) {
        if (cic < MIN_CIC || cic > MAX_CIC) { return -1; }
        return cic % 32;
    }
}
