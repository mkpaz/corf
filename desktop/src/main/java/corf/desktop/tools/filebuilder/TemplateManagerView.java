package corf.desktop.tools.filebuilder;

import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import org.jetbrains.annotations.Nullable;
import corf.base.Env;
import corf.base.desktop.Dialogs;
import corf.base.desktop.Focusable;
import corf.base.desktop.controls.Message;
import corf.desktop.i18n.DM;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.ui.TemplateManagerBase;

import java.io.File;
import java.util.regex.Pattern;

import static corf.base.i18n.I18n.t;
import static corf.base.io.FileSystemUtils.sanitizeFileName;

final class TemplateManagerView extends TemplateManagerBase<Template>
        implements View<TemplateManagerView, TemplateManagerViewModel>, Initializable, Focusable {

    static final Pattern PARAM_NAME_PATTERN = Pattern.compile(Param.PLACEHOLDER_CHARACTERS);
    static final Pattern PASSWORD_LENGTH_PATTERN = Pattern.compile("\\d{1,2}");

    private final TemplateManagerViewModel model;
    private final TemplateEditor editor;

    @Inject
    public TemplateManagerView(TemplateManagerViewModel model) {
        this.model = model;
        this.editor = new TemplateEditor();

        templateList.setCellFactory(c -> new TemplateListCell());
        content.getChildren().add(0, editor);
    }

    @Override
    public void init() {
        Bindings.bindContent(editor.reservedTemplateNames(), model.getUsedTemplateNames());

        model.selectedTemplateProperty().addListener((obs, old, val) -> {
            if (!model.isShouldUpdateEditor()) { return; }

            if (val != null) {
                editor.setTemplate(val.copy());
                editor.toFront();
            } else {
                editor.setTemplate(null);
                placeholder.toFront();
            }
        });

        // ugly reference, because JavaFX selection model is package-private
        // order matters, selected template listener must be added first
        model.setSelectionModel(templateList.getSelectionModel());
        templateList.setItems(model.getTemplates());

        filterText.textProperty().bindBidirectional(model.filterProperty());

        duplicateBtn.disableProperty().bind(Bindings.not(model.duplicateTemplateCommand().executableProperty()));
        removeBtn.disableProperty().bind(Bindings.not(model.removeTemplateCommand().executableProperty()));
        exportBtn.disableProperty().bind(Bindings.not(model.exportTemplateCommand().executableProperty()));
        applyBtn.disableProperty().bind(Bindings.not(editor.validProperty()));

        model.errorMessageProperty().addListener((obs, old, val) -> {
            if (val == null) {
                hideMessage();
                return;
            }

            var msg = new Message(Message.Type.DANGER, t(DM.ERROR), val);
            msg.setCloseHandler(m -> model.resetErrorMessage().run());
            showMessage(msg);
        });
    }

    public boolean isDirty() {
        return model.dirtyProperty().get();
    }

    @Override
    public TemplateManagerView getRoot() {
        return this;
    }

    @Override
    public void reset() {
        model.resetCommand().run();
    }

    @Override
    public TemplateManagerViewModel getViewModel() {
        return model;
    }

    @Override
    public @Nullable Node getPrimaryFocusNode() {
        return templateList;
    }

    @Override
    protected void addTemplate() {
        model.addTemplateCommand().run();
    }

    @Override
    protected void updateTemplate() {
        var template = editor.getEditedTemplate();
        if (template != null) {
            model.updateTemplateCommand().execute(template);
        }
    }

    @Override
    protected void removeTemplate() {
        model.removeTemplateCommand().run();
    }

    @Override
    protected void duplicateTemplate() {
        model.duplicateTemplateCommand().run();
    }

    @Override
    protected void importTemplate() {
        File inputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_YAML), "*.yaml", "*.yml")
                .initialDirectory(Env.getLastVisitedDir())
                .build()
                .showOpenDialog(getScene().getWindow());
        if (inputFile == null) { return; }

        model.importTemplateCommand().execute(inputFile);
    }

    @Override
    protected void exportTemplate() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_YAML), "*.yaml", "*.yml")
                .initialDirectory(Env.getLastVisitedDir())
                .initialFileName(sanitizeFileName(model.selectedTemplateProperty().get().getName()) + ".yaml")
                .build()
                .showSaveDialog(getScene().getWindow());
        if (outputFile == null) { return; }

        model.exportTemplateCommand().execute(outputFile);
    }

    @Override
    public void setCloseHandler(EventHandler<ActionEvent> eventHandler) {
        super.setCloseHandler(eventHandler);
    }

    ///////////////////////////////////////////////////////////////////////////

    static final class TemplateListCell extends ListCell<Template> {

        @Override
        protected void updateItem(Template item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null || empty ? null : item.getName());
        }
    }
}
