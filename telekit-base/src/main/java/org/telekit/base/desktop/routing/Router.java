package org.telekit.base.desktop.routing;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.ViewLoader;
import org.telekit.base.desktop.mvvm.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Router {

    private final Map<String, Class<? extends Component>> routes = new HashMap<>();
    private final ReadOnlyObjectWrapper<Route> currentRoute = new ReadOnlyObjectWrapper<>(this, "currentRoute");
    private final AnchorPane routerPane;
    private Routable previousRoute = null;

    public Router() {
        routerPane = new AnchorPane();
        routerPane.getStyleClass().add("router-pane");
    }

    public <T extends Component> void registerRoute(String routeName, Class<T> view) {
        Objects.requireNonNull(routeName);
        Objects.requireNonNull(view);
        routes.put(routeName, view);
    }

    public void unregisterRoute(String routeName) {
        if (routeName == null) { return; }
        routes.remove(routeName);
    }

    public void navigate(Route route) {
        if (route == null || route.getName() == null || !routes.containsKey(route.getName())) { return; }
        Class<? extends Component> renderer = routes.get(route.getName());
        if (previousRoute != null) { previousRoute.leave(); }

        Component component = ViewLoader.load(renderer);

        getRoutable(component).ifPresentOrElse(
                routable -> {
                    routable.enter(route);
                    previousRoute = routable;
                },
                () -> previousRoute = null
        );

        currentRoute.set(route);

        // refreshing UI, should be the last action
        routerPane.getChildren().setAll(component.getRoot());
        AnchorPane.setTopAnchor(component.getRoot(), 0d);
        AnchorPane.setRightAnchor(component.getRoot(), 0d);
        AnchorPane.setBottomAnchor(component.getRoot(), 0d);
        AnchorPane.setLeftAnchor(component.getRoot(), 0d);
    }

    private Optional<Routable> getRoutable(Component c) {
        if (c instanceof Routable r) {
            return Optional.of(r);
        }

        if (c instanceof View<?> view && view.getViewModel() != null && view.getViewModel() instanceof Routable r) {
            return Optional.of(r);
        }

        return Optional.empty();
    }

    public Pane getRouterPane() {
        return routerPane;
    }

    public Route getCurrentRoute() {
        return currentRoute.get();
    }

    public ReadOnlyObjectProperty<Route> currentRouteProperty() {
        return currentRoute.getReadOnlyProperty();
    }
}
