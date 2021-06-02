package org.telekit.example.service;

import org.telekit.base.di.DependencyModule;
import org.telekit.base.di.Provides;

import javax.inject.Singleton;

public class ExampleDependencyModule implements DependencyModule {

    @Provides
    @Singleton
    public HelloService fooService() {
        return new HelloService();
    }
}
