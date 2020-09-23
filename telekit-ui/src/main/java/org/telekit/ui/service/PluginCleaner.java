package org.telekit.ui.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.Environment;
import org.telekit.base.util.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

public class PluginCleaner {

    private static final Logger LOGGER = Logger.getLogger(PluginCleaner.class.getName());

    private static final String STARTUP_RM_FILE = ".removed";
    private static final String SEPARATOR = " ";
    private static final String ACTION_RM = "rm";
    private static final String ACTION_RMDIR = "rmdir";

    private final Path configPath;

    public PluginCleaner() {
        configPath = Environment.PLUGINS_DIR.resolve(STARTUP_RM_FILE);
    }

    public void appendTask(@NotNull Path path) {
        if (!Files.exists(path)) return;

        try (Writer writer = createConfigOutputStream(true)) {
            String pathToDelete = path.toAbsolutePath().toString();

            if (Files.isDirectory(path)) {
                writeTask(writer, List.of(ACTION_RMDIR, pathToDelete));
            }

            if (Files.isRegularFile(path)) {
                writeTask(writer, List.of(ACTION_RM, pathToDelete));
            }
        } catch (Exception e) {
            LOGGER.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    public void executeAll() throws IOException {
        if (!Files.exists(configPath)) return;

        List<String> tasks = Files.readAllLines(configPath, StandardCharsets.UTF_8);
        for (String task : tasks) {
            if (isBlank(task)) continue;
            String[] args = trim(task).split(SEPARATOR);
            execute(args);
        }

        clearAllTasks();
    }

    public void executeAllSilently() {
        try {
            executeAll();
        } catch (Exception e) {
            LOGGER.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    private Writer createConfigOutputStream(boolean append) throws IOException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(configPath.toFile(), append), StandardCharsets.UTF_8
                )
        );
    }

    private void writeTask(Writer writer, List<String> args) throws IOException {
        writer.write(String.join(SEPARATOR, args));
        writer.write("\n");
    }

    private void clearAllTasks() {
        try (Writer writer = createConfigOutputStream(false)) {
            writer.write("");
        } catch (Exception e) {
            LOGGER.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    private void execute(String[] args) throws IOException {
        String action = args[0];
        if (args.length <= 1) return;

        Path pathToDelete = Paths.get(args[1]);
        switch (action) {
            case ACTION_RMDIR:
                LOGGER.info("DELETE: " + pathToDelete);
                if (Files.exists(pathToDelete)) FileUtils.deleteFolder(pathToDelete);
                break;
            case ACTION_RM:
                LOGGER.info("DELETE: " + pathToDelete);
                Files.deleteIfExists(pathToDelete);
                break;
        }
    }
}
