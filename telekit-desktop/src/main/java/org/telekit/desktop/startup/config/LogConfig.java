package org.telekit.desktop.startup.config;

import javafx.stage.Screen;
import org.apache.commons.lang3.SystemUtils;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.telekit.base.Env.DATA_DIR;
import static org.telekit.base.Env.LOGS_DIR;

public final class LogConfig implements Config {

    public static final String LOG_CONFIG_FILE_NAME = "logging.properties";
    public static final String LOG_OUTPUT_FILE_NAME = "telekit.log";

    public LogConfig() {
        initialize();
    }

    private void initialize() {
        setupLogging();
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
            } else {
                // use console logger
                logManager.readConfiguration(Config.getResourceAsStream(LOG_CONFIG_FILE_NAME));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    public void logEnvironmentInfo() {
        Logger log = getLogger(getClass());
        try {
            log.info("OS=" + System.getProperty("os.name"));
            log.info("OS arch=" + System.getProperty("os.arch"));

            Screen.getScreens().forEach(screen -> log.info(
                    "Screen: bounds=" + screen.getVisualBounds() + "; dpi=" + screen.getDpi()
            ));

            log.fine("Supported locales:");
            Locale[] locales = SimpleDateFormat.getAvailableLocales();
            for (Locale locale : locales) {
                log.fine(locale.toString() + " / " + locale.getDisplayName());
            }

            log.fine("Supported SSL/TLS ciphers:");
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            String[] ciphers = sslSocketFactory.getDefaultCipherSuites();
            for (String cipherSuite : ciphers) {
                log.fine(cipherSuite);
            }
        } catch (Throwable ignored) {}
    }
}
