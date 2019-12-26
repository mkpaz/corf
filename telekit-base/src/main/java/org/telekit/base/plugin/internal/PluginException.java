package org.telekit.base.plugin.internal;

public class PluginException extends Exception {

    public PluginException() {}

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
