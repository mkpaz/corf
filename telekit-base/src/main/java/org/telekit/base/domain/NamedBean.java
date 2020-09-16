package org.telekit.base.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class NamedBean<T extends NamedBean<T>> implements Comparable<T> {

    public static final Comparator<NamedBean> COMPARATOR = Comparator.comparing(NamedBean::getName);

    private String id;
    private String name;

    public NamedBean() {}

    public NamedBean(String id) {
        this.id = id;
    }

    public NamedBean(T bean) {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NotNull NamedBean other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedBean that = (NamedBean) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NamedBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public abstract T deepCopy();
}
