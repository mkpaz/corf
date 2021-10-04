package telekit.base.util;

import telekit.base.domain.LineSeparator;

import java.util.Collection;

/** Convenient wrapper around StringBuilder */
public class TextBuilder {

    private final StringBuilder buf;
    private final String lineSeparator;

    public TextBuilder() {
        this(LineSeparator.UNIX);
    }

    public TextBuilder(LineSeparator lineSeparator) {
        this.buf = new StringBuilder();
        this.lineSeparator = lineSeparator.getCharacters();
    }

    public TextBuilder append(String str) {
        buf.append(str);
        return this;
    }

    public TextBuilder append(String... strings) {
        for (String str : strings) {
            buf.append(str);
        }
        return this;
    }

    public TextBuilder appendIf(boolean condition, String str) {
        if (condition) { append(str); }
        return this;
    }

    public TextBuilder appendLine(String str) {
        buf.append(str);
        newLine();
        return this;
    }

    public TextBuilder appendLine(String... strings) {
        for (String str : strings) {
            buf.append(str);
        }
        newLine();
        return this;
    }

    public TextBuilder appendLineIf(boolean condition, String str) {
        if (condition) { appendLine(str); }
        return this;
    }

    public TextBuilder appendLineIf(boolean condition, String... strings) {
        if (condition) { appendLine(strings); }
        return this;
    }

    public TextBuilder appendLines(Collection<String> lines) {
        for (String line : lines) {
            appendLine(line);
        }
        return this;
    }

    public TextBuilder appendLinesIf(boolean condition, Collection<String> lines) {
        if (condition) { appendLines(lines); }
        return this;
    }

    public TextBuilder newLine() {
        buf.append(lineSeparator);
        return this;
    }

    public TextBuilder newLineIf(boolean condition) {
        if (condition) { newLine(); }
        return this;
    }

    public StringBuilder unwrap() {
        return buf;
    }

    public String toString() {
        return buf.toString();
    }
}
