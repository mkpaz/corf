package org.telekit.base.domain;

public enum LineSeparator {

    UNIX ("\n"), WINDOWS ("\r\n");

    public static final String LINE_SPLIT_PATTERN = "\\r?\\n";

    private final String characters;

    public String getCharacters() {
        return characters;
    }

    LineSeparator(String characters) {
        this.characters = characters;
    }
}
