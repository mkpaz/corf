package corf.base;

import backbonefx.di.Feather;
import backbonefx.mvvm.View;
import org.jetbrains.annotations.Nullable;
import corf.base.plugin.DependencyModule;

import java.util.Collections;
import java.util.Objects;

/** The global dependency injector. */
public final class Injector {

    private Feather feather = Feather.with();

    @SafeVarargs
    public synchronized final <T extends DependencyModule> void configure(T... modules) {
        feather = Feather.with((Object[]) modules);
    }

    public synchronized <T extends DependencyModule> void configure(@Nullable Iterable<T> modules) {
        feather = Feather.with(Objects.requireNonNullElse(modules, Collections.emptyList()));
    }

    public <T> T getBean(Class<T> type) {
        return feather.instance(type);
    }

    public static <T extends View<?, ?>> T getView(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return Injector.getInstance().getBean(clazz);
    }

    ///////////////////////////////////////////////////////////////////////////

    private Injector() { }

    private static class InstanceHolder {

        private static final Injector INSTANCE = new Injector();
    }

    public static Injector getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
