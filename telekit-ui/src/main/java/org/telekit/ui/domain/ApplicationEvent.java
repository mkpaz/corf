package org.telekit.ui.domain;

public class ApplicationEvent {

    private final Type type;
    private Object userData;

    public ApplicationEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public enum Type {
        RESTART_REQUIRED,
        PLUGINS_STATE_CHANGED,
        PREFERENCES_CHANGED
    }
}