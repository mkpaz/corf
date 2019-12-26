package corf.example;

import backbonefx.di.Provides;
import jakarta.inject.Singleton;
import corf.base.plugin.DependencyModule;
import corf.example.tools.HelloService;

public final class ExampleDependencyModule implements DependencyModule {

    @Provides
    @Singleton
    public HelloService helloService() {
        return new HelloService();
    }
}
