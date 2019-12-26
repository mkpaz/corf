package corf.base.plugin.internal;

public final class PluginException extends Exception {

    public PluginException() { }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
