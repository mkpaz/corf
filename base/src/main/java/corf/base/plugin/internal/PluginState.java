package corf.base.plugin.internal;

public enum PluginState {

    /** The runtime knows about the plugin and its metadata. */
    LOADED,

    /** The {@link corf.base.plugin.Plugin#start} has executed. */
    STARTED,

    /** The {@link corf.base.plugin.Plugin#stop()} has executed. */
    STOPPED,

    /** The plugin is disabled and won't be started automatically. */
    DISABLED,

    /** Plugin is failed to load or to start. */
    FAILED,

    /** The plugin is just installed and don't yet loaded because we have to restart the app. */
    INSTALLED,

    /** The plugin is marked to uninstall. */
    UNINSTALLED
}
