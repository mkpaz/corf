package org.telekit.base.plugin.internal;

import org.telekit.base.event.Event;
import org.telekit.base.plugin.Plugin;

import java.util.Objects;

public class PluginStateChangedEvent extends Event {

    private final Class<? extends Plugin> pluginClass;
    private final PluginState pluginState;

    public PluginStateChangedEvent(Class<? extends Plugin> pluginClass,
                                   PluginState pluginState) {
        this.pluginClass = Objects.requireNonNull(pluginClass);
        this.pluginState = Objects.requireNonNull(pluginState);
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
