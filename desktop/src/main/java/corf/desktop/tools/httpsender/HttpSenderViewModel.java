package corf.desktop.tools.httpsender;

import backbonefx.di.Initializable;
import backbonefx.mvvm.ConsumerCommand;
import backbonefx.mvvm.RunnableCommand;
import backbonefx.mvvm.ViewModel;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.desktop.Async;
import corf.base.desktop.ChangeList;
import corf.base.desktop.Observables;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.base.preferences.CompletionRegistry;
import corf.base.preferences.SharedPreferences;
import corf.base.text.CSV;
import corf.base.text.LineSeparator;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.ReplacementCheckResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static corf.base.i18n.I18n.t;
import static corf.desktop.tools.httpsender.Executor.Options;

public final class HttpSenderViewModel implements Initializable, ViewModel {

    static final String LOG_FILE_NAME = "http-sender.log";

    private final SharedPreferences preferences;
    private final CompletionRegistry completionRegistry;
    private final TemplateRepository templateRepository;
    private final ExecutorService executorService;

    private Executor executor;

    @Inject
    @SuppressWarnings("NullAway.Init")
    public HttpSenderViewModel(TemplateRepository templateRepository,
                               SharedPreferences preferences,
                               CompletionRegistry completionRegistry,
                               ExecutorService executorService) {
        this.templateRepository = templateRepository;
        this.preferences = preferences;
        this.completionRegistry = completionRegistry;
        this.executorService = executorService;
    }

    @Override
    public void init() {
        templateRepository.loadFromDisk();
        templates.getSortedList().setComparator(Template::compareTo);

        reloadTemplates(null);

        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.TEMPLATE_MANAGER_RELOAD, HttpSenderTool.EVENT_SOURCE)) {
                reloadTemplates(selectedTemplate.get());
            }
        });

        log.getItems().addListener(new LogStatListener());

        logErrorsOnly.addListener((obs, old, val) -> {
            if (val != null) {
                log.getFilteredList().setPredicate(request -> !val || !request.succeeded());
            }
        });
    }

    ReplacementCheckResult validate() {
        var template = new Template(Objects.requireNonNull(getSelectedTemplate(), "Selected template must not be null!"));
        template.setParams(getTemplateParams());
        return Executor.validate(template, CSV.from(csvText.get()));
    }

    // ComboBox is bugged! Do not try to update list items partially. You'll run into issues
    // with duplicated items and dropdown resize. The only working solution is to fully
    // reload list content and restore selection after that.
    private void reloadTemplates(@Nullable Template templateToSelect) {
        List<Template> newTemplates = templateRepository.getAll();

        // object properties could change, so we have to find and use the updated bean
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

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
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

    // the value is bound to the task runningProperty()
    private final ReadOnlyBooleanWrapper ongoing = new ReadOnlyBooleanWrapper();
    public ReadOnlyBooleanProperty ongoingProperty() { return ongoing.getReadOnlyProperty(); }

    private final BooleanProperty useBasicAuth = new SimpleBooleanProperty();
    public BooleanProperty useBasicAuthProperty() { return useBasicAuth; }

    private final StringProperty username = new SimpleStringProperty();
    public StringProperty usernameProperty() { return username; }

    private final StringProperty password = new SimpleStringProperty();
    public StringProperty passwordProperty() { return password; }

    private final IntegerProperty pollTimeout = new SimpleIntegerProperty();
    public IntegerProperty pollTimeoutProperty() { return pollTimeout; }

    private final ChangeList<LogRecord> log = new ChangeList<>();
    public ObservableList<LogRecord> getFilteredLog() { return log.getFilteredList(); }
    public ObservableList<LogRecord> getFullLog() { return log.getItems(); }

    private final ReadOnlyObjectWrapper<ProgressCounter> logStat = new ReadOnlyObjectWrapper<>(ProgressCounter.EMPTY);
    public ReadOnlyObjectProperty<ProgressCounter> logStatProperty() { return logStat.getReadOnlyProperty(); }

    private final BooleanProperty logErrorsOnly = new SimpleBooleanProperty();
    public BooleanProperty logErrorsOnlyProperty() { return logErrorsOnly; }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // == openTemplateManagerCommand ==

    public RunnableCommand openTemplateManagerCommand() { return openTemplateManager; }

    private final RunnableCommand openTemplateManager = new RunnableCommand(() -> {
        var template = getSelectedTemplate();
        var event = new ActionEvent<>(EventID.TEMPLATE_MANAGER_SHOW, HttpSenderTool.EVENT_SOURCE, template);
        Events.fire(event);
    });

    // == exportLogCommand ==

    public ConsumerCommand<File> exportLogCommand() { return exportLogCommand; }

    private final ConsumerCommand<File> exportLogCommand = new ConsumerCommand<>(this::exportLog);

    private void exportLog(File outputFile) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        var EOL = LineSeparator.UNIX.getCharacters();

        Runnable runnable = () -> {
            try (var writer = new OutputStreamWriter(new FileOutputStream(outputFile), UTF_8);
                 var out = new BufferedWriter(writer)) {

                for (var rec : log.getItems()) {
                    out.write("[");
                    out.write(rec.getTimestamp().format(formatter));
                    out.write("]\t[");
                    out.write(String.valueOf(rec.getStatusCode()));
                    out.write("]\t[");
                    out.write(String.valueOf(rec.getHttpRequest().method()));
                    out.write("]\t[");
                    out.write(rec.getUri());
                    out.write("]");
                    out.write(EOL);

                    out.write("Request:");
                    out.write(EOL);
                    out.write(("Headers:" + rec.getHttpRequest().headers()).indent(4));
                    out.write(("Body:" + rec.getHttpRequest().body()).indent(4));
                    out.write(EOL);

                    out.write("Response:");
                    out.write(EOL);
                    out.write(("Headers:" + rec.getHttpResponse().headers()).indent(4));
                    out.write(("Body:" + rec.getHttpResponse().body()).indent(4));
                    out.write(EOL);
                }
            } catch (Exception e) {
                throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
            }
        };

        Async.with(runnable)
                .setOnSucceeded(v -> Env.setLastVisitedDir(outputFile))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }

    // == startCommand ==

    public RunnableCommand startCommand() { return startCommand; }

    private final RunnableCommand startCommand = new RunnableCommand(this::start, Bindings.not(ongoing));

    private void start() {
        var template = new Template(Objects.requireNonNull(getSelectedTemplate(), "template"));
        template.setParams(getTemplateParams());

        var csv = CSV.from(csvText.get());
        var options = useBasicAuth.get()
                ? Options.forBasicAuth(pollTimeout.get(), preferences.getProxy(), username.get(), password.get())
                : Options.simple(pollTimeout.get(), preferences.getProxy());

        executor = new Executor(template, csv, log.getItems(), options);

        // prepare properties
        Observables.rebind(ongoing, executor.runningProperty());
        log.getItems().clear(); // this will also set logStat value to LogStat.EMPTY
        logStat.set(new ProgressCounter(executor.getPlannedRequestCount(), 0, 0));

        executor.setOnFailed(event -> {
            var exception = event.getSource().getException();
            if (exception != null) {
                Events.fire(Notification.error(exception));
            }
        });

        // start task
        executorService.execute(executor);
    }

    // == stopCommand ==

    public RunnableCommand stopCommand() { return stopCommand; }

    private final RunnableCommand stopCommand = new RunnableCommand(this::stop, ongoing);

    private void stop() {
        if (executor != null) { executor.cancel(); }
    }

    ////////////////////////////////////////////////////////////

    class LogStatListener implements ListChangeListener<LogRecord> {

        @Override
        public void onChanged(Change<? extends LogRecord> change) {
            ProgressCounter old = logStat.get();

            while (change.next()) {
                if (old == null) {
                    logStat.set(ProgressCounter.EMPTY);
                    return;
                }

                // log must only be either cleared or appended
                if (!change.wasAdded()) { return; }

                change.getAddedSubList().forEach(r -> {
                    if (r.succeeded() || r.forwarded()) {
                        logStat.set(old.incrementSuccessCount());
                    }
                    if (r.failed() || !r.responded()) {
                        logStat.set(old.incrementFailedCount());
                    }
                });
            }
        }
    }
}
