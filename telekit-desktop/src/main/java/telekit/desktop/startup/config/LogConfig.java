package telekit.desktop.startup.config;

import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import telekit.base.Env;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static telekit.base.Env.DATA_DIR;
import static telekit.base.Env.LOGS_DIR;

public final class LogConfig implements Config {

    public static final String LOG_CONFIG_FILE_NAME = "logging.properties";
    public static final String LOG_OUTPUT_FILE_NAME = "telekit.log";

    private Path logFilePath;

    public LogConfig() {
        initialize();
    }

    private void initialize() {
        setupLogging();

        if (Env.isDevMode()) { sayHello(); }
    }

    private void setupLogging() {
        try {
            LogManager logManager = LogManager.getLogManager();
            Path configPath = DATA_DIR.resolve(LOG_CONFIG_FILE_NAME);
            if (Files.exists(configPath)) {
                String outputPath = LOGS_DIR.resolve(LOG_OUTPUT_FILE_NAME).toString();

                // JUL can't handle Windows-style paths properly
                if (SystemUtils.IS_OS_WINDOWS) { outputPath = outputPath.replace("\\", "/"); }

                List<String> configData = Files.readAllLines(configPath, StandardCharsets.UTF_8);
                configData.add("java.util.logging.FileHandler.pattern=" + outputPath);
                logManager.readConfiguration(new ByteArrayInputStream(String.join("\n", configData).getBytes()));
                logFilePath = Paths.get(outputPath);
            } else {
                // use console logger
                logManager.readConfiguration(getResourceAsStream(LOG_CONFIG_FILE_NAME));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sayHello() {
        java.util.logging.Logger julLogger = getLogger(getClass());
        julLogger.info("Hey from java.util.logging");

        org.slf4j.Logger slfLogger = org.slf4j.LoggerFactory.getLogger(getClass());
        slfLogger.info("Hey from SLF4J");

        org.apache.commons.logging.Log jclLogger = org.apache.commons.logging.LogFactory.getLog(getClass());
        jclLogger.info("Hey from Commons Logging");
    }

    public Path getLogFilePath() {
        return logFilePath;
    }

    public Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void logEnvironmentInfo() {
        Logger log = getLogger(getClass());
        try {
            log.info("OS=" + System.getProperty("os.name"));
            log.info("OS arch=" + System.getProperty("os.arch"));

            Screen.getScreens().forEach(screen -> log.info(
                    "Screen: bounds=" + screen.getVisualBounds() +
                            "; dpi=" + screen.getDpi() +
                            "; scaleX=" + screen.getOutputScaleX() +
                            "; scaleY=" + screen.getOutputScaleY()
            ));

            log.info("System properties:");
            try {
                logEnv(log, (Map) System.getProperties());
            } catch (Throwable t) {
                log.info("Unable to read system properties");
            }

            log.info("Env variables:");
            try {
                logEnv(log, System.getenv());
            } catch (Throwable t) {
                log.info("Unable to read env variables");
            }

            log.info("JVM options:");
            try {
                ManagementFactory.getRuntimeMXBean().getInputArguments().forEach(log::fine);
            } catch (Throwable t) {
                log.info("Unable to read JVM options");
            }

            log.fine("Supported locales:");
            Locale[] locales = SimpleDateFormat.getAvailableLocales();
            for (Locale locale : locales) {
                log.fine(locale.toString() + " / " + locale.getDisplayName());
            }

            log.fine("Security providers:");
            for (final Provider provider : Security.getProviders()) {
                log.fine(provider.getName());
            }

            log.fine("Supported SSL/TLS ciphers:");
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            String[] ciphers = sslSocketFactory.getDefaultCipherSuites();
            for (String cipherSuite : ciphers) {
                log.fine(cipherSuite);
            }
        } catch (Throwable ignored) { }
    }

    private void logEnv(Logger log, Map<String, String> env) {
        env.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    if (StringUtils.startsWithIgnoreCase(e.getKey(), Env.APP_NAME)) {
                        log.info(e.getKey() + "=" + e.getValue());
                    } else {
                        log.fine(e.getKey() + "=" + e.getValue());
                    }
                });
    }
}
