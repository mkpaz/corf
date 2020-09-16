package org.telekit.base;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.telekit.base.fx.Controller;
import org.telekit.base.plugin.Plugin;

import java.net.URL;
import java.util.ResourceBundle;

public final class UILoader {

    public static Controller load(URL fxmlLocation) {
        return loadImpl(fxmlLocation, ApplicationContext.class, null);
    }

    public static Controller load(URL fxmlLocation, ResourceBundle resourceBundle) {
        return loadImpl(fxmlLocation, ApplicationContext.class, resourceBundle);
    }

    public static Controller load(URL fxmlLocation, Class<? extends Plugin> pluginClass) {
        return loadImpl(fxmlLocation, pluginClass, null);
    }

    public static Controller load(URL fxmlLocation, Class<? extends Plugin> pluginClass, ResourceBundle resourceBundles) {
        return loadImpl(fxmlLocation, pluginClass, resourceBundles);
    }

    private static Controller loadImpl(URL fxmlLocation, Class<?> clazz, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            loader.setControllerFactory(ApplicationContext.getInstance()::getBean);

            // plugin can be loaded by a separate classloader and FXMLLoader MUST use the same one
            loader.setClassLoader(clazz.getClassLoader());

            if (resourceBundle != null) {
                loader.setResources(resourceBundle);
            }

            Parent parent = loader.load();
            Controller controller = loader.getController();
            controller.setParent(parent);

            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
