package telekit.base.plugin.internal;

public enum PluginState {

    /**
     * The runtime knows knows about the plugin and its metadata.
     */
    LOADED,

    /**
     * The {@link telekit.base.plugin.Plugin#start} has executed.
     */
    STARTED,

    /**
     * The {@link telekit.base.plugin.Plugin#stop()} has executed.
     */
    STOPPED,

    /**
     * The plugin is disabled and won't be started automatically.
     */
    DISABLED,

    /**
     * Plugin failed to load or to start.
     */
    FAILED,

    /**
     * The plugin is just installed.
     */
    INSTALLED,

    /**
     * The plugin is marked to uninstall.
     */
    UNINSTALLED,

}
