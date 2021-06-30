package org.telekit.desktop.startup.config;

import org.telekit.base.desktop.routing.Route;
import org.telekit.base.plugin.Tool;
import org.telekit.base.util.ClasspathResource;
import org.telekit.desktop.startup.DefaultExceptionHandler;
import org.telekit.desktop.tools.apiclient.ApiClientTool;
import org.telekit.desktop.tools.base64.Base64Tool;
import org.telekit.desktop.tools.filebuilder.FileBuilderTool;
import org.telekit.desktop.tools.ipcalc.IPv4CalcTool;
import org.telekit.desktop.tools.passgen.PasswordGeneratorTool;
import org.telekit.desktop.tools.seqgen.SequenceGeneratorTool;
import org.telekit.desktop.views.system.WelcomeView;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Objects;

public interface Config {

    ClasspathResource DESKTOP_MODULE_PATH = ClasspathResource.of("/org/telekit/desktop", Config.class);

    Route DEFAULT_ROUTE = WelcomeView.ROUTE;

    UncaughtExceptionHandler DEFAULT_EXCEPTION_HANDLER = new DefaultExceptionHandler();

    static List<Tool<?>> getBuiltinTools() {
        return List.of(
                new ApiClientTool(),
                new Base64Tool(),
                new FileBuilderTool(),
                new IPv4CalcTool(),
                new PasswordGeneratorTool(),
                new SequenceGeneratorTool()
        );
    }

    static String getResource(String subPath) {
        String path = DESKTOP_MODULE_PATH.concat(subPath).toString();
        return Objects.requireNonNull(Config.class.getResource(path)).toExternalForm();
    }

    static InputStream getResourceAsStream(String subPath) {
        String path = DESKTOP_MODULE_PATH.concat(subPath).toString();
        return Objects.requireNonNull(Config.class.getResourceAsStream(path));
    }
}
