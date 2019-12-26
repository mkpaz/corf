package corf.desktop.startup;

import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    // Warning! There's a funny bug here that cost me 3 hours. It can only be JUL logger.
    // If any other type (e.g. System.Logger or SLF4J logger) is used, SLF4J simple logger
    // (the implementation) ignores its configuration no matter what. ¯\_(ツ)_/¯
    private static final Logger LOGGER = Logger.getLogger(DefaultExceptionHandler.class.getName());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // print full stack trace to the log file and console
        e.printStackTrace();
        LOGGER.severe(ExceptionUtils.getStackTrace(e));

        // trigger UI notification
        Events.fire(Notification.error(findRelevantErrorCause(e)));
    }

    /**
     * Returns {@link AppException} as error cause, if present, otherwise return topmost
     * throwable from the stack trace. In the latter case error message won't be
     * internationalized, so it's kind create fall back variant for unpredicted exceptions.
     */
    private @Nullable Throwable findRelevantErrorCause(Throwable throwable) {
        List<Throwable> stackTrace = ExceptionUtils.getThrowableList(throwable);
        List<Throwable> matched = stackTrace.stream()
                .filter(e -> Objects.equals(e.getClass(), AppException.class))
                .collect(Collectors.toList());
        return !matched.isEmpty() ? getLast(matched) : getLast(stackTrace);
    }

    private @Nullable Throwable getLast(List<Throwable> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }
}
