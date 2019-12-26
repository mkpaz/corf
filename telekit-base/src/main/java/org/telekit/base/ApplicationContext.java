package org.telekit.base;

import org.telekit.base.feather.Feather;
import org.telekit.base.feather.Key;

import javax.inject.Provider;

@SuppressWarnings("unused")
public final class ApplicationContext {

    private ApplicationContext() {}

    private static class InstanceHolder {

        private static final ApplicationContext INSTANCE = new ApplicationContext();
    }

    public static ApplicationContext getInstance() {
        return InstanceHolder.INSTANCE;
    }

    ///////////////////////////////////////////////////////////////////////////

    private Feather injector = Feather.with();

    public synchronized void configure(Object... modules) {
        injector = Feather.with(modules);
    }

    public synchronized void configure(Iterable<?> modules) {
        injector = Feather.with(modules);
    }

    public <T> T getBean(Class<T> type) {
        return injector.instance(type);
    }

    public <T> T getBean(Key<T> key) {
        return injector.instance(key);
    }

    public <T> Provider<T> getProvider(Class<T> type) {
        return injector.provider(type);
    }

    public <T> Provider<T> getProvider(Key<T> key) {
        return injector.provider(key);
    }

    public void injectFields(Object target) {
        injector.injectFields(target);
    }

}
