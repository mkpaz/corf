package telekit.base.di;

import telekit.base.di.feather.Feather;

import javax.inject.Provider;

public final class Injector {

    private Injector() {}

    private static class InstanceHolder {

        private static final Injector INSTANCE = new Injector();
    }

    public static Injector getInstance() {
        return InstanceHolder.INSTANCE;
    }

    ///////////////////////////////////////////////////////////////////////////

    private Feather feather = Feather.with();

    @SafeVarargs
    public synchronized final <T extends DependencyModule> void configure(T... modules) {
        feather = Feather.with((Object[]) modules);
    }

    public synchronized final <T extends DependencyModule> void configure(Iterable<T> modules) {
        feather = Feather.with(modules);
    }

    public <T> T getBean(Class<T> type) {
        return feather.instance(type);
    }

    public <T> Provider<T> getProvider(Class<T> type) {
        return feather.provider(type);
    }
}
