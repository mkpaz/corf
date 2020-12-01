package org.telekit.base.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Credential {

    protected final String name;

    public Credential(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Credential{" +
                "name='" + name + '\'' +
                '}';
    }
}
