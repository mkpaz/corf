package corf.base.desktop;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FileChooserBuilder {

    private final FileChooser fileChooser;

    public FileChooserBuilder() {
        fileChooser = new FileChooser();
        initialDirectory(Env.HOME_DIR);
    }

    public FileChooserBuilder addFilter(ExtensionFilter filter) {
        Objects.requireNonNull(filter, "filter");
        fileChooser.getExtensionFilters().add(filter);
        return this;
    }

    public FileChooserBuilder addFilter(String description, String... extensions) {
        return addFilter(new ExtensionFilter(description, extensions));
    }

    public FileChooserBuilder initialDirectory(@Nullable Path path) {
        if (path != null && Files.exists(path) && Files.isDirectory(path)) {
            fileChooser.setInitialDirectory(path.toFile());
        }
        return this;
    }

    public FileChooserBuilder initialFileName(@Nullable String filename) {
        if (StringUtils.isNoneBlank(filename)) {
            fileChooser.setInitialFileName(filename);
        }
        return this;
    }

    public FileChooser build() {
        return fileChooser;
    }
}