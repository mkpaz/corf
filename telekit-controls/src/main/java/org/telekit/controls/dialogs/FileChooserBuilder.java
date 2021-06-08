package org.telekit.controls.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileChooserBuilder {

    private final FileChooser fileChooser;

    public FileChooserBuilder() {
        this.fileChooser = new FileChooser();
    }

    public FileChooserBuilder addFilter(ExtensionFilter filter) {
        fileChooser.getExtensionFilters().add(filter);
        return this;
    }

    public FileChooserBuilder addFilter(String description, String... extensions) {
        return addFilter(new ExtensionFilter(description, extensions));
    }

    public FileChooserBuilder initialDirectory(Path path) {
        if (Files.isDirectory(path)) {
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