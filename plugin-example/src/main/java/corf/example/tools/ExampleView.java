package corf.example.tools;

import backbonefx.di.Initializable;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import corf.base.desktop.Component;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.example.ExamplePlugin;
import corf.example.i18n.EM;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static corf.base.Env.getPluginConfigDir;

public final class ExampleView extends StackPane implements Component<ExampleView>, Initializable {

    private static final String CFG_FILE_NAME = "example.cfg";

    private final HelloService helloService;

    @Inject
    public ExampleView(HelloService helloService) {
        super();

        // adding custom CSS example
        ExamplePlugin.MODULE_PATH.concat("assets/styles.css").getResource()
                .ifPresent(r -> getStylesheets().add(r.toExternalForm()));

        this.helloService = helloService;
        createView();
    }

    private void createView() {
        var helloBtn = new Button("Say!");
        helloBtn.setDefaultButton(true);
        helloBtn.setOnAction(e -> {
            System.out.println(helloService.hello());
            Events.fire(Notification.info(helloService.hello()));
        });

        getChildren().add(helloBtn);
        setId("example-view");
        setMaxWidth(400);
        setPadding(new Insets(30));
    }

    @Override
    public void init() {
        loadConfig();
    }

    private void loadConfig() {
        System.out.println("Loading external resources:");
        var file = getPluginConfigDir(ExamplePlugin.class)
                .resolve(CFG_FILE_NAME)
                .toFile();

        try (var reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
            var cfg = new Properties();
            cfg.load(reader);
            System.out.println(cfg);
        } catch (IOException e) {
            throw new AppException(EM.MSG_GENERIC_IO_ERROR, e);
        }
    }

    @Override
    public ExampleView getRoot() {
        return this;
    }

    @Override
    public void reset() { }
}
