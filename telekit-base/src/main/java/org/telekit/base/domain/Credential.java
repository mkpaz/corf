package org.telekit.base.domain;

import java.util.Objects;

public abstract class Credential {

    protected final String name;

    public Credential(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Credential{" +
                "name='" + name + '\'' +
                '}';
    }
}
