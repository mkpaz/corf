package telekit.example.tools;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import telekit.base.desktop.Component;
import telekit.base.desktop.FxmlPath;
import telekit.base.i18n.I18n;
import telekit.base.util.CommonUtils;
import telekit.controls.dialogs.Dialogs;
import telekit.controls.i18n.ControlsMessages;
import telekit.example.ExamplePlugin;
import telekit.example.service.HelloService;

import javax.inject.Inject;
import java.io.File;
import java.util.Properties;

import static telekit.base.Env.getPluginConfigDir;
import static telekit.example.ExamplePlugin.SAMPLE_PROPERTIES_FILE_NAME;

@FxmlPath("/telekit/example/tools/example.fxml")
public class ExampleController implements Component {

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
        File resource = getPluginConfigDir(ExamplePlugin.class).resolve(SAMPLE_PROPERTIES_FILE_NAME).toFile();
        return CommonUtils.loadProperties(resource);
    }

    @FXML
    public void hello() {
        Dialogs.info()
                .title(I18n.t(ControlsMessages.INFO))
                .content(helloService.hello())
                .owner(rootPane.getScene().getWindow())
                .build()
                .showAndWait();
    }

    @Override
    public Region getRoot() { return rootPane; }

    @Override
    public void reset() {}

    @Override
    public Node getPrimaryFocusNode() { return null; }
}
