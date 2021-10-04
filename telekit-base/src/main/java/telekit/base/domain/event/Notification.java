package telekit.base.domain.event;

import org.jetbrains.annotations.Nullable;
import telekit.base.event.Event;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static telekit.base.i18n.BaseMessages.MSG_GENERIC_ERROR;
import static telekit.base.i18n.I18n.t;

public final class Notification extends Event {

    public enum NotificationType {
        ERROR, INFO, SUCCESS, WARNING
    }

    private final NotificationType type;
    private final String text;
    private final Throwable throwable;

    private Notification(NotificationType type, String text, Throwable throwable) {
        this.type = Objects.requireNonNull(type);
        this.text = Objects.requireNonNull(text);
        this.throwable = throwable;
    }

    public NotificationType getType() { return type; }

    public String getText() { return text; }

    public String getClippedText(int length) {
        return text.length() <= length ? text : text.substring(0, length) + "...";
    }

    public @Nullable Throwable getThrowable() { return throwable; }

    public static Notification error(String text, Throwable t) {
        return new Notification(NotificationType.ERROR, text, t);
    }

    public static Notification error(Throwable t) {
        return error(defaultIfBlank(t.getMessage(), t(MSG_GENERIC_ERROR)), t);
    }

    public static Notification info(String text) {
        return new Notification(NotificationType.INFO, text, null);
    }

    public static Notification success(String text) {
        return new Notification(NotificationType.SUCCESS, text, null);
    }

    public static Notification warning(String text) {
        return new Notification(NotificationType.WARNING, text, null);
    }
}
