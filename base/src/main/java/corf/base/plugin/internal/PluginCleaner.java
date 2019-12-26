package corf.base.plugin.internal;

import corf.base.Env;
import corf.base.io.FileSystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.lang.System.Logger.Level.*;

/**
 * Plugin JARs can't be removed without restarting JVM. This is a small cleaner
 * utility that accepts paths to the resources that need to be removed and stores
 * them as internal tasks. Added tasks can be then executed at any moment when those
 * resources supposed to be free. Typically, cleaner tasks should be executed on
 * every app startup.
 */
public final class PluginCleaner {

    private static final System.Logger LOGGER = System.getLogger(PluginCleaner.class.getName());

    private static final String CONFIG_FILE_NAME = ".cleanup";
    private static final String TASK_RM = "rm";
    private static final String TASK_RMDIR = "rmdir";
    private static final String ARG_SEPARATOR = " ";

    private final Path configFilePath = Env.PLUGINS_DIR.resolve(CONFIG_FILE_NAME);

    public PluginCleaner() { }

    /**
     * Adds cleanup task for the cleaner. Specify path to the file or directory that
     * should be removed on next cleaner execution.
     */
    public void appendTask(Path path) {
        Objects.requireNonNull(path, "path");
        if (!Files.exists(path)) { return; }

        try (Writer writer = newOutputStream(true)) {
            String pathToDelete = path.toAbsolutePath().toString();

            if (Files.isDirectory(path)) {
                writeTask(writer, List.of(TASK_RMDIR, pathToDelete));
            }

            if (Files.isRegularFile(path)) {
                writeTask(writer, List.of(TASK_RM, pathToDelete));
            }
        } catch (Exception e) {
            LOGGER.log(WARNING, ExceptionUtils.getStackTrace(e));
        }
    }

    /** Executes all cleaner tasks sequentially. */
    public void executeAll() throws IOException {
        if (!Files.exists(configFilePath)) { return; }

        LOGGER.log(DEBUG, "Executing tasks:");
        List<String> tasks = Files.readAllLines(configFilePath, StandardCharsets.UTF_8);
        for (String task : tasks) {
            if (StringUtils.isBlank(task)) { continue; }
            String[] args = StringUtils.trim(task).split(ARG_SEPARATOR);
            execute(args);
        }

        clearConfig();
    }

    /** See {@link #executeAll()}. */
    public void executeAllSilently() {
        try {
            executeAll();
        } catch (Exception e) {
            LOGGER.log(WARNING, ExceptionUtils.getStackTrace(e));
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
        var task = String.join(ARG_SEPARATOR, args);
        writer.write(task);
        writer.write("\n");
        LOGGER.log(DEBUG, "Task added [" + task + "]");
    }

    private void clearConfig() {
        try (Writer writer = newOutputStream(false)) {
            LOGGER.log(DEBUG, "Clearing config file");
            writer.write("");
        } catch (Exception e) {
            LOGGER.log(WARNING, ExceptionUtils.getStackTrace(e));
        }
    }

    private void execute(String[] args) {
        String action = args[0];
        if (args.length == 1) { return; }

        var pathToDelete = Paths.get(args[1]);
        switch (action) {
            case TASK_RMDIR -> {
                LOGGER.log(INFO, "Deleting directory: " + pathToDelete);
                FileSystemUtils.deleteDir(pathToDelete);
            }
            case TASK_RM -> {
                LOGGER.log(INFO, "Deleting file: " + pathToDelete);
                FileSystemUtils.deleteFile(pathToDelete);
            }
        }
    }
}
