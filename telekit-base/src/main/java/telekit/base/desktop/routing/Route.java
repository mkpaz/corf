package telekit.base.desktop.routing;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class Route {

    private final String name;
    private final Map<String, Object> args;

    public Route(String name) {
        this(name, Collections.emptyMap());
    }

    public Route(String name, Map<String, Object> args) {
        this.name = Objects.requireNonNull(name);
        this.args = Objects.requireNonNull(args);
    }

    public String getName() { return name; }

    public Map<String, Object> getArgs() { return args; }

    public <T> T getArg(String argName, Class<T> type) { return type.cast(args.get(argName)); }

    public boolean matches(String routeName) { return Objects.equals(routeName, this.name); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Route route = (Route) o;
        return name.equals(route.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Route{" +
                "name='" + name + '\'' +
                ", args=" + args +
                '}';
    }
}
