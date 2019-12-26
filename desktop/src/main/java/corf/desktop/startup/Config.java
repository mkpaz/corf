package corf.desktop.startup;

import corf.base.common.ClasspathResource;
import corf.desktop.Launcher;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;

public interface Config {

    ClasspathResource DESKTOP_MODULE = ClasspathResource.of("/corf/desktop", Launcher.class);
    UncaughtExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultExceptionHandler();

    default InputStream getResourceAsStream(String subPath) {
        var path = DESKTOP_MODULE.concat(subPath).toString();
        return Objects.requireNonNull(Config.class.getResourceAsStream(path));
    }
}
