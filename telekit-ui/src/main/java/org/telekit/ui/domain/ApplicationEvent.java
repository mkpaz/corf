package org.telekit.ui.domain;

public class ApplicationEvent {

    private final Type type;
    private Object userDta;

    public ApplicationEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Object getUserDta() {
        return userDta;
    }

    public void setUserDta(Object userDta) {
        this.userDta = userDta;
    }

    public enum Type {
        RESTART_REQUIRED,
        PLUGINS_STATE_CHANGED,
        PREFERENCES_CHANGED
    }
}