package org.telekit.base.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.telekit.base.ApplicationContext;
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

    public static Controller load(URL fxmlLocation,
                                  Class<? extends Plugin> pluginClass,
                                  ResourceBundle resourceBundle) {
        return loadImpl(fxmlLocation, pluginClass, resourceBundle);
    }

    public static <T extends Controller> T load(Class<T> cls) {
        T controller = ApplicationContext.getInstance().getBean(cls);
        controller.initialize();
        return controller;
    }

    private static Controller loadImpl(URL fxmlLocation,
                                       Class<?> clazz,
                                       ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            loader.setControllerFactory(ApplicationContext.getInstance()::getBean);

            // plugin can be loaded by a separate classloader and FXMLLoader MUST use the same one
            loader.setClassLoader(clazz.getClassLoader());

            if (resourceBundle != null) {
                loader.setResources(resourceBundle);
            }

            // TODO: implement controllers caching (or replace fxml with Java code)

            // NOTE:
            // The FXMLLoader is currently not designed to perform as a template provider that
            // instantiates the same item over and over again. Rather it is meant to be a one-time-loader
            // for large GUIs (or to serialize them).
            // https://stackoverflow.com/a/11735301/7421700

            Parent parent = loader.load();
            Controller controller = loader.getController();
            if (parent instanceof Pane) {
                controller.setView((Pane) parent);
            }

            return controller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
