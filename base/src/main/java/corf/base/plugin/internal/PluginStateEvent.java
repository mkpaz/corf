package corf.base.plugin.internal;

import backbonefx.event.Event;
import corf.base.plugin.Plugin;

import java.util.Objects;

public final class PluginStateEvent implements Event {

    private final Class<? extends Plugin> pluginClass;
    private final PluginState pluginState;

    public PluginStateEvent(Class<? extends Plugin> pluginClass,
                            PluginState pluginState) {
        this.pluginClass = Objects.requireNonNull(pluginClass, "pluginClass");
        this.pluginState = Objects.requireNonNull(pluginState, "pluginState");
    }

    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }

    public PluginState getPluginState() {
        return pluginState;
    }

    @Override
    public String toString() {
        return "PluginStateChangedEvent{" +
                "pluginClass=" + pluginClass +
                ", pluginState=" + pluginState +
                '}';
    }
}
