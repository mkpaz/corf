package telekit.desktop.startup;

import org.apache.commons.lang3.exception.ExceptionUtils;
import telekit.base.domain.event.Notification;
import telekit.base.domain.exception.TelekitException;
import telekit.base.event.DefaultEventBus;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static telekit.base.util.CollectionUtils.getLastElement;

public final class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Logger LOG = Logger.getLogger(DefaultExceptionHandler.class.getName());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // show full stack trace in the log file or console
        e.printStackTrace();
        LOG.severe(ExceptionUtils.getStackTrace(e));

        // we should find exact error cause to display relevant error message to user,
        // all low-level (library) exceptions have to be wrapped to instance of TelekitException
        Throwable errorCause = findErrorCause(e, TelekitException.class);
        DefaultEventBus.getInstance().publish(Notification.error(errorCause));
    }

    private Throwable findErrorCause(Throwable throwable, Class<?> clazz) {
        List<Throwable> stackTrace = ExceptionUtils.getThrowableList(throwable);
        List<Throwable> matched = stackTrace.stream()
                .filter(e -> e.getClass() == clazz)
                .collect(Collectors.toList());
        return !matched.isEmpty() ? getLastElement(matched) : getLastElement(stackTrace);
    }
}