package org.telekit.desktop.tools.apiclient;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.telekit.base.net.HttpConstants.ContentType;
import org.telekit.base.net.HttpConstants.Method;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.controls.widgets.OverlayDialog;
import org.telekit.desktop.tools.Action;
import org.telekit.desktop.tools.apiclient.Template.BatchSeparator;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.property.IntegerProperty.integerProperty;
import static javafx.collections.FXCollections.observableArrayList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.util.Containers.*;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.controls.util.Controls.gridLabel;
import static org.telekit.desktop.i18n.DesktopMessages.*;
import static org.telekit.desktop.tools.apiclient.Executor.BATCH_PLACEHOLDER_NAME;

public class TemplateEditor extends OverlayDialog {

    public static final String DEFAULT_BATCH_WRAPPER = "%(" + BATCH_PLACEHOLDER_NAME + ")";

    TabPane tabPane;
    TextField nameText;
    ComboBox<Method> methodChoice;
    TextField uriText;
    TextArea headersText;
    TextArea bodyText;
    Spinner<Integer> waitTimeoutSpinner;

    Spinner<Integer> batchSizeSpinner;
    TextArea batchWrapperText;
    ComboBox<BatchSeparator> batchSeparatorChoice;

    TextArea descriptionText;
    Button commitBtn;

    private Set<String> usedTemplateNames;
    private Action action;
    private Template template;
    private BiConsumer<Action, Template> onCommitCallback;

    public TemplateEditor() {
        super();
        createContent();
    }

    private void createContent() {
        // everything below initialized in parent constructor context
        usedTemplateNames = new HashSet<>();

        tabPane = stretchedTabPane(
                createParamsTab(),
                createBatchTab(),
                createDescriptionTab()
        );

        commitBtn = button(t(ACTION_OK), null, "form-action");
        commitBtn.setDefaultButton(true);
        commitBtn.disableProperty().bind(createBooleanBinding(() -> {
            String name = trim(nameText.getText());
            String uri = trim(uriText.getText());
            return isEmpty(name) || isEmpty(uri) || usedTemplateNames.contains(name);
        }, nameText.textProperty(), uriText.textProperty()));

        commitBtn.setOnAction(e -> commit());

        footerBox.getChildren().add(1, commitBtn);
        bottomCloseBtn.setText(t(ACTION_CANCEL));

        setContent(tabPane);
        setPrefWidth(500);
    }

    private Tab createParamsTab() {
        nameText = new TextField();

        methodChoice = new ComboBox<>(observableArrayList(Method.values()));
        methodChoice.setPrefWidth(100);

        uriText = new TextField();
        HBox.setHgrow(uriText, Priority.ALWAYS);

        HBox methodBox = hbox(0, Pos.CENTER_LEFT, Insets.EMPTY);
        methodBox.getChildren().addAll(methodChoice, uriText);

        headersText = Controls.create(TextArea::new, "monospace");
        headersText.setPrefHeight(60);

        bodyText = Controls.create(TextArea::new, "monospace");
        bodyText.setPrefHeight(120);

        waitTimeoutSpinner = new Spinner<>(1, 100, 3, 1);
        waitTimeoutSpinner.setPrefWidth(200);

        HBox waitTimeoutBox = hbox(10, Pos.CENTER_LEFT, Insets.EMPTY);
        waitTimeoutBox.getChildren().setAll(waitTimeoutSpinner, new Label(t(TOOLS_SECONDS)));

        // GRID

        GridPane grid = Containers.gridPane(20, 10, new Insets(10));

        grid.add(gridLabel("* " + t(NAME), HPos.RIGHT, nameText), 0, 0);
        grid.add(nameText, 1, 0);

        grid.add(gridLabel("* URI", HPos.RIGHT, uriText), 0, 1);
        grid.add(methodBox, 1, 1);

        grid.add(gridLabel(t(APICLIENT_HTTP_HEADERS), HPos.RIGHT, headersText), 0, 2);
        grid.add(headersText, 1, 2);

        grid.add(gridLabel(t(APICLIENT_BODY), HPos.RIGHT, bodyText), 0, 3);
        grid.add(bodyText, 1, 3);

        grid.add(gridLabel(t(APICLIENT_WAIT_TIMEOUT), HPos.RIGHT, waitTimeoutSpinner), 0, 4);
        grid.add(waitTimeoutBox, 1, 4);

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

    private Tab createBatchTab() {
        batchSizeSpinner = new Spinner<>(0, 100, 0, 5);
        batchSizeSpinner.setPrefWidth(200);
        batchSizeSpinner.valueProperty().addListener((obs, old, value) -> {
            if (value == null) { return; }
            String text = batchWrapperText.getText();
            if (value > 1 && isBlank(text)) {
                batchWrapperText.setText(DEFAULT_BATCH_WRAPPER);
            } else if (value <= 1) {
                batchWrapperText.setText("");
            }
        });

        batchWrapperText = Controls.create(TextArea::new, "monospace");

        batchSeparatorChoice = new ComboBox<>(observableArrayList(BatchSeparator.values()));
        batchSeparatorChoice.setPrefWidth(200);

        BooleanBinding batchSizeLessThatOne = Bindings.lessThan(
                integerProperty(batchSizeSpinner.getValueFactory().valueProperty()), 2
        );
        batchWrapperText.disableProperty().bind(batchSizeLessThatOne);
        batchSeparatorChoice.disableProperty().bind(batchSizeLessThatOne);

        // GRID

        GridPane grid = Containers.gridPane(20, 10, new Insets(10));

        grid.add(gridLabel(t(APICLIENT_BATCH_SIZE), HPos.RIGHT, batchSizeSpinner), 0, 0);
        grid.add(batchSizeSpinner, 1, 0);

        grid.add(gridLabel(t(APICLIENT_BATCH_WRAPPER), HPos.RIGHT, batchWrapperText), 0, 1);
        grid.add(batchWrapperText, 1, 1);

        grid.add(gridLabel(t(SEPARATOR), HPos.RIGHT, batchSeparatorChoice), 0, 2);
        grid.add(batchSeparatorChoice, 1, 2);

        grid.getColumnConstraints().addAll(columnConstraints(80, Priority.SOMETIMES), HGROW_ALWAYS);
        grid.getRowConstraints().addAll(VGROW_NEVER, VGROW_ALWAYS, VGROW_NEVER);

        return new Tab(t(TOOLS_BATCH), grid);
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
            template.setHeaders(ContentType.APPLICATION_JSON.toHeader(StandardCharsets.UTF_8));
            template.setBatchSeparator(BatchSeparator.COMMA);
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
        uriText.setText(template.getUri());
        methodChoice.getSelectionModel().select(template.getMethod());
        headersText.setText(template.getHeaders());
        bodyText.setText(template.getBody());
        batchSizeSpinner.getValueFactory().setValue(template.getBatchSize());
        batchWrapperText.setText(template.getBatchWrapper());
        batchSeparatorChoice.getSelectionModel().select(template.getBatchSeparator());
        waitTimeoutSpinner.getValueFactory().setValue(template.getWaitTimeout());
        descriptionText.setText(template.getDescription());

        setTitle(t(titleKey));
        tabPane.getSelectionModel().selectFirst();
    }

    public void commit() {
        template.setName(trim(nameText.getText()));
        template.setMethod(methodChoice.getSelectionModel().getSelectedItem());
        template.setUri(trim(uriText.getText()));
        template.setHeaders(trim(headersText.getText()));
        template.setBody(trim(bodyText.getText()));
        template.setDescription(trim(descriptionText.getText()));
        template.setBatchSize(batchSizeSpinner.getValue());
        template.setBatchWrapper(batchWrapperText.getText());
        template.setBatchSeparator(batchSeparatorChoice.getSelectionModel().getSelectedItem());
        template.setWaitTimeout(waitTimeoutSpinner.getValue());

        if (onCommitCallback != null) {
            onCommitCallback.accept(action, new Template(template));
        }
    }

    public void setOnCommit(BiConsumer<Action, Template> handler) {
        this.onCommitCallback = handler;
    }
}
