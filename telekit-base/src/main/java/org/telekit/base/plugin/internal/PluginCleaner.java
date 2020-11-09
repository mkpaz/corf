package org.telekit.base.plugin.internal;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.Env;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.util.FileUtils.deleteFile;
import static org.telekit.base.util.FileUtils.deleteDir;

public class PluginCleaner {

    private static final Logger LOGGER = Logger.getLogger(PluginCleaner.class.getName());

    private static final String CONFIG_FILE_NAME = ".cleanup";
    private static final String TASK_RM = "rm";
    private static final String TASK_RMDIR = "rmdir";
    private static final String ARG_SEPARATOR = " ";

    private final Path configFilePath = Env.PLUGINS_DIR.resolve(CONFIG_FILE_NAME);

    public PluginCleaner() {}

    public void appendTask(@NotNull Path path) {
        if (!Files.exists(path)) return;

        try (Writer writer = newOutputStream(true)) {
            String pathToDelete = path.toAbsolutePath().toString();

            if (Files.isDirectory(path)) {
                writeTask(writer, List.of(TASK_RMDIR, pathToDelete));
            }

            if (Files.isRegularFile(path)) {
                writeTask(writer, List.of(TASK_RM, pathToDelete));
            }
        } catch (Exception e) {
            LOGGER.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    public void executeAll() throws IOException {
        if (!Files.exists(configFilePath)) return;

        LOGGER.fine("Executing tasks:");
        List<String> tasks = Files.readAllLines(configFilePath, StandardCharsets.UTF_8);
        for (String task : tasks) {
            if (isBlank(task)) continue;
            String[] args = trim(task).split(ARG_SEPARATOR);
            execute(args);
        }

        clearConfig();
    }

    public void executeAllSilently() {
        try {
            executeAll();
        } catch (Exception e) {
            LOGGER.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    private Writer newOutputStream(boolean append) throws IOException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(configFilePath.toFile(), append), StandardCharsets.UTF_8
                )
        );
    }

    private void writeTask(Writer writer, List<String> args) throws IOException {
        String task = String.join(ARG_SEPARATOR, args);
        writer.write(task);
        writer.write("\n");
        LOGGER.fine("Task added [" + task + "]");
    }

    private void clearConfig() {
        try (Writer writer = newOutputStream(false)) {
            LOGGER.fine("Clearing config file");
            writer.write("");
        } catch (Exception e) {
            LOGGER.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    private void execute(String[] args) {
        String action = args[0];
        if (args.length <= 1) return;

        Path pathToDelete = Paths.get(args[1]);
        switch (action) {
            case TASK_RMDIR -> {
                LOGGER.info("Deleting directory: " + pathToDelete);
                deleteDir(pathToDelete);
            }
            case TASK_RM -> {
                LOGGER.info("Deleting file: " + pathToDelete);
                deleteFile(pathToDelete);
            }
        }
    }
}
