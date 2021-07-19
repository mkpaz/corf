package org.telekit.desktop.tools.filebuilder;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.converter.DefaultStringConverter;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.event.DefaultEventBus;
import org.telekit.base.util.DesktopUtils;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.Controls;
import org.telekit.controls.util.TableUtils;
import org.telekit.desktop.event.CompletionRegistryUpdateEvent;
import org.telekit.desktop.tools.Action;
import org.telekit.desktop.tools.common.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.Env.TEMP_DIR;
import static org.telekit.base.i18n.BaseMessages.MSG_GENERIC_IO_ERROR;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.base.util.FileUtils.getParentPath;
import static org.telekit.base.util.FileUtils.sanitizeFileName;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.menuItem;
import static org.telekit.controls.util.TableUtils.setColumnConstraints;
import static org.telekit.desktop.i18n.DesktopMessages.*;
import static org.telekit.desktop.tools.Action.PREVIEW;
import static org.telekit.desktop.tools.Action.*;
import static org.telekit.desktop.tools.common.ComponentUtils.*;
import static org.telekit.desktop.tools.filebuilder.FileBuilderView.createMenuItem;
import static org.telekit.desktop.tools.filebuilder.FileBuilderViewModel.PREVIEW_FILE_NAME;

public final class SettingsPane extends AnchorPane {

    ComboBox<Template> templateChoice;
    TableView<Param> paramTable;
    MenuItem paramCompletionItem;

    TextArea csvText;
    Label csvLineCountLabel;

    TemplateEditor templateEditor = null;
    ParamEditor paramEditor = null;
    ParamCompletionDialog paramCompletionDialog = null;

    private final FileBuilderView view;
    private final FileBuilderViewModel model;
    private Path lastVisitedDirectory;

    public SettingsPane(FileBuilderView view) {
        this.view = view;
        this.model = view.getViewModel();

        createView();

        DefaultEventBus.getInstance().subscribe(CompletionRegistryUpdateEvent.class, e -> {
            paramTable.refresh();
            refreshCompletionMenu(paramTable.getSelectionModel().getSelectedItem());
        });
    }

    private void createView() {
        // COLUMN 0

        templateChoice = new ComboBox<>(model.getTemplates());
        templateChoice.setButtonCell(new TemplateListCell());
        templateChoice.setCellFactory(property -> new TemplateListCell());
        templateChoice.setMaxWidth(Double.MAX_VALUE);
        templateChoice.valueProperty().bindBidirectional(model.selectedTemplateProperty());
        HBox.setHgrow(templateChoice, Priority.ALWAYS);

        HBox templateBox = new HBox();
        templateBox.getChildren().addAll(templateChoice, createTemplateMenu());
        templateBox.setAlignment(Pos.CENTER_LEFT);

        Label paramsLabel = new Label(t(PARAMETERS));
        paramsLabel.setPadding(new Insets(5, 0, 0, 0));

        paramTable = createParamTable();
        model.selectedTemplateProperty().addListener((obs, old, value) -> {
            List<Param> params = new ArrayList<>();
            if (value != null && isNotEmpty(value.getParams())) {
                params.addAll(value.getParams());
                params.sort(Param.COMPARATOR);
            }
            paramTable.getItems().setAll(params);
            paramTable.getSelectionModel().selectFirst();
        });

        // COLUMN 1

        MenuButton clipboardMenu = Controls.create(MenuButton::new, "link-button");
        clipboardMenu.setGraphic(Controls.fontIcon(Material2AL.CONTENT_PASTE));
        clipboardMenu.setCursor(Cursor.HAND);
        clipboardMenu.getItems().addAll(
                menuItem(t(TOOLS_PASTE_COLUMNS_RIGHT), null, e -> pasteAsColumns(csvText)),
                menuItem(t(TOOLS_PASTE_FROM_EXCEL), null, e -> pasteFromExcel(csvText))
        );

        csvLineCountLabel = new Label();

        HBox replacementBox = hbox(5, Pos.CENTER_LEFT, Insets.EMPTY);
        replacementBox.getChildren().addAll(
                new Label(t(TOOLS_LIST_FOR_REPLACEMENT)),
                clipboardMenu,
                horizontalSpacer(),
                csvLineCountLabel,
                new Label(t(TOOLS_LINES))
        );

        csvText = Controls.create(TextArea::new, "monospace");
        csvText.textProperty().bindBidirectional(model.csvTextProperty());
        csvText.focusedProperty().addListener((obs, old, value) -> {
            if (!value) { updateCsvLineCount(); }
        });
        updateCsvLineCount();

        // GRID

        GridPane grid = gridPane(10, 5, new Insets(10));

        grid.add(new Label(t(TOOLS_TEMPLATE)), 0, 0);
        grid.add(templateBox, 0, 1);
        grid.add(paramsLabel, 0, 2);
        grid.add(paramTable, 0, 3);

        grid.add(replacementBox, 1, 0);
        grid.add(csvText, 1, 1, 1, GridPane.REMAINING);

        grid.getColumnConstraints().addAll(
                columnConstraints(400, Priority.NEVER),
                HGROW_ALWAYS
        );

        grid.getRowConstraints().addAll(
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                rowConstraints(Priority.ALWAYS),
                VGROW_NEVER
        );

        getChildren().setAll(grid);
        setAnchors(grid, Insets.EMPTY);
    }

    ///////////////////////////////////////////////////////////////////////////
    // TEMPLATE                                                              //
    ///////////////////////////////////////////////////////////////////////////

    private MenuButton createTemplateMenu() {
        MenuButton templateMenu = Controls.create(MenuButton::new, "link-button");
        templateMenu.setGraphic(Controls.fontIcon(Material2MZ.MORE_VERT));
        templateMenu.setCursor(Cursor.HAND);

        BooleanBinding noTemplates = Bindings.isEmpty(model.getTemplates());

        templateMenu.getItems().addAll(
                //@Formatter:off
                createMenuItem(t(ACTION_PREVIEW),   e -> handleTemplateAction(PREVIEW), noTemplates),
                new SeparatorMenuItem(),
                createMenuItem(t(ACTION_ADD),       e -> handleTemplateAction(ADD), null),
                createMenuItem(t(ACTION_EDIT),      e -> handleTemplateAction(EDIT), noTemplates),
                createMenuItem(t(ACTION_DUPLICATE), e -> handleTemplateAction(DUPLICATE), noTemplates),
                createMenuItem(t(ACTION_DELETE),    e -> handleTemplateAction(REMOVE), noTemplates),
                new SeparatorMenuItem(),
                createMenuItem(t(ACTION_IMPORT),    e -> handleTemplateAction(IMPORT), null),
                createMenuItem(t(ACTION_EXPORT),    e -> handleTemplateAction(EXPORT), noTemplates)
                //@Formatter:on
        );

        return templateMenu;
    }

    private void handleTemplateAction(Action action) {
        switch (action) {
            case ADD, DUPLICATE, EDIT -> {
                TemplateEditor editor = getOrCreateTemplateEditor();
                Template template = action != ADD ? model.selectedTemplateProperty().get() : null;
                editor.setData(action, template, model.getUsedTemplateNames());
                view.showOverlay(editor);
            }
            case REMOVE -> removeTemplate();
            case PREVIEW -> showPreview();
            case IMPORT -> importTemplate();
            case EXPORT -> exportTemplate();
        }
    }

    private TemplateEditor getOrCreateTemplateEditor() {
        if (templateEditor != null) { return templateEditor; }

        templateEditor = new TemplateEditor();
        templateEditor.setOnCommit(this::updateTemplate);
        templateEditor.setOnCloseRequest(view::hideOverlay);

        return templateEditor;
    }

    private void updateTemplate(Action action, Template template) {
        Objects.requireNonNull(template);

        switch (action) {
            case ADD, DUPLICATE -> model.addTemplateCommand().execute(template);
            case EDIT -> model.updateTemplateCommand().execute(template);
            default -> throw new RuntimeException("Invalid action");
        }

        templateEditor.close();
    }

    private void showPreview() {
        Template template = model.selectedTemplateProperty().get();
        if (template == null || !DesktopUtils.isSupported(Desktop.Action.BROWSE)) { return; }

        File outputFile = TEMP_DIR.resolve(PREVIEW_FILE_NAME).toFile();
        String html = PreviewRenderer.render(template);
        try {
            Files.writeString(outputFile.toPath(), html);
            DesktopUtils.browse(outputFile.toURI());
        } catch (IOException e) {
            throw new TelekitException(t(MSG_GENERIC_IO_ERROR), e);
        }
    }

    private void removeTemplate() {
        Template template = model.selectedTemplateProperty().get();
        if (template == null) { return; }

        Dialogs.confirm()
                .title(t(CONFIRMATION))
                .content(t(TOOLS_MSG_DELETE_TEMPLATE, template.getName()))
                .owner(view.getWindow())
                .build()
                .showAndWait()
                .filter(type -> type == ButtonType.OK)
                .ifPresent(type -> model.removeTemplateCommand().execute());
    }

    private void importTemplate() {
        File inputFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_YAML), "*.yaml", "*.yml")
                .initialDirectory(lastVisitedDirectory)
                .build()
                .showOpenDialog(view.getWindow());
        if (inputFile == null) { return; }

        lastVisitedDirectory = getParentPath(inputFile);
        model.importTemplateCommand().execute(inputFile);
    }

    private void exportTemplate() {
        Template template = model.selectedTemplateProperty().get();
        if (template == null) { return; }

        File outputFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_YAML), "*.yaml", "*.yml")
                .initialDirectory(lastVisitedDirectory)
                .initialFileName(sanitizeFileName(template.getName()) + ".yaml")
                .build()
                .showSaveDialog(view.getWindow());
        if (outputFile == null) { return; }

        lastVisitedDirectory = getParentPath(outputFile);
        model.exportTemplateCommand().execute(outputFile);
    }

    ///////////////////////////////////////////////////////////////////////////
    // PARAMS                                                                //
    ///////////////////////////////////////////////////////////////////////////

    private TableView<Param> createParamTable() {
        TableColumn<Param, Image> indicatorColumn = new TableColumn<>();
        setColumnConstraints(indicatorColumn, 30, 30, false, Pos.CENTER);
        indicatorColumn.setCellFactory(cell -> new ParamIndicatorTableCell(model.getCompletionRegistry()));

        TableColumn<Param, String> nameColumn = TableUtils.createColumn(t(NAME), "name");
        setColumnConstraints(nameColumn, 120, USE_COMPUTED_SIZE, false, Pos.CENTER_LEFT);

        TableColumn<Param, String> valueColumn = TableUtils.createColumn(t(VALUE), "value");
        TableUtils.setColumnConstraints(valueColumn, 120, USE_COMPUTED_SIZE, true, Pos.CENTER_LEFT);
        valueColumn.setCellFactory(t -> new ParamValueTableCell(new DefaultStringConverter()));
        valueColumn.setOnEditCommit(e -> e.getTableView()
                .getItems()
                .get(e.getTablePosition().getRow())
                .setValue(e.getNewValue())
        );

        TableView<Param> table = Controls.create(TableView::new, "editable");
        TableViewSelectionModel<Param> selectionModel = table.getSelectionModel();
        table.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        table.getColumns().setAll(List.of(indicatorColumn, nameColumn, valueColumn));
        table.getSortOrder().add(nameColumn);
        table.setEditable(true);

        ContextMenu contextMenu = new ContextMenu();
        table.setContextMenu(contextMenu);
        contextMenu.getItems().addAll(
                createMenuItem(t(ACTION_ADD), e -> showParamDialog(), templateChoice.valueProperty().isNull()),
                createMenuItem(t(ACTION_REMOVE), e -> removeParam(), Bindings.isEmpty(selectionModel.getSelectedItems()))
        );

        paramCompletionItem = createMenuItem(t(TOOLS_CHOOSE_VALUE), e -> showParamCompletions(), null);
        contextMenu.getItems().add(paramCompletionItem);
        paramCompletionItem.setVisible(false);
        selectionModel.selectedItemProperty().addListener((obs, old, value) -> refreshCompletionMenu(value));

        return table;
    }

    private void refreshCompletionMenu(Param param) {
        boolean visible = model.doesParamSupportCompletion(param);
        paramCompletionItem.setVisible(visible);
    }

    private void showParamDialog() {
        ParamEditor editor = getOrCreateParamEditor();
        Set<String> usedParamNames = new HashSet<>();
        if (paramTable.getItems() != null) {
            paramTable.getItems().forEach(param -> usedParamNames.add(param.getName()));
        }
        editor.setData(usedParamNames);
        view.showOverlay(editor);
    }

    private ParamEditor getOrCreateParamEditor() {
        if (paramEditor != null) { return paramEditor; }

        paramEditor = new ParamEditor();
        paramEditor.setOnCommit(this::addParam);
        paramEditor.setOnCloseRequest(view::hideOverlay);

        return paramEditor;
    }

    private void addParam(Param param) {
        model.addParamCommand().execute(param);
        paramEditor.close();
    }

    public void removeParam() {
        Param param = paramTable.getSelectionModel().getSelectedItem();
        if (param == null) { return; }
        model.removeParamCommand().execute(param);
    }

    public void showParamCompletions() {
        Param param = paramTable.getSelectionModel().getSelectedItem();
        if (param == null) { return; }

        ParamCompletionDialog dialog = getOrCreateCompletionDialog();
        dialog.setData(model.getParamCompletion(param));
        view.showOverlay(dialog);
    }

    private ParamCompletionDialog getOrCreateCompletionDialog() {
        if (paramCompletionDialog != null) { return paramCompletionDialog; }

        paramCompletionDialog = new ParamCompletionDialog();

        paramCompletionDialog.setOnCommit(kv -> {
            if (kv != null) {
                paramTable.getSelectionModel().getSelectedItem().setValue(kv.getValue());
                paramTable.refresh();
            }
            paramCompletionDialog.close();
        });
        paramCompletionDialog.setOnCloseRequest(view::hideOverlay);

        return paramCompletionDialog;
    }

    ///////////////////////////////////////////////////////////////////////////
    // CSV                                                                   //
    ///////////////////////////////////////////////////////////////////////////

    public void updateCsvLineCount() {
        // WARNING: avoid binding counting method to the observable property.
        // If text area size is big enough, it will lead to extensive memory
        // usage on multiple subsequent edits.
        int count = countNotBlankLines(trim(csvText.getText()));
        csvLineCountLabel.setText(String.valueOf(count));
    }

    ///////////////////////////////////////////////////////////////////////////

    static class TemplateListCell extends ListCell<Template> {

        @Override
        protected void updateItem(Template template, boolean empty) {
            super.updateItem(template, empty);

            if (template != null) {
                setText(template.getName());
            } else {
                setText(null);
            }
        }
    }
}
