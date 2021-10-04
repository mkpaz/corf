package telekit.example.service;

import telekit.base.di.DependencyModule;
import telekit.base.di.Provides;

import javax.inject.Singleton;

public class ExampleDependencyModule implements DependencyModule {

    @Provides
    @Singleton
    public HelloService fooService() {
        return new HelloService();
    }
}
