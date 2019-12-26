package corf.desktop.tools.common;

import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;
import corf.base.common.KeyValue;

import java.util.Collection;
import java.util.Map;

public interface TemplateWorker {

    Faker FAKER = new Faker();

    static boolean putParamReplacements(Map<String, String> map, Collection<Param> params) {
        boolean hasBlankValues = false;
        for (var param : params) {
            KeyValue<String, String> kv = param.resolve();
            map.put(kv.getKey(), kv.getValue());
            if (StringUtils.isBlank(kv.getValue())) {
                hasBlankValues = true;
            }
        }
        return hasBlankValues;
    }

    static void putCsvReplacements(Map<String, String> map, String[] row) {
        for (int i = 0; i < row.length; i++) {
            map.put("_csv" + i, row[i]);
        }
    }

    /**
     * Provides user an ability to include record index into template pattern.
     * _index0 - row index, starting from 0
     * _index1 - row index, starting from 1
     */
    static void putIndexReplacements(Map<String, String> map, int index) {
        map.put("_index0", String.valueOf(index));
        map.put("_index1", String.valueOf(index + 1));
    }
}
