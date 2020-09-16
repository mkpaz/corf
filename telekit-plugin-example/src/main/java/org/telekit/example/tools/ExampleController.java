package org.telekit.example.tools;

import javafx.fxml.FXML;
import org.telekit.base.Settings;
import org.telekit.base.fx.Controller;
import org.telekit.base.fx.Dialogs;
import org.telekit.base.util.FileUtils;
import org.telekit.example.ExamplePlugin;
import org.telekit.example.service.FooService;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.telekit.example.ExamplePlugin.SAMPLE_PROPERTIES;

public class ExampleController extends Controller {

    private final FooService fooService;

    @Inject
    public ExampleController(FooService fooService) {
        this.fooService = fooService;
    }

    @FXML
    public void initialize() {
        System.out.println("Loading external resource:");
        System.out.println(loadConfig());

        System.out.println("Checking injected dependencies:");
        System.out.println(FooService.class.getName() + " says: " + fooService.foo());
    }

    private Properties loadConfig() {
        File resource = Settings.getPluginDataDir(ExamplePlugin.class).resolve(SAMPLE_PROPERTIES).toFile();
        return FileUtils.loadProperties(resource, StandardCharsets.UTF_8);
    }

    @FXML
    public void hello() {
        Dialogs.info()
                .title("Info")
                .content(fooService.foo())
                .build()
                .showAndWait();
    }

    @Override
    public void reset() {}
}
