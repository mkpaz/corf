package telekit.base.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import telekit.base.di.Injector;
import telekit.base.i18n.I18n;

import java.net.URL;
import java.util.Objects;

public final class ViewLoader {

    public static <T extends Component> T load(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        return clazz.isAnnotationPresent(FxmlPath.class) ?
                load(getFxmlPath(clazz), clazz) :
                Injector.getInstance().getBean(clazz);
    }

    // TODO: Implement caching for FXML
    public static <T> T load(String fxmlPath, Class<T> clazz) {
        Pair<Parent, T> result = loadFromFXML(fxmlPath, clazz);
        return result.getRight();
    }

    private static String getFxmlPath(Class<?> clazz) {
        FxmlPath fxmlPath = clazz.getAnnotation(FxmlPath.class);
        Objects.requireNonNull(fxmlPath);
        return fxmlPath.value();
    }

    private static <T> Pair<Parent, T> loadFromFXML(String fxmlPath, Class<T> controllerClass) {

        URL url = controllerClass.getResource(fxmlPath);
        if (url == null) {
            // note that corresponding package should be explicitly opened in module-info
            throw new RuntimeException(String.format("Unable to get access to the %s.", fxmlPath));
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(Injector.getInstance()::getBean);
            loader.setClassLoader(controllerClass.getClassLoader());
            loader.setResources(I18n.getInstance());

            Parent parent = loader.load();
            Object controller = loader.getController();

            if (!controllerClass.isInstance(controller)) {
                throw new RuntimeException(String.format("Invalid controller class of path. " +
                                                                 "Check that '%s' controller class is exactly %s.", fxmlPath, controllerClass));
            }

            return ImmutablePair.of(parent, controllerClass.cast(controller));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
