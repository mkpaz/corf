package org.telekit.desktop.tools.filebuilder;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.telekit.base.domain.Encoding;
import org.telekit.base.domain.LineSeparator;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.controls.widgets.OverlayDialog;
import org.telekit.desktop.tools.Action;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.controls.util.Controls.gridLabel;
import static org.telekit.desktop.i18n.DesktopMessages.*;

public class TemplateEditor extends OverlayDialog {

    static final Set<String> DELIMITERS = Set.of("", ",", ":", "|", ";", "\\t");

    TabPane tabPane;
    TextField nameText;
    TextArea headerText;
    TextArea footerText;
    ComboBox<String> delimiterChoice;
    TextArea patternText;
    ComboBox<Encoding> encodingChoice;
    ComboBox<LineSeparator> lineSeparatorChoice;
    TextArea descriptionText;
    Button commitBtn;

    private Set<String> usedTemplateNames;
    private Action action;
    private Template template;
    private BiConsumer<Action, Template> onCommitCallback;

    public TemplateEditor() {
        super();
    }

    @Override
    protected Region createContent() {
        // everything below initialized in parent constructor context
        usedTemplateNames = new HashSet<>();

        tabPane = stretchedTabPane(
                createParamsTab(),
                createDescriptionTab()
        );

        commitBtn = button(t(ACTION_OK), null, "form-action");
        commitBtn.setDefaultButton(true);
        commitBtn.disableProperty().bind(createBooleanBinding(() -> {
            String name = trim(nameText.getText());
            String pattern = trim(patternText.getText());
            return isEmpty(name) || isEmpty(pattern) || usedTemplateNames.contains(name);
        }, nameText.textProperty(), patternText.textProperty()));

        commitBtn.setOnAction(e -> commit());

        footerBox.getChildren().add(1, commitBtn);
        bottomCloseBtn.setText(t(ACTION_CANCEL));

        setPrefWidth(500);

        return tabPane;
    }

    private Tab createParamsTab() {
        nameText = new TextField();

        headerText = Controls.create(TextArea::new, "monospace");
        headerText.setPrefHeight(60);

        delimiterChoice = new ComboBox<>(observableArrayList(""));
        delimiterChoice.getItems().addAll(DelimiterStringConverter.VALUES.keySet());
        delimiterChoice.setConverter(new DelimiterStringConverter());
        delimiterChoice.setPrefWidth(200);

        patternText = Controls.create(TextArea::new, "monospace");
        patternText.setPrefHeight(120);

        footerText = Controls.create(TextArea::new, "monospace");
        footerText.setPrefHeight(60);

        encodingChoice = new ComboBox<>(observableArrayList(Encoding.values()));
        encodingChoice.setPrefWidth(100);

        lineSeparatorChoice = new ComboBox<>(observableArrayList(LineSeparator.values()));
        lineSeparatorChoice.setPrefWidth(100);

        HBox fileFormatBox = hbox(20, Pos.CENTER_LEFT, Insets.EMPTY);
        fileFormatBox.getChildren().addAll(encodingChoice, lineSeparatorChoice);

        // GRID

        GridPane grid = Containers.gridPane(20, 10, new Insets(10));

        grid.add(gridLabel("* " + t(NAME), HPos.RIGHT, nameText), 0, 0);
        grid.add(nameText, 1, 0);

        grid.add(gridLabel(t(TOOLS_DOCUMENT_START), HPos.RIGHT, headerText), 0, 1);
        grid.add(headerText, 1, 1);

        grid.add(gridLabel(t(DELIMITER), HPos.RIGHT, delimiterChoice), 0, 2);
        grid.add(delimiterChoice, 1, 2);

        grid.add(gridLabel("* " + t(TOOLS_ELEMENT), HPos.RIGHT, patternText), 0, 3);
        grid.add(patternText, 1, 3);

        grid.add(gridLabel(t(TOOLS_DOCUMENT_END), HPos.RIGHT, footerText), 0, 4);
        grid.add(footerText, 1, 4);

        grid.add(gridLabel(t(FILE_FORMAT), HPos.RIGHT, fileFormatBox), 0, 5);
        grid.add(fileFormatBox, 1, 5);

        grid.getColumnConstraints().addAll(
                columnConstraints(80, Priority.SOMETIMES),
                HGROW_ALWAYS
        );

        grid.getRowConstraints().addAll(
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_NEVER,
                VGROW_ALWAYS,
                VGROW_NEVER
        );

        return new Tab(t(PARAMETERS), grid);
    }

    private Tab createDescriptionTab() {
        descriptionText = new TextArea();
        VBox.setVgrow(descriptionText, Priority.ALWAYS);

        VBox container = Containers.vbox(0, Pos.CENTER_LEFT, new Insets(20));
        container.getChildren().setAll(descriptionText);

        return new Tab(t(DESCRIPTION), container);
    }

    ///////////////////////////////////////////////////////////////////////////

    public void setData(Action action, Template source, Set<String> templateNames) {
        this.action = Objects.requireNonNull(action);

        usedTemplateNames.clear();
        if (templateNames != null) { usedTemplateNames.addAll(templateNames); }

        if (source == null) {
            template = new Template();
        } else {
            template = new Template(source);
        }

        String titleKey = "";
        if (action == Action.ADD || action == Action.DUPLICATE) {
            template.setId(UUID.randomUUID());
            titleKey = TOOLS_NEW_TEMPLATE;
        }
        if (action == Action.EDIT) {
            titleKey = TOOLS_EDIT_TEMPLATE;
            usedTemplateNames.remove(template.getName()); // bypass name check
        }

        nameText.setText(template.getName());
        headerText.setText(template.getHeader());
        footerText.setText(template.getFooter());
        delimiterChoice.getSelectionModel().select(template.getDelimiter());
        patternText.setText(template.getPattern());
        encodingChoice.getSelectionModel().select(template.getEncoding());
        lineSeparatorChoice.getSelectionModel().select(template.getLineSeparator());
        descriptionText.setText(template.getDescription());

        setTitle(t(titleKey));
        tabPane.getSelectionModel().selectFirst();
    }

    @FXML
    public void commit() {
        template.setName(trim(nameText.getText()));
        template.setHeader(trim(headerText.getText()));
        template.setFooter(trim(footerText.getText()));
        template.setDelimiter(delimiterChoice.getSelectionModel().getSelectedItem());
        template.setPattern(trim(patternText.getText()));
        template.setEncoding(encodingChoice.getSelectionModel().getSelectedItem());
        template.setLineSeparator(lineSeparatorChoice.getSelectionModel().getSelectedItem());
        template.setDescription(trim(descriptionText.getText()));

        if (onCommitCallback != null) {
            onCommitCallback.accept(action, new Template(template));
        }
    }

    public void setOnCommit(BiConsumer<Action, Template> handler) {
        this.onCommitCallback = handler;
    }
}
