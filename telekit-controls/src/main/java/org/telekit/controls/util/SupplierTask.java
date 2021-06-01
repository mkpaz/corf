package org.telekit.controls.util;

import javafx.concurrent.Task;

import java.util.function.Supplier;

public class SupplierTask<T> extends Task<T> {

    protected final Supplier<T> supplier;

    public SupplierTask(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T call() {
        return supplier.get();
    }
}