package org.telekit.desktop.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.service.completion.CompletionRegistry;
import org.telekit.base.service.completion.KeyValueCompletionProvider;
import org.telekit.desktop.event.CompletionRegistryUpdateEvent;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static org.telekit.base.Env.AUTOCOMPLETE_DIR;
import static org.telekit.base.service.completion.CompletionProvider.isValidKey;
import static org.telekit.base.util.CommonUtils.hush;
import static org.telekit.base.util.FileSystemUtils.fileExists;

public class FileCompletionMonitoringService {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static volatile boolean stop = false;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final CompletionRegistry completionRegistry;
    private final WatchService watchService;

    public FileCompletionMonitoringService(CompletionRegistry completionRegistry) {
        try {
            this.completionRegistry = completionRegistry;
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            LOGGER.severe(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public void registerAllProviders() {
        if (!Files.exists(AUTOCOMPLETE_DIR) || !Files.isDirectory(AUTOCOMPLETE_DIR)) {
            LOGGER.info(String.format("Directory '%s' doesn't exist. Service stopped.", AUTOCOMPLETE_DIR));
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(AUTOCOMPLETE_DIR)) {
            stream.forEach(this::registerProvider);
        } catch (IOException e) {
            LOGGER.severe(ExceptionUtils.getStackTrace(e));
        }
    }

    public void start() {
        watchDir(AUTOCOMPLETE_DIR);

        if (keys.isEmpty()) {
            LOGGER.info("No directories to watch found. Service stopped.");
            return;
        }

        Thread task = new Thread(() -> {
            WatchKey key;
            try {
                while (!stop && (key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        handle(key, event);
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                LOGGER.severe(ExceptionUtils.getStackTrace(e));
            }
        });
        task.start();
    }

    private void handle(WatchKey key, WatchEvent<?> event) {
        WatchEvent.Kind<?> eventKind = event.kind();
        if (key == null || event.context() == null || !(event.context() instanceof Path context)) { return; }

        Path dir = keys.get(key);
        if (dir == null) { return; }

        Path path = dir.resolve(context);
        if (ENTRY_CREATE.equals(eventKind)) {
            registerProvider(path);
        } else if (ENTRY_DELETE.equals(eventKind)) {
            unregisterProvider(path);
        }
    }

    public void stop() {
        Thread task = new Thread(() -> {
            stop = true;
            for (WatchKey key : keys.keySet()) {
                hush(key::reset);
                hush(key::cancel);
            }
            keys.clear();
            LOGGER.info("Service stopped.");
        });
        task.start();
    }

    private void watchDir(Path path) {
        if (path == null || !Files.exists(path) || !Files.isDirectory(path)) {
            LOGGER.info("Unable to watch (either doesn't exist or not a directory): " + path);
            return;
        }

        try {
            WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
            keys.put(key, path);
        } catch (IOException e) {
            LOGGER.severe("Unable to watch: " + path);
            LOGGER.severe(ExceptionUtils.getStackTrace(e));
        }
    }

    private void registerProvider(Path path) {
        if (!fileExists(path)) { return; }

        String providerName = FilenameUtils.getBaseName(path.toString());

        // silently ignore invalid names files and existing providers,
        // or it will write to the log after each file edit because of temporary files
        if (!isValidKey(providerName) || completionRegistry.isSupported(providerName)) { return; }

        if ("properties".equalsIgnoreCase(FilenameUtils.getExtension(path.toString()))) {
            completionRegistry.registerProvider(new KeyValueCompletionProvider(providerName, path));
            LOGGER.info(String.format("Registered provider for '%s' via '%s'", providerName, path));
            DefaultEventBus.getInstance().publish(new CompletionRegistryUpdateEvent());
        }
    }

    private void unregisterProvider(Path path) {
        if (path == null) { return; }
        String providerName = FilenameUtils.getBaseName(path.toString());
        if (completionRegistry.isSupported(providerName)) {
            completionRegistry.unregisterProvider(providerName);
            LOGGER.info(String.format("Unregistered provider for '%s' via '%s'", providerName, path));
            DefaultEventBus.getInstance().publish(new CompletionRegistryUpdateEvent());
        }
    }
}
