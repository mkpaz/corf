package org.telekit.ui.domain;

import javafx.beans.property.SimpleBooleanProperty;
import org.telekit.base.Settings;
import org.telekit.base.plugin.Plugin;

import java.nio.file.Files;

import static org.telekit.base.util.CommonUtils.canonicalName;
import static org.telekit.ui.domain.PluginContainer.Status.ENABLED;

public class PluginContainer {

    private final Plugin plugin;
    private Status status;
    private SimpleBooleanProperty selected;

    public PluginContainer(Plugin plugin) {
        this(plugin, ENABLED);
    }

    public PluginContainer(Plugin plugin, Status status) {
        this.plugin = plugin;
        this.status = status;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean hasResources() {
        return Files.exists(Settings.getPluginResourcesDir(plugin.getClass()));
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public String toString() {
        return "PluginContainer{" +
                "plugin=" + canonicalName(plugin) +
                ", status=" + status +
                '}';
    }

    public enum Status {
        ENABLED, DISABLED, INACTIVE, UNINSTALLED
    }
}
