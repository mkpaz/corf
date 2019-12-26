package corf.desktop.tools.filebuilder;

import atlantafx.base.theme.Styles;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.desktop.Observables;
import corf.base.desktop.controls.HorizontalForm;
import corf.base.desktop.controls.TextFieldTableCell;
import corf.base.desktop.controls.TextFieldTableCell.CellTextFormatter;
import corf.base.text.Encoding;
import corf.base.text.LineSeparator;
import corf.base.text.SequenceMatcher;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.TemplateWorker;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static atlantafx.base.theme.Styles.BUTTON_ICON;
import static atlantafx.base.theme.Styles.SMALL;
import static corf.base.i18n.I18n.t;

final class TemplateEditor extends TabPane {

    private static final SequenceMatcher<Param> PARAM_NAME_GEN = SequenceMatcher.<Param>create("param")
            .setExtractor(Param::getName);

    // pattern tab
    TextField nameText;
    TextArea headerText;
    TextArea patternText;
    TextField delimiterText;
    TextArea footerText;
    TextField filenameText;
    ComboBox<Encoding> encodingChoice;
    ComboBox<LineSeparator> lineSeparatorChoice;

    // params tab
    TableView<Param> paramTable;
    Button addParamBtn;
    Button removeParamBtn;

    // description tab
    TextArea descriptionText;

    private final ObjectProperty<Template> backingTemplate = new SimpleObjectProperty<>();
    private final ObservableSet<String> reservedTemplateNames = FXCollections.observableSet(new HashSet<>());
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper();

    @SuppressWarnings("NullAway.Init")
    public TemplateEditor() {
        super();

        createView();
        init();
    }

    public ObservableSet<String> reservedTemplateNames() {
        return reservedTemplateNames;
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    public @Nullable Template getEditedTemplate() {
        var template = backingTemplate.get();
        if (template == null) { return null; }

        template.setName(nameText.getText());
        template.setHeader(headerText.getText());
        template.setFooter(footerText.getText());
        template.setPattern(patternText.getText());
        template.setDelimiter(delimiterText.getText());
        template.setDescription(descriptionText.getText());
        template.setOutputFileName(filenameText.getText());
        template.setEncoding(encodingChoice.getValue());
        template.setLineSeparator(lineSeparatorChoice.getValue());
        template.setParams(new TreeSet<>(paramTable.getItems()));

        return template;
    }

    public void setTemplate(@Nullable Template template) {
        if (template != null) {
            nameText.setText(template.getName());
            headerText.setText(template.getHeader());
            footerText.setText(template.getFooter());
            patternText.setText(template.getPattern());
            delimiterText.setText(template.getDelimiter());
            descriptionText.setText(template.getDescription());
            filenameText.setText(template.getOutputFileName());
            encodingChoice.getSelectionModel().select(template.getEncoding());
            lineSeparatorChoice.getSelectionModel().select(template.getLineSeparator());

            var params = new ArrayList<Param>();
            if (CollectionUtils.isNotEmpty(template.getParams())) {
                params.addAll(template.getParams());
                params.sort(Param.COMPARATOR);
            }

            paramTable.getItems().setAll(params);
        } else {
            nameText.setText(null);
            headerText.setText(null);
            footerText.setText(null);
            patternText.setText(null);
            delimiterText.setText(null);
            descriptionText.setText(null);
            filenameText.setText(null);
            encodingChoice.getSelectionModel().clearSelection();
            lineSeparatorChoice.getSelectionModel().clearSelection();

            paramTable.getItems().clear();
        }

        backingTemplate.set(template);
        resetTabSelection();
        paramTable.getSelectionModel().selectFirst();
        paramTable.refresh();
    }

    public void resetTabSelection() {
        getSelectionModel().selectFirst();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private API                                                           //
    ///////////////////////////////////////////////////////////////////////////

    private void createView() {
        getTabs().setAll(
                createPatternTab(),
                createParamsTab(),
                createDescriptionTab()
        );
        getStyleClass().add(ExtraStyles.BG_DEFAULT);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    private void init() {
        tabMinWidthProperty().bind(widthProperty().divide(getTabs().size()).subtract(20));

        addParamBtn.setOnAction(e -> addParam(new Param(generateParamName(), Param.Type.CONSTANT, null, null)));

        removeParamBtn.setOnAction(
                e -> removeParams(paramTable.getSelectionModel().getSelectedItems())
        );
        removeParamBtn.disableProperty().bind(
                Bindings.isEmpty(paramTable.getSelectionModel().getSelectedItems())
        );

        var nameValidator = Bindings.createBooleanBinding(() -> {
            var name = StringUtils.trim(nameText.getText());
            if (StringUtils.isEmpty(name)) { return false; }

            return !reservedTemplateNames.contains(name) || Objects.equals(name, getBackingTemplateName());
        }, backingTemplate, nameText.textProperty());
        nameValidator.addListener((obs, old, val) -> nameText.pseudoClassStateChanged(Styles.STATE_DANGER, !val));

        var patternValidator = Bindings.createBooleanBinding(
                () -> StringUtils.isNotBlank(patternText.getText()), backingTemplate, patternText.textProperty()
        );
        patternValidator.addListener((obs, old, val) -> patternText.pseudoClassStateChanged(Styles.STATE_DANGER, !val));

        valid.bind(Observables.and(nameValidator, patternValidator));
    }

    private Tab createPatternTab() {
        // == FIELDS ==

        nameText = new TextField();
        nameText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(nameText, true);

        headerText = new TextArea();
        headerText.setPromptText(t(DM.FILE_BUILDER_BEFORE_THE_FIRST_ELEMENT));
        headerText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(headerText, true);
        headerText.getStyleClass().add(ExtraStyles.MONOSPACE);
        headerText.setPrefHeight(60);

        patternText = new TextArea();
        patternText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(patternText, true);
        patternText.getStyleClass().add(ExtraStyles.MONOSPACE);
        patternText.setPrefHeight(120);

        delimiterText = new TextField();
        delimiterText.setPromptText(t(DM.FILE_BUILDER_BETWEEN_ELEMENTS));
        delimiterText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(delimiterText, true);

        footerText = new TextArea();
        footerText.setPromptText(t(DM.FILE_BUILDER_AFTER_THE_LAST_ELEMENT));
        footerText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(footerText, true);
        footerText.getStyleClass().add(ExtraStyles.MONOSPACE);
        footerText.setPrefHeight(60);

        filenameText = new TextField();
        filenameText.setMaxWidth(Double.MAX_VALUE);
        filenameText.setPromptText(FileBuilderViewModel.DEFAULT_FILE_NAME);
        GridPane.setFillWidth(delimiterText, true);

        encodingChoice = new ComboBox<>(FXCollections.observableArrayList(Encoding.values()));

        lineSeparatorChoice = new ComboBox<>(FXCollections.observableArrayList(LineSeparator.values()));

        var fileFormatBox = new HBox(Recommends.FORM_INLINE_SPACING, filenameText, encodingChoice, lineSeparatorChoice);
        fileFormatBox.setAlignment(Pos.CENTER_LEFT);

        // == GRID ==

        var form = new HorizontalForm(Recommends.FORM_HGAP, Recommends.FORM_VGAP);
        form.setPadding(new Insets(Recommends.CONTENT_SPACING, 0, 0, 0));

        form.add(t(DM.NAME), true, nameText);
        form.add(t(DM.FILE_BUILDER_DOCUMENT_START), headerText);
        form.add(t(DM.FILE_BUILDER_ELEMENT), true, patternText);
        form.add(t(DM.DELIMITER), delimiterText);
        form.add(t(DM.FILE_BUILDER_DOCUMENT_END), footerText);
        form.add(t(DM.OUTPUT_FILE), fileFormatBox);

        form.getColumnConstraints().addAll(
                new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, false),
                new ColumnConstraints(-1, 400, -1, Priority.ALWAYS, HPos.LEFT, false)
        );

        form.getRowConstraints().addAll(
                new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.CENTER, false),
                new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.CENTER, false),
                new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.CENTER, false),
                new RowConstraints(-1, -1, -1, Priority.ALWAYS, VPos.CENTER, false),
                new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.CENTER, false),
                new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.CENTER, false)
        );

        return new Tab(t(DM.PATTERN), form);
    }

    private Tab createParamsTab() {
        var typeColumn = new TableColumn<Param, Param.Type>(t(DM.TYPE));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(Param.Type.values()));
        typeColumn.setOnEditCommit(this::updateParamType);

        var nameColumn = new TableColumn<Param, String>(t(DM.NAME));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setEditable(true);
        nameColumn.setCellFactory(c -> new TextFieldTableCell<>(null, ParamNameTextFormatter::new));
        nameColumn.setOnEditCommit(this::updateParamName);

        var optionColumn = new TableColumn<Param, String>(t(DM.OPTIONS));
        optionColumn.setCellValueFactory(new PropertyValueFactory<>("option"));
        optionColumn.setEditable(true);
        optionColumn.setCellFactory(c -> new TextFieldTableCell<>(Param::isConfigurable, ParamOptionTextFormatter::new));
        optionColumn.setOnEditCommit(this::updateParamOption);

        paramTable = new TableView<>();
        paramTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        paramTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        paramTable.getColumns().setAll(List.of(typeColumn, nameColumn, optionColumn));
        paramTable.setEditable(true);
        paramTable.getSortOrder().add(nameColumn);
        VBox.setVgrow(paramTable, Priority.ALWAYS);

        // == ACTIONS ==

        addParamBtn = new Button("", new FontIcon(Material2OutlinedAL.ADD));
        addParamBtn.getStyleClass().addAll(BUTTON_ICON, SMALL);

        removeParamBtn = new Button("", new FontIcon(Material2OutlinedMZ.REMOVE));
        removeParamBtn.getStyleClass().addAll(BUTTON_ICON, SMALL);

        var toolBar = new ToolBar(addParamBtn, removeParamBtn);
        toolBar.getStyleClass().addAll("actions", Styles.BOTTOM);

        // == CONTAINER ==

        var paramGroup = new VBox(paramTable, toolBar);
        VBox.setVgrow(paramGroup, Priority.ALWAYS);

        var container = new VBox(Recommends.FORM_VGAP, paramGroup);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(Recommends.CONTENT_SPACING, 0, 0, 0));

        return new Tab(t(DM.PARAMETERS), container);
    }

    private Tab createDescriptionTab() {
        descriptionText = new TextArea();
        descriptionText.setPromptText(t(DM.DESCRIPTION));
        descriptionText.setWrapText(true);
        VBox.setVgrow(descriptionText, Priority.ALWAYS);

        var container = new VBox(descriptionText);
        container.setPadding(new Insets(Recommends.CONTENT_SPACING, 0, 0, 0));

        return new Tab(t(DM.DESCRIPTION), container);
    }

    private void addParam(Param param) {
        paramTable.getItems().add(param);
    }

    private void removeParams(List<Param> params) {
        paramTable.getItems().removeAll(params);
    }

    private void updateParamType(CellEditEvent<Param, Param.Type> event) {
        Param param = event.getRowValue();
        param.setType(event.getNewValue());
        switch (event.getNewValue()) {
            case CHOICE, CONSTANT, TIMESTAMP, UUID -> param.setOption(null);
            case DATAFAKER -> param.setOption("#{color.name}");
            case PASSWORD, PASSWORD_BASE64 -> param.setOption(String.valueOf(Param.DEFAULT_PASSWORD_LENGTH));
        }
        event.getTableView().refresh();
    }

    private void updateParamName(CellEditEvent<Param, String> event) {
        Param tableParam = event.getRowValue();
        Param testParam = findParamByName(event.getNewValue());
        boolean valid = testParam == null || // no param with that name, so new name is unique
                Objects.equals(testParam.getName(), event.getOldValue()); // dummy commit, name not changed

        if (valid) {
            tableParam.setName(event.getNewValue());
        } else {
            tableParam.setName(event.getOldValue());
            event.getTableView().refresh();
        }
    }

    private void updateParamOption(CellEditEvent<Param, String> event) {
        Param param = event.getRowValue();
        boolean valid = true;

        if (param.getType() == Param.Type.PASSWORD || param.getType() == Param.Type.PASSWORD_BASE64) {
            try {
                int len = Integer.parseInt(event.getNewValue());
                valid = len >= Param.MIN_PASSWORD_LENGTH && len <= Param.MAX_PASSWORD_LENGTH;
            } catch (Exception e) {
                valid = false;
            }
        }

        if (param.getType() == Param.Type.DATAFAKER) {
            try {
                // datafaker doesn't validate expression format
                // so #color.name (no brackets) won't throw exception,
                // but #{color.shame} (no provider) will
                var expression = event.getNewValue();

                if (StringUtils.isNotBlank(expression) && expression.startsWith("#{") && expression.endsWith("}")) {
                    TemplateWorker.FAKER.expression(event.getNewValue());
                    valid = true;
                } else {
                    valid = false;
                }
            } catch (Exception e) {
                valid = false;
            }
        }

        if (valid) {
            param.setOption(event.getNewValue());
        } else {
            param.setOption(event.getOldValue());
            event.getTableView().refresh();
        }
    }

    private @Nullable Param findParamByName(String name) {
        return paramTable.getItems().stream()
                .filter(p -> Objects.equals(p.getName(), name))
                .findFirst()
                .orElse(null);
    }

    private @Nullable String getBackingTemplateName() {
        return backingTemplate.get() != null ? backingTemplate.get().getName() : null;
    }

    private String generateParamName() {
        return PARAM_NAME_GEN.get(paramTable.getItems());
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ParamNameTextFormatter extends TextFormatter<Param> {

        public ParamNameTextFormatter() {
            super(change -> {
                String newText = change.getControlNewText();
                boolean valid = newText.isEmpty() ||
                        // _ is reserved for internal params
                        (!newText.startsWith("_") && TemplateManagerView.PARAM_NAME_PATTERN.matcher(newText).matches());
                return valid ? change : null;
            });
        }
    }

    private static final class ParamOptionTextFormatter extends CellTextFormatter<Param> {

        public ParamOptionTextFormatter() {
            super(new ParamOptionUnaryOperator());
        }

        @Override
        public void startEdit(@Nullable Param rowItem) {
            super.startEdit(rowItem);

            @Nullable Pattern pattern = null;
            var filter = (ParamOptionUnaryOperator) getFilter();

            if (rowItem != null) {
                pattern = switch (rowItem.getType()) {
                    case PASSWORD, PASSWORD_BASE64 -> TemplateManagerView.PASSWORD_LENGTH_PATTERN;
                    default -> null;
                };
            }

            filter.setPattern(pattern);
        }

        @Override
        public void finishEdit() {
            super.finishEdit();
            ((ParamOptionUnaryOperator) getFilter()).setPattern(null);
        }
    }

    private static final class ParamOptionUnaryOperator implements UnaryOperator<Change> {

        private @Nullable Pattern pattern;

        @Override
        public @Nullable Change apply(Change change) {
            if (pattern == null) { return change; }

            String newText = change.getControlNewText();
            if (newText.isEmpty() || pattern.matcher(newText).matches()) {
                return change;
            } else {
                return null;
            }
        }

        public void setPattern(@Nullable Pattern pattern) {
            this.pattern = pattern;
        }
    }
}
