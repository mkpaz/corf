package org.telekit.desktop.tools.filebuilder;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.Toggle;
import org.telekit.base.Env;
import org.telekit.base.desktop.mvvm.*;
import org.telekit.base.di.Initializable;
import org.telekit.base.domain.KeyValue;
import org.telekit.base.domain.event.TaskProgressEvent;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.service.CompletionProvider;
import org.telekit.base.service.CompletionRegistry;
import org.telekit.base.service.impl.KeyValueCompletionProvider;
import org.telekit.base.util.DesktopUtils;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.Promise;
import org.telekit.controls.util.TransformationListHandle;
import org.telekit.controls.util.UnconditionalObjectProperty;
import org.telekit.desktop.tools.common.Param;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.telekit.base.util.CSVUtils.COMMA_OR_SEMICOLON;
import static org.telekit.base.util.CSVUtils.textToTable;
import static org.telekit.controls.util.BindUtils.isNotBlank;
import static org.telekit.desktop.tools.filebuilder.Generator.MODE_APPEND;
import static org.telekit.desktop.tools.filebuilder.Generator.MODE_REPLACE;

@Singleton
public final class FileBuilderViewModel implements Initializable, ViewModel {

    static final String PREVIEW_FILE_NAME = "import-file-builder.preview.html";
    static final Path DEFAULT_PATH = Env.HOME_DIR.resolve("import.txt");
    static final int SAVE_TYPE_DYNAMIC = 0;
    static final int SAVE_TYPE_PREDEFINED = 1;

    private final CompletionRegistry completionRegistry;
    private final TemplateRepository templateRepository;
    private final ExecutorService threadPool;

    @Inject
    public FileBuilderViewModel(YAMLMapper yamlMapper,
                                CompletionRegistry completionRegistry,
                                ExecutorService threadPool) {
        this.completionRegistry = completionRegistry;
        this.templateRepository = new TemplateRepository(yamlMapper);
        this.threadPool = threadPool;
    }

    @Override
    public void initialize() {
        templates.getSortedList().setComparator(Template::compareTo);

        // Note: double check if you will want to run this from another thread
        // can cause ComboBox updating issues
        templateRepository.reloadAll();
        templates.getItems().setAll(templateRepository.getAll());
        selectFirstTemplate();
    }

    public void selectFirstTemplate() {
        if (!templates.getItems().isEmpty()) {
            selectedTemplateProperty().set(templates.getSortedList().get(0));
        }
    }

    public Set<String> getUsedTemplateNames() {
        return templateRepository.getNames();
    }

    public KeyValueCompletionProvider getCompletionProvider(Param param) {
        CompletionProvider<?> provider = completionRegistry.getProviderFor(param.getName()).orElse(null);
        return provider instanceof KeyValueCompletionProvider value ? value : null;
    }

    public CompletionRegistry getCompletionRegistry() {
        return completionRegistry;
    }

    public List<KeyValue<String, String>> getParamCompletion(Param param) {
        KeyValueCompletionProvider provider = getCompletionProvider(param);
        return provider != null ? new ArrayList<>(provider.find(e -> true)) : Collections.emptyList();
    }

    public boolean doesParamSupportCompletion(Param param) {
        return Param.doesSupportCompletion(param, completionRegistry);
    }

    public List<String> validate() {
        return Generator.validate(selectedTemplate.get(), textToTable(csvText.get(), COMMA_OR_SEMICOLON));
    }

    private void updateTemplate(Consumer<Template> mutator) {
        Template source = selectedTemplate.get();
        Template copy = selectedTemplate.get().deepCopy();
        mutator.accept(copy);
        updateTemplate(source, copy);
    }

    private void updateTemplate(Template source, Template copy) {
        Promise.runAsync(() -> {
            templateRepository.update(copy);
            templateRepository.beginTransaction(source).rollbackOnException(templateRepository::saveAll);
        }).then(() -> reloadTemplates(copy))
                .start(threadPool);
    }

    // Note: ComboBox is bugged. Do not try to update list items partially: add(), set(),
    // remove() etc. You will get issues with duplicated items and dropdown resize. The only
    // working solution is to fully reload list content and restore selection after that.
    private void reloadTemplates(Template templateToSelect) {
        templates.getItems().setAll(templateRepository.getAll());
        if (templateToSelect != null) {
            selectedTemplate.set(templateToSelect);
        } else {
            selectFirstTemplate();
        }
    }

    private void toggleProgressIndicator(boolean on) {
        ongoing.set(on);
        DefaultEventBus.getInstance().publish(new TaskProgressEvent(getClass().getCanonicalName(), on));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
    private final TransformationListHandle<Template> templates = new TransformationListHandle<>();
    public ObservableList<Template> getTemplates() { return templates.getSortedList(); }

    private final ReadOnlyBooleanWrapper ongoing = new ReadOnlyBooleanWrapper(this, "ongoing", false);
    public ReadOnlyBooleanProperty ongoingProperty() { return ongoing.getReadOnlyProperty(); }

    private final ObjectProperty<Template> selectedTemplate = new UnconditionalObjectProperty<>(this, "selectedTemplate");
    public ObjectProperty<Template> selectedTemplateProperty() { return selectedTemplate; }

    private final StringProperty csvText = new SimpleStringProperty(this, "csvText");
    public StringProperty csvTextProperty() { return csvText; }

    private final ObjectProperty<Toggle> saveType = new SimpleObjectProperty<>(this, "saveType");
    public ObjectProperty<Toggle> saveTypeProperty() { return saveType; }

    private final BooleanProperty appendToFile = new SimpleBooleanProperty(this, "appendToFile");
    public BooleanProperty appendToFileProperty() { return appendToFile; }

    private final BooleanProperty openAfterGeneration = new SimpleBooleanProperty(this, "openAfterGeneration", true);
    public BooleanProperty openAfterGenerationProperty() { return openAfterGeneration; }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Event Bus                                                             //
    ///////////////////////////////////////////////////////////////////////////

    public ConsumerCommand<Template> addTemplateCommand() { return addTemplateCommand; }

    private final ConsumerCommand<Template> addTemplateCommand = new ConsumerCommandBase<>() {

        @Override
        protected void doExecute(Template template) {
            Promise.runAsync(() -> {
                // ensure formatting
                templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.add(template);
                    templateRepository.saveAll();
                });
            }).then(() -> reloadTemplates(template))
                    .start(threadPool);
        }
    };

    // ~

    public ConsumerCommand<Template> updateTemplateCommand() { return updateTemplateCommand; }

    private final ConsumerCommand<Template> updateTemplateCommand = new ConsumerCommandBase<>() {

        { executable.bind(selectedTemplate.isNotNull()); }

        @Override
        protected void doExecute(Template template) {
            updateTemplate(selectedTemplate.get(), template);
        }
    };

    // ~

    public Command removeTemplateCommand() { return removeTemplateCommand; }

    private final Command removeTemplateCommand = new CommandBase() {

        { executable.bind(selectedTemplate.isNotNull()); }

        @Override
        protected void doExecute() {
            Template template = selectedTemplate.get();

            Promise.runAsync(() -> {
                // ensure formatting
                templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.removeById(template.getId());
                    templateRepository.saveAll();
                });
            }).then(() -> reloadTemplates(null))
                    .start(threadPool);
        }
    };

    // ~

    public ConsumerCommand<File> importTemplateCommand() { return importTemplateCommand; }

    private final ConsumerCommand<File> importTemplateCommand = new ConsumerCommandBase<>() {

        @Override
        protected void doExecute(File file) {
            Promise.runAsync(() -> {
                // ensure formatting
                templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.importFromFile(file);
                    templateRepository.saveAll();
                });
            }).then(() -> reloadTemplates(null))
                    .start(threadPool);
        }
    };

    // ~

    public ConsumerCommand<File> exportTemplateCommand() { return exportTemplateCommand; }

    private final ConsumerCommand<File> exportTemplateCommand = new ConsumerCommandBase<>() {

        { executable.bind(selectedTemplate.isNotNull()); }

        @Override
        protected void doExecute(File file) {
            Template template = selectedTemplate.get();
            Promise.runAsync(() -> templateRepository.exportToFile(List.of(template), file))
                    .start(threadPool);
        }
    };

    // ~

    public ConsumerCommand<Param> addParamCommand() { return addParamCommand; }

    private final ConsumerCommand<Param> addParamCommand = new ConsumerCommandBase<>() {

        { executable.bind(selectedTemplate.isNotNull()); }

        @Override
        protected void doExecute(Param param) {
            if (param == null) { return; }
            updateTemplate(template -> template.addParam(param));
        }
    };

    // ~

    public ConsumerCommand<Param> removeParamCommand() { return removeParamCommand; }

    private final ConsumerCommand<Param> removeParamCommand = new ConsumerCommandBase<>() {

        { executable.bind(selectedTemplate.isNotNull()); }

        @Override
        protected void doExecute(Param param) {
            if (param == null) { return; }
            updateTemplate(template -> template.removeParam(param));
        }
    };

    // ~

    public ConsumerCommand<File> generateCommand() { return generateCommand; }

    private final ConsumerCommand<File> generateCommand = new ConsumerCommandBase<>() {

        { executable.bind(BindUtils.and(ongoing.not(), selectedTemplate.isNotNull(), isNotBlank(csvText))); }

        @Override
        protected void doExecute(File file) {
            Template template = selectedTemplate.get();
            String[][] csv = textToTable(csvText.get(), COMMA_OR_SEMICOLON);

            // configure task
            Generator generator = new Generator(template, csv, file);

            generator.setCharset(template.getEncoding().getCharset(), template.getEncoding().requiresBOM());
            generator.setLineSeparator(template.getLineSeparator().getCharacters());
            generator.setMode(appendToFile.get() ? MODE_APPEND : MODE_REPLACE);

            // start task
            toggleProgressIndicator(true);
            Promise.runAsync(generator).then(() -> {
                toggleProgressIndicator(false);
                if (openAfterGeneration.get()) {
                    DesktopUtils.openQuietly(file);
                }
            }).start(threadPool);
        }
    };
}
