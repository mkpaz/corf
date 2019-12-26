package corf.base.event;

import backbonefx.event.Event;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.i18n.M;

import java.util.Objects;

import static corf.base.i18n.I18n.t;

public final class Notification implements Event {

    public enum Type {
        INFO, SUCCESS, WARNING, ERROR
    }

    private final Type type;
    private final String text;
    private @Nullable final Throwable throwable;

    private Notification(Type type, String text, @Nullable Throwable throwable) {
        this.type = Objects.requireNonNull(type, "type");
        this.text = Objects.requireNonNull(text, "text");
        this.throwable = throwable;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getClippedText(int length) {
        return text.length() <= length ? text : text.substring(0, length) + "...";
    }

    public @Nullable Throwable getThrowable() {
        return throwable;
    }

    public static Notification info(String text) {
        return new Notification(Type.INFO, text, null);
    }

    public static Notification success(String text) {
        return new Notification(Type.SUCCESS, text, null);
    }

    public static Notification warning(String text) {
        return new Notification(Type.WARNING, text, null);
    }

    public static Notification error(String text, @Nullable Throwable t) {
        return new Notification(Type.ERROR, text, t);
    }

    public static Notification error(@Nullable Throwable t) {
        if (t == null) {
            return new Notification(Type.ERROR, t(M.MSG_GENERIC_ERROR), null);
        }
        return new Notification(Type.ERROR, StringUtils.defaultIfBlank(t.getMessage(), t(M.MSG_GENERIC_ERROR)), t);
    }
}
