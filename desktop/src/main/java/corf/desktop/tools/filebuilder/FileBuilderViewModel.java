package corf.desktop.tools.filebuilder;

import backbonefx.di.Initializable;
import backbonefx.mvvm.RunnableCommand;
import backbonefx.mvvm.ViewModel;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.desktop.Async;
import corf.base.desktop.ChangeList;
import corf.base.desktop.OS;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.base.io.FileSystemUtils;
import corf.base.preferences.CompletionRegistry;
import corf.base.text.CSV;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.ReplacementCheckResult;
import corf.desktop.tools.common.SaveMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public final class FileBuilderViewModel implements Initializable, ViewModel {

    static final String DEFAULT_FILE_NAME = "import.txt";
    static final Path DEFAULT_FILE_PATH = Env.HOME_DIR.resolve(DEFAULT_FILE_NAME);

    private final CompletionRegistry completionRegistry;
    private final TemplateRepository templateRepository;
    private final ExecutorService executorService;

    @Inject
    public FileBuilderViewModel(TemplateRepository templateRepository,
                                CompletionRegistry completionRegistry,
                                ExecutorService executorService) {
        this.templateRepository = templateRepository;
        this.completionRegistry = completionRegistry;
        this.executorService = executorService;
    }

    @Override
    public void init() {
        templateRepository.loadFromDisk();
        templates.getSortedList().setComparator(Template::compareTo);

        selectedTemplate.addListener((obs, old, val) -> setNewFileName(val));
        reloadTemplates(null);

        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.TEMPLATE_MANAGER_RELOAD, FileBuilderTool.EVENT_SOURCE)) {
                reloadTemplates(selectedTemplate.get());
            }
        });
    }

    ReplacementCheckResult validate() {
        var template = new Template(Objects.requireNonNull(getSelectedTemplate(), "Selected template must not be null!"));
        template.setParams(getTemplateParams());
        return Generator.validate(template, CSV.from(csvText.get()));
    }

    private @Nullable Path getParentPath(@Nullable Path path) {
        return path != null ? FileSystemUtils.getParentPath(path.toFile()) : Env.HOME_DIR;
    }

    // ComboBox is bugged! Do not try to update list items partially. You'll run into issues
    // with duplicated items and dropdown resize. The only working solution is to fully
    // reload list content and restore selection after that.
    private void reloadTemplates(@Nullable Template templateToSelect) {
        List<Template> newTemplates = templateRepository.getAll();

        // object properties may be changed, so we have to find and use the updated bean
        Template newSelection = null;
        if (templateToSelect != null) {
            newSelection = newTemplates.stream()
                    .filter(t -> Objects.equals(t, templateToSelect))
                    .findFirst()
                    .orElse(null);
        }

        templates.getItems().setAll(newTemplates);
        selectTemplate(newSelection);
    }

    private void selectTemplate(@Nullable Template templateToSelect) {
        Template newSelection = null;

        // select template
        if (!templates.getItems().isEmpty()) {
            newSelection = templateToSelect != null ? templateToSelect : templates.getSortedList().get(0);
        }

        selectedTemplate.set(newSelection);
    }

    private void setNewFileName(@Nullable Template template) {
        if (template != null && StringUtils.isNotBlank(template.getOutputFileName())) {
            // use preferred file name and directory create the previously selected template
            newFileProperty().set(DEFAULT_FILE_PATH.resolveSibling(template.getOutputFileName()));
        } else {
            // fallback to default if no selection or new selection have no preferred file name
            newFileProperty().set(DEFAULT_FILE_PATH);
        }
    }

    private void toggleProgressIndicator(boolean active) {
        ongoing.set(active);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    // @formatter:off
    public CompletionRegistry getCompletionRegistry() { return completionRegistry; }

    private final ChangeList<Template> templates = new ChangeList<>();
    public ObservableList<Template> getTemplates() { return templates.getSortedList(); }

    // template properties MUST NOT be modified by the view, because it changes objects state
    // in the repository and that change is not synchronized with other UI parts using the same object
    private final ObjectProperty<Template> selectedTemplate = new SimpleObjectProperty<>();
    public ObjectProperty<Template> selectedTemplateProperty() { return selectedTemplate; }
    public @Nullable Template getSelectedTemplate() { return selectedTemplate.get(); }

    private final ObjectProperty<Set<Param>> templateParams = new SimpleObjectProperty<>();
    public Set<Param> getTemplateParams() { return templateParams.get(); }
    public void setTemplateParams(Set<Param> params) { templateParams.set(params != null ? params: Collections.emptySet()); }

    private final StringProperty csvText = new SimpleStringProperty();
    public StringProperty csvTextProperty() { return csvText; }

    private final ReadOnlyBooleanWrapper ongoing = new ReadOnlyBooleanWrapper(false);
    public ReadOnlyBooleanProperty ongoingProperty() { return ongoing.getReadOnlyProperty(); }

    private final ObjectProperty<Path> newFile = new SimpleObjectProperty<>(DEFAULT_FILE_PATH);
    public ObjectProperty<Path> newFileProperty() { return newFile; }
    public Path getNewFile() { return Objects.requireNonNullElse(newFile.get(), FileBuilderViewModel.DEFAULT_FILE_PATH); }
    public Path getNewFileDir() { return Objects.requireNonNullElse(getParentPath(getNewFile()), Env.HOME_DIR); }

    private final ObjectProperty<Path> appendFile = new SimpleObjectProperty<>(null);
    public ObjectProperty<Path> appendFileProperty() { return appendFile; }
    public Path getAppendFile() { return Objects.requireNonNullElse(appendFile.get(), FileBuilderViewModel.DEFAULT_FILE_PATH); }
    public Path getAppendFileDir() { return Objects.requireNonNullElse(getParentPath(getAppendFile()), Env.HOME_DIR); }

    private final ToggleGroup saveModeGroup = new ToggleGroup();
    public ToggleGroup getSaveModeGroup() { return saveModeGroup; }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == openTemplateManagerCommand ==

    public RunnableCommand openTemplateManagerCommand() { return openTemplateManager; }

    private final RunnableCommand openTemplateManager = new RunnableCommand(() -> {
        var template = getSelectedTemplate();
        var event = new ActionEvent<>(EventID.TEMPLATE_MANAGER_SHOW, FileBuilderTool.EVENT_SOURCE, template);
        Events.fire(event);
    });

    // == generateCommand ==

    public RunnableCommand generateCommand() { return generateCommand; }

    private final RunnableCommand generateCommand = new RunnableCommand(this::generate, Bindings.not(ongoing));

    private void generate() {
        var template = new Template(Objects.requireNonNull(getSelectedTemplate(), "template"));
        template.setParams(getTemplateParams());

        var csv = CSV.from(csvText.get());
        SaveMode saveMode = (SaveMode) saveModeGroup.getSelectedToggle().getUserData();
        var outputPath = switch (saveMode) {
            case NEW_FILE -> getNewFile();
            case APPEND_FILE -> getAppendFile();
            case CLIPBOARD -> FileSystemUtils.createTempFile();
        };

        var options = new Generator.Options(
                template.getEncoding().getCharset(),
                template.getLineSeparator().getCharacters(),
                template.getEncoding().requiresBOM(),
                saveMode == SaveMode.APPEND_FILE
        );

        var generator = new Generator(template, csv, outputPath.toFile(), options);

        Async.with(generator)
                .setOnScheduled(() -> toggleProgressIndicator(true))
                .setOnSucceeded(nil -> {
                    toggleProgressIndicator(false);

                    if (saveMode != SaveMode.CLIPBOARD) {
                        newFileProperty().set(outputPath.resolveSibling(DEFAULT_FILE_NAME));
                        appendFileProperty().set(outputPath);
                    } else {
                        copyFileContentToClipboard(outputPath);
                    }
                })
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .setOnCancelled(() -> {
                    toggleProgressIndicator(false);

                    if (saveMode != SaveMode.CLIPBOARD) {
                        newFileProperty().set(outputPath.resolveSibling(DEFAULT_FILE_NAME));
                    }
                })
                .start(executorService);
    }

    private void copyFileContentToClipboard(Path path) {
        try {
            OS.setClipboard(Files.readString(path));
        } catch (IOException e) {
            Notification.error(new AppException(DM.MSG_GENERIC_IO_ERROR, e));
        }
    }
}
