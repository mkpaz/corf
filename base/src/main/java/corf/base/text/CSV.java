package corf.base.text;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public final class CSV {

    public static final String COMMA_OR_SEMICOLON = "[,;]";

    private final String[][] data;

    public CSV(String[][] data) {
        Objects.requireNonNull(data, "data");
        this.data = data;
    }

    public int length() {
        return data.length;
    }

    public String[] get(int rowNum) {
        return data[rowNum];
    }

    public String get(int rowNum, int colNum) {
        return data[rowNum][colNum];
    }

    public String[][] getData() {
        return data;
    }

    public String[][] getDataRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

    public Iterator<String[]> iterator() {
        return Arrays.stream(data).iterator();
    }

    public static CSV from(String text) {
        return from(text, COMMA_OR_SEMICOLON);
    }

    @SuppressWarnings("StringSplitter")
    public static CSV from(String text, @Nullable String sep) {
        Objects.requireNonNull(text, "text");

        final var csvText = Objects.requireNonNullElse(text, "");
        final var csvSep = Objects.requireNonNullElse(sep, COMMA_OR_SEMICOLON);

        if (csvText.isBlank()) {
            return new CSV(new String[][] { });
        }

        String[] rows = csvText.split(LineSeparator.LINE_SPLIT_PATTERN);
        String[][] csv = new String[rows.length][];

        var i = 0;
        for (var row : rows) {
            if (StringUtils.isBlank(row)) {
                continue;
            }
            csv[i] = row.split(csvSep);
            i++;
        }
        return new CSV(Arrays.copyOfRange(csv, 0, i));
    }
}
