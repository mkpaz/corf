package org.telekit.base.domain;

public enum Language {

    EN("English"), RU("Русский");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}