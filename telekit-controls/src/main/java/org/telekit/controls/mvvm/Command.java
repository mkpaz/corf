package org.telekit.controls.mvvm;

public interface Command extends Runnable {

    void execute();

    boolean isExecutable();

    @Override
    default void run() {
        execute();
    }
}