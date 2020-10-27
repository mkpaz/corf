package org.telekit.base.plugin.internal;

import org.telekit.base.plugin.Plugin;

public class PluginStateChangedEvent {

    private final Class<? extends Plugin> pluginClass;
    private final PluginState pluginState;

    public PluginStateChangedEvent(Class<? extends Plugin> pluginClass,
                                   PluginState pluginState) {
        this.pluginClass = pluginClass;
        this.pluginState = pluginState;
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
