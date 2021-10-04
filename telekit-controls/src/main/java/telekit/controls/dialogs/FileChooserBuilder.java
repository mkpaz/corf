package telekit.controls.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import telekit.base.Env;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileChooserBuilder {

    private final FileChooser fileChooser;

    public FileChooserBuilder() {
        fileChooser = new FileChooser();
        initialDirectory(Env.HOME_DIR);
    }

    public FileChooserBuilder addFilter(ExtensionFilter filter) {
        fileChooser.getExtensionFilters().add(filter);
        return this;
    }

    public FileChooserBuilder addFilter(String description, String... extensions) {
        return addFilter(new ExtensionFilter(description, extensions));
    }

    public FileChooserBuilder initialDirectory(Path path) {
        if (path != null && Files.exists(path) && Files.isDirectory(path)) {
            fileChooser.setInitialDirectory(path.toFile());
        }
        return this;
    }

    public FileChooserBuilder initialFileName(String filename) {
        fileChooser.setInitialFileName(filename);
        return this;
    }

    public FileChooser build() {
        return fileChooser;
    }
}