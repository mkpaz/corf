package org.telekit.base.util;

public class TextBuilder {

    public static final String LINE_SEPARATOR = "\n";

    private final StringBuilder sb;
    private final String eol;

    public TextBuilder() {
        this(LINE_SEPARATOR);
    }

    public TextBuilder(String eol) {
        this.sb = new StringBuilder();
        this.eol = eol;
    }

    public TextBuilder append(String str) {
        sb.append(str);
        return this;
    }

    public TextBuilder append(String... strings) {
        for (String str : strings) {
            sb.append(str);
        }
        return this;
    }

    public TextBuilder appendIf(boolean condition, String str) {
        if (condition) {
            append(str);
        }
        return this;
    }

    public TextBuilder appendLine(String str) {
        sb.append(str);
        newLine();
        return this;
    }

    public TextBuilder appendLineIf(boolean condition, String str) {
        if (condition) {
            appendLine(str);
        }
        return this;
    }

    public TextBuilder appendLine(String... strings) {
        for (String str : strings) {
            sb.append(str);
        }
        newLine();
        return this;
    }

    public TextBuilder appendLineIf(boolean condition, String... strings) {
        if (condition) {
            appendLine(strings);
        }
        return this;
    }

    public TextBuilder newLine() {
        sb.append(eol);
        return this;
    }

    public TextBuilder newLineIf(boolean condition) {
        if (condition) {
            newLine();
        }
        return this;
    }

    public String toString() {
        return sb.toString();
    }
}
