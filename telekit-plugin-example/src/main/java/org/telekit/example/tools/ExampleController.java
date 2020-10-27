package org.telekit.example.tools;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import org.telekit.base.Env;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.Dialogs;
import org.telekit.base.util.FileUtils;
import org.telekit.example.ExamplePlugin;
import org.telekit.example.MessageKeys;
import org.telekit.example.service.HelloService;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.telekit.example.ExamplePlugin.SAMPLE_PROPERTIES_FILE_NAME;

public class ExampleController extends Controller {

    public @FXML GridPane rootPane;
    private final HelloService helloService;

    @Inject
    public ExampleController(HelloService helloService) {
        this.helloService = helloService;
    }

    @FXML
    public void initialize() {
        System.out.println("Loading external resource:");
        System.out.println(loadConfig());

        System.out.println("Checking injected dependencies:");
        System.out.println(HelloService.class.getName() + " says: " + helloService.hello());
    }

    private Properties loadConfig() {
        File resource = Env.getPluginDataDir(ExamplePlugin.class).resolve(SAMPLE_PROPERTIES_FILE_NAME).toFile();
        return FileUtils.loadProperties(resource, StandardCharsets.UTF_8);
    }

    @FXML
    public void hello() {
        Dialogs.info()
                .title(Messages.get(MessageKeys.INFO))
                .content(helloService.hello())
                .owner(rootPane.getScene().getWindow())
                .build()
                .showAndWait();
    }

    @Override
    public void reset() {}
}
