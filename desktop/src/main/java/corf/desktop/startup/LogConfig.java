package corf.desktop.startup;

import corf.base.Env;
import corf.desktop.Launcher;
import javafx.stage.Screen;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.security.Provider;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

public final class LogConfig implements Config {

    public static final String LOG_CONFIG_FILE_NAME = "simplelogger.properties";

    public LogConfig() {
        if (Env.isDevMode()) { ping(); }
    }

    @SuppressWarnings("CatchAndPrintStackTrace")
    public static void setupLogging() {
        try {
            var logConfig = new Properties();
            logConfig.load(Launcher.class.getResourceAsStream(LOG_CONFIG_FILE_NAME));

            for (var entry : logConfig.entrySet()) {
                System.setProperty((String) entry.getKey(), (String) entry.getValue());
            }

            setUserProperty(
                    "org.slf4j.simpleLogger.defaultLogLevel",
                    Env.getPropertyOrEnv("corf.log.level", "CORF_LOG_LEVEL")
            );

            setUserProperty(
                    "org.slf4j.simpleLogger.logFile",
                    Env.getPropertyOrEnv("corf.log.file", "CORF_LOG_FILE")
            );

            // JUL compatibility garbage
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setUserProperty(String property, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            System.setProperty(property, value);
        }
    }

    private void ping() {
        System.Logger systemLogger = System.getLogger(getClass().getName());
        systemLogger.log(INFO, "Ping System.Logger");

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        julLogger.info("Ping java.util.logging");

        org.slf4j.Logger slfLogger = org.slf4j.LoggerFactory.getLogger(getClass());
        slfLogger.info("Ping SLF4J");

        org.apache.commons.logging.Log jclLogger = org.apache.commons.logging.LogFactory.getLog(getClass());
        jclLogger.info("Ping Apache Commons Logging");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void logEnvironmentInfo() {
        System.Logger logger = System.getLogger(getClass().getName());
        try {
            logger.log(INFO, "OS=" + System.getProperty("os.name"));
            logger.log(INFO, "OS arch=" + System.getProperty("os.arch"));

            Screen.getScreens().forEach(screen -> logger.log(
                    INFO,
                    "Screen: bounds=" + screen.getVisualBounds() +
                            "; dpi=" + screen.getDpi() +
                            "; scaleX=" + screen.getOutputScaleX() +
                            "; scaleY=" + screen.getOutputScaleY()
            ));

            logger.log(INFO, "System properties:");
            try {
                logEnv(logger, (Map) System.getProperties(), "system property: ");
            } catch (Throwable t) {
                logger.log(INFO, "Unable to read system properties.");
            }

            logger.log(INFO, "Env variables:");
            try {
                logEnv(logger, System.getenv(), "env: ");
            } catch (Throwable t) {
                logger.log(INFO, "Unable to read env variables.");
            }

            logger.log(DEBUG, "JVM options:");
            try {
                ManagementFactory.getRuntimeMXBean().getInputArguments().forEach(opt -> logger.log(DEBUG, opt));
            } catch (Throwable t) {
                logger.log(DEBUG, "Unable to read JVM options.");
            }

            logger.log(DEBUG, "Supported locales:");
            Locale[] locales = SimpleDateFormat.getAvailableLocales();
            for (Locale locale : locales) {
                logger.log(DEBUG, "locale: " + locale.toString() + " / " + locale.getDisplayName());
            }

            logger.log(DEBUG, "Security providers:");
            for (final Provider provider : Security.getProviders()) {
                logger.log(DEBUG, "security provider: " + provider.getName());
            }

            logger.log(DEBUG, "Supported SSL/TLS ciphers:");
            var sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            String[] ciphers = sslSocketFactory.getDefaultCipherSuites();
            for (String cipherSuite : ciphers) {
                logger.log(DEBUG, "cipher: " + cipherSuite);
            }
        } catch (Throwable ignored) { /* ignore */ }
    }

    private void logEnv(System.Logger logger, Map<String, String> env, String prefix) {
        env.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    if (StringUtils.startsWithIgnoreCase(e.getKey(), Env.APP_NAME)) {
                        logger.log(INFO, prefix + e.getKey() + "=" + e.getValue());
                    } else {
                        logger.log(DEBUG, prefix + e.getKey() + "=" + e.getValue());
                    }
                });
    }
}
