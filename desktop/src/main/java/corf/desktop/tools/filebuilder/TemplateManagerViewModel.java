package corf.desktop.tools.filebuilder;

import backbonefx.di.Initializable;
import backbonefx.mvvm.ConsumerCommand;
import backbonefx.mvvm.RunnableCommand;
import backbonefx.mvvm.ViewModel;
import jakarta.inject.Inject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.control.SelectionModel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.desktop.Async;
import corf.base.desktop.ChangeList;
import corf.base.text.SequenceMatcher;
import corf.desktop.i18n.DM;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

import static corf.base.i18n.I18n.t;

@SuppressWarnings("UnnecessaryLambda")
final class TemplateManagerViewModel implements Initializable, ViewModel {

    private static final String TEMPLATE_NAME_PREFIX = "New Template";
    private static final Predicate<Template> NO_FILTER = t -> true;

    private final TemplateRepository templateRepository;
    private final ExecutorService executorService;

    @Inject
    public TemplateManagerViewModel(TemplateRepository templateRepository,
                                    ExecutorService executorService) {
        this.templateRepository = templateRepository;
        this.executorService = executorService;
    }

    @Override
    public void init() {
        filter.addListener((obs, old, val) -> templates.getFilteredList().setPredicate(
                val != null ? template -> isTemplateMatchesFilter(val, template) : NO_FILTER
        ));

        templates.getSortedList().setComparator(Template::compareTo);
        reloadTemplates(null, true);
    }

    private void reloadTemplates(@Nullable Template templateToSelect, boolean updateEditor) {
        // protect editor from updating when reloading items
        setShouldUpdateEditor(updateEditor);

        templates.getItems().setAll(templateRepository.getAll());
        usedTemplateNames.clear();
        usedTemplateNames.addAll(templateRepository.getNames());
        selectTemplate(templateToSelect);

        setShouldUpdateEditor(true);
    }

    private void selectTemplate(@Nullable Template template) {
        if (getSelectionModel() == null) { return; }

        if (!templates.getItems().isEmpty()) {
            getSelectionModel().select(template != null ? template : templates.getSortedList().get(0));
        }
    }

    private String generateTemplateName(String prefix) {
        var m = SequenceMatcher.<Template>create(StringUtils.trim(prefix), " ")
                .setExtractor(Template::getName);
        return m.get(templates.getItems(), true);
    }

    private void triggerErrorMessage(@Nullable Throwable t) {
        String cause = (t != null && t.getMessage() != null && !t.getMessage().isEmpty())
                ? t.getMessage()
                : t(DM.MSG_GENERIC_ERROR);
        errorMessage.set(cause);
        reloadTemplates(selectedTemplate.get(), true);
    }

    private void requestFilterReset(@Nullable Template changedTemplate) {
        if (changedTemplate == null || !isTemplateMatchesFilter(filter.get(), changedTemplate)) {
            filter.set(null);
        }
    }

    private boolean isTemplateMatchesFilter(@Nullable String filter, @Nullable Template template) {
        if (filter == null || template == null) { return true; }
        var s = filter.trim().toLowerCase();
        return s.isEmpty() || template.getName().toLowerCase().contains(s);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
    private final ChangeList<Template> templates = new ChangeList<>();
    public ObservableList<Template> getTemplates() { return templates.getSortedList(); }

    private final ObjectProperty<SelectionModel<Template>> selectionModel = new SimpleObjectProperty<>();
    public void setSelectionModel(SelectionModel<Template> model) {
        selectionModel.set(model);

        if (selectedTemplate.isBound()) { selectedTemplate.unbind(); }
        selectedTemplate.bind(model.selectedItemProperty());

        if (!templates.getItems().isEmpty()) {
            getSelectionModel().select(templates.getSortedList().get(0));
        }
    }
    public SelectionModel<Template> getSelectionModel() { return selectionModel.get(); }

    private boolean shouldUpdateEditor = true;
    public boolean isShouldUpdateEditor() { return shouldUpdateEditor; }
    private void setShouldUpdateEditor(boolean shouldUpdateEditor) { this.shouldUpdateEditor = shouldUpdateEditor; }

    private final ReadOnlyObjectWrapper<Template> selectedTemplate = new ReadOnlyObjectWrapper<>();
    public ReadOnlyObjectProperty<Template> selectedTemplateProperty() { return selectedTemplate.getReadOnlyProperty(); }
    private final BooleanBinding hasSelectedTemplate = selectedTemplateProperty().isNotNull();

    private final ObservableSet<String> usedTemplateNames = FXCollections.observableSet(new HashSet<>());
    public ObservableSet<String> getUsedTemplateNames() { return usedTemplateNames; }

    private final StringProperty filter = new SimpleStringProperty();
    public StringProperty filterProperty() { return filter; }

    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper(false);
    public ReadOnlyBooleanProperty dirtyProperty() { return dirty.getReadOnlyProperty(); }

    private final ReadOnlyStringWrapper errorMessage = new ReadOnlyStringWrapper();
    public ReadOnlyStringProperty errorMessageProperty() { return errorMessage.getReadOnlyProperty(); }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == addTemplateCommand ==

    public RunnableCommand addTemplateCommand() { return addTemplateCommand; }

    private final RunnableCommand addTemplateCommand = new RunnableCommand(this::addTemplate);

    private void addTemplate() {
        // set all mandatory template properties
        final Template template = Template.create(generateTemplateName(TEMPLATE_NAME_PREFIX), "Hello ${name}!");

        Async.with(() -> templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.add(template);
                    templateRepository.saveToDisk();
                }))
                .setOnSucceeded(t -> {
                    requestFilterReset(template);
                    reloadTemplates(template, true);
                    dirty.set(true);
                })
                .setOnFailed(this::triggerErrorMessage)
                .start(executorService);
    }

    // == duplicateTemplateCommand ==

    public RunnableCommand duplicateTemplateCommand() { return duplicateTemplateCommand; }

    private final RunnableCommand duplicateTemplateCommand = new RunnableCommand(
            this::duplicateTemplate, hasSelectedTemplate
    );

    private void duplicateTemplate() {
        final Template template = selectedTemplate.get().duplicate();

        // generate template name that follows the pattern `original_name (copy) N`, N >= 2
        int copyPrefixStart = template.getName().lastIndexOf(" (copy)");
        // if original name already contains `(copy)`, use it as prefix
        // to avoid this - `original_name (copy) (copy)` or this - `original_name (copy) 2 (copy)
        String namePrefix = copyPrefixStart >= 0 ?
                template.getName().substring(0, copyPrefixStart) + " (copy)" :
                template.getName() + " (copy)";
        template.setName(generateTemplateName(namePrefix));

        Async.with(() -> templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.add(template);
                    templateRepository.saveToDisk();
                }))
                .setOnSucceeded(t -> {
                    requestFilterReset(template);
                    reloadTemplates(template, true);
                    dirty.set(true);
                })
                .setOnFailed(this::triggerErrorMessage)
                .start(executorService);
    }

    // == updateTemplateCommand ==

    public ConsumerCommand<Template> updateTemplateCommand() { return updateTemplateCommand; }

    private final ConsumerCommand<Template> updateTemplateCommand = new ConsumerCommand<>(
            this::updateTemplate, hasSelectedTemplate
    );

    private void updateTemplate(Template updTemplate) {
        if (selectedTemplate.get().deepEquals(updTemplate)) { return; }

        Async.with(() -> {
                    templateRepository.update(updTemplate);
                    templateRepository.beginTransaction(updTemplate).rollbackOnException(templateRepository::saveToDisk);
                })
                .setOnSucceeded(nil -> {
                    requestFilterReset(updTemplate);
                    reloadTemplates(updTemplate, false);
                    dirty.set(true);
                })
                .setOnFailed(this::triggerErrorMessage)
                .start(executorService);
    }

    // == removeTemplateCommand ==

    public RunnableCommand removeTemplateCommand() { return removeTemplateCommand; }

    private final RunnableCommand removeTemplateCommand = new RunnableCommand(
            this::removeTemplate, hasSelectedTemplate
    );

    private void removeTemplate() {
        final Template template = selectedTemplate.get();
        Async.with(() -> templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.removeById(template.getId());
                    templateRepository.saveToDisk();
                }))
                .setOnSucceeded(nil -> {
                    reloadTemplates(null, true);
                    dirty.set(true);
                })
                .setOnFailed(this::triggerErrorMessage)
                .start(executorService);
    }

    // == importTemplateCommand ==

    public ConsumerCommand<File> importTemplateCommand() { return importTemplateCommand; }

    private final ConsumerCommand<File> importTemplateCommand = new ConsumerCommand<>(this::importTemplate);

    private void importTemplate(File inputFile) {
        Env.setLastVisitedDir(inputFile);
        Async.with(() -> templateRepository.beginTransaction(false).rollbackOnException(() -> {
                    templateRepository.importFromFile(inputFile);
                    templateRepository.saveToDisk();
                }))
                .setOnSucceeded(nil -> {
                    requestFilterReset(null);
                    reloadTemplates(null, true);
                    dirty.set(true);
                })
                .setOnFailed(this::triggerErrorMessage)
                .start(executorService);
    }

    // == exportTemplateCommand ==

    public ConsumerCommand<File> exportTemplateCommand() { return exportTemplateCommand; }

    private final ConsumerCommand<File> exportTemplateCommand = new ConsumerCommand<>(
            this::exportTemplate, hasSelectedTemplate
    );

    private void exportTemplate(File outputFile) {
        Env.setLastVisitedDir(outputFile);
        final Template template = selectedTemplateProperty().get();

        Async.with(() -> templateRepository.exportToFile(List.of(template), outputFile))
                .setOnFailed(this::triggerErrorMessage)
                .start(executorService);
    }

    // == resetCommand ==

    public RunnableCommand resetCommand() { return resetCommand; }

    private final RunnableCommand resetCommand = new RunnableCommand(() -> {
        requestFilterReset(null);
        selectTemplate(null);
    });

    public RunnableCommand resetErrorMessage() { return resetErrorMessage; }

    private final RunnableCommand resetErrorMessage = new RunnableCommand(() -> errorMessage.set(null));
}
