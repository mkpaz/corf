package corf.desktop.service;

import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.preferences.CompletionRegistry;
import corf.base.preferences.KeyValueCompletionProvider;
import corf.desktop.EventID;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static corf.base.Env.AUTOCOMPLETE_DIR;
import static corf.base.io.FileSystemUtils.fileExists;
import static corf.base.preferences.CompletionProvider.isValidKey;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public class FileCompletionMonitoringService {

    private final static System.Logger LOGGER = System.getLogger(FileCompletionMonitoringService.class.getName());

    private static volatile boolean stop = false;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final CompletionRegistry completionRegistry;
    private final WatchService watchService;

    public FileCompletionMonitoringService(CompletionRegistry completionRegistry) {
        try {
            this.completionRegistry = completionRegistry;
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            LOGGER.log(ERROR, ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    public void registerAllProviders() {
        if (!Files.exists(AUTOCOMPLETE_DIR) || !Files.isDirectory(AUTOCOMPLETE_DIR)) {
            LOGGER.log(INFO, String.format("Directory '%s' doesn't exist. Service stopped.", AUTOCOMPLETE_DIR));
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(AUTOCOMPLETE_DIR)) {
            stream.forEach(this::registerProvider);
        } catch (IOException e) {
            LOGGER.log(ERROR, ExceptionUtils.getStackTrace(e));
        }
    }

    public void start() {
        watchDir(AUTOCOMPLETE_DIR);

        if (keys.isEmpty()) {
            LOGGER.log(INFO, "No directories to watch found. Service stopped.");
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
                LOGGER.log(ERROR, ExceptionUtils.getStackTrace(e));
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
                try {
                    key.reset();
                } catch (Throwable ignored) { /* ignore */ }

                try {
                    key.cancel();
                } catch (Throwable ignored) { /* ignore */ }
            }
            keys.clear();
            LOGGER.log(INFO, "Service stopped.");
        });
        task.start();
    }

    private void watchDir(Path path) {
        if (path == null || !Files.exists(path) || !Files.isDirectory(path)) {
            LOGGER.log(INFO, "Unable to watch (either doesn't exist or not a directory): " + path);
            return;
        }

        try {
            WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
            keys.put(key, path);
        } catch (IOException e) {
            LOGGER.log(ERROR, "Unable to watch: " + path);
            LOGGER.log(ERROR, ExceptionUtils.getStackTrace(e));
        }
    }

    private void registerProvider(Path path) {
        if (!fileExists(path)) { return; }

        String providerName = FilenameUtils.getBaseName(path.toString());

        // silently ignore invalid names files and existing providers,
        // or it will write to the log after each file edit because create temporary files
        if (!isValidKey(providerName) || completionRegistry.containsKey(providerName)) { return; }

        if ("properties".equalsIgnoreCase(FilenameUtils.getExtension(path.toString()))) {
            completionRegistry.registerProvider(new KeyValueCompletionProvider(providerName, path));
            LOGGER.log(INFO, String.format("Registered provider for '%s' via '%s'", providerName, path));
            Events.fire(new ActionEvent<>(EventID.COMPLETION_UPDATE));
        }
    }

    private void unregisterProvider(Path path) {
        if (path == null) { return; }
        String providerName = FilenameUtils.getBaseName(path.toString());
        if (completionRegistry.containsKey(providerName)) {
            completionRegistry.unregisterProvider(providerName);
            LOGGER.log(INFO, String.format("Unregistered provider for '%s' via '%s'", providerName, path));
            Events.fire(new ActionEvent<>(EventID.COMPLETION_UPDATE));
        }
    }
}
