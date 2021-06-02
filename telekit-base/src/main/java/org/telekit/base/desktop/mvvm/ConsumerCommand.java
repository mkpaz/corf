package org.telekit.base.desktop.mvvm;

import java.util.function.Consumer;

public interface ConsumerCommand<T> extends Consumer<T> {

    void execute(T t);

    boolean isExecutable();

    @Override
    default void accept(T t) {
        execute(t);
    }
}
