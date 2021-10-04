package telekit.desktop.startup.config;

import telekit.base.desktop.routing.Route;
import telekit.base.plugin.Tool;
import telekit.base.util.ClasspathResource;
import telekit.desktop.startup.DefaultExceptionHandler;
import telekit.desktop.tools.apiclient.ApiClientTool;
import telekit.desktop.tools.base64.Base64Tool;
import telekit.desktop.tools.filebuilder.FileBuilderTool;
import telekit.desktop.tools.ipcalc.IPv4CalcTool;
import telekit.desktop.tools.passgen.PasswordGeneratorTool;
import telekit.desktop.tools.seqgen.SequenceGeneratorTool;
import telekit.desktop.views.system.WelcomeView;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Objects;

public interface Config {

    ClasspathResource DESKTOP_MODULE_PATH = ClasspathResource.of("/telekit/desktop", Config.class);

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

    default InputStream getResourceAsStream(String subPath) {
        String path = DESKTOP_MODULE_PATH.concat(subPath).toString();
        return Objects.requireNonNull(Config.class.getResourceAsStream(path));
    }
}
