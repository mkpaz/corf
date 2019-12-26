package org.telekit.base;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.telekit.base.fx.Controller;
import org.telekit.base.plugin.Plugin;

import java.net.URL;

public final class UILoader {

    public static Controller load(URL fxmlLocation) {
        return loadImpl(fxmlLocation, ApplicationContext.class);
    }

    public static Controller load(URL fxmlLocation, Class<? extends Plugin> pluginClass) {
        // plugin can be loaded by a separate classloader and FXMLLoader MUST use the same one
        return loadImpl(fxmlLocation, pluginClass);
    }

    private static Controller loadImpl(URL fxmlLocation, Class<?> clazz) {
        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            loader.setClassLoader(clazz.getClassLoader());
            loader.setControllerFactory(ApplicationContext.getInstance()::getBean);

            Parent parent = loader.load();
            Controller controller = loader.getController();
            controller.setParent(parent);

            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
