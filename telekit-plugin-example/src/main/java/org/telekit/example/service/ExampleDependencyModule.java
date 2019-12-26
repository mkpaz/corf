package org.telekit.example.service;

import org.telekit.base.Provides;
import org.telekit.base.plugin.DependencyModule;

import javax.inject.Singleton;

public class ExampleDependencyModule implements DependencyModule {

    @Provides
    @Singleton
    public FooService fooService() {
        return new FooService();
    }
}
