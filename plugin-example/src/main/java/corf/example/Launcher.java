package corf.example;

import backbonefx.mvvm.View;
import corf.base.plugin.DependencyModule;
import corf.base.plugin.PluginLauncher;
import corf.example.tools.ExampleView;

import java.util.List;

public class Launcher extends PluginLauncher<ExampleView> {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected Class<? extends View<ExampleView, ?>> getView() {
        return ExampleView.class;
    }

    @Override
    protected List<DependencyModule> getDependencyModules() {
        return List.of(new ExampleDependencyModule());
    }
}
