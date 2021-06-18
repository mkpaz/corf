package org.telekit.base.service;

import org.telekit.base.domain.Entity;

import java.util.Comparator;
import java.util.UUID;

public class FooEntity extends Entity<FooEntity, UUID> {

    public static final Comparator<FooEntity> COMPARATOR = Comparator.comparing(FooEntity::getId);
    private String name;

    public FooEntity() {
        super(UUID.randomUUID());
    }

    public FooEntity(UUID id) {
        super(id);
    }

    public FooEntity(UUID id, String name) {
        super(id);
        this.name = name;
    }

    public FooEntity(FooEntity that) {
        super(that);
        this.name = that.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public FooEntity deepCopy() {
        return new FooEntity(this);
    }

    @Override
    public int compareTo(FooEntity that) {
        return COMPARATOR.compare(this, that);
    }

    @Override
    public String toString() {
        return "FooEntity{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
