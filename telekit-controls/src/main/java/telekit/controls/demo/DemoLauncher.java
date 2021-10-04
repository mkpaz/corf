package telekit.controls.demo;

import javafx.scene.Scene;
import javafx.stage.Stage;
import telekit.base.desktop.Component;
import telekit.base.i18n.BundleLoader;

import java.util.Collection;
import java.util.Collections;

public class DemoLauncher extends BaseLauncher {

    public static void main(String[] args) { launch(args); }

    @Override
    protected Class<? extends Component> getComponent() { return DemoController.class; }

    @Override
    protected Collection<BundleLoader> getBundleLoaders() { return Collections.emptyList(); }

    @Override
    protected void initStage(Stage stage, Scene scene) {
        stage.setTitle("Components Overview");
    }
}