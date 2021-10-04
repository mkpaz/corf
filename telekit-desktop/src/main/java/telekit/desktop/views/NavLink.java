package telekit.desktop.views;

import telekit.base.desktop.routing.Route;

public record NavLink(String title, Route route) {

    public interface Arg {
        String APP_TITLE = "APP_TITLE";
        String PLUGIN_CLASS = "PLUGIN_CLASS";
    }
}
