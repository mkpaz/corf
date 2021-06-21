package org.telekit.controls.demo;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.telekit.base.desktop.Component;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.controls.BaseLauncher;

import java.util.Collection;
import java.util.Collections;

public class DemoLauncher extends BaseLauncher {

    public static void main(String[] args) { launch(args); }

    @Override
    protected Class<? extends Component> getComponent() { return DemoController.class; }

    @Override
    protected Collection<BundleLoader> getBundleLoaders() { return Collections.emptyList(); }

    @Override
    protected void initLauncher(Stage stage, Scene scene) {
        stage.setTitle("Components Overview");
    }
}