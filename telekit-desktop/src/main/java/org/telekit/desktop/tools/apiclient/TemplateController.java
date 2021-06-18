package org.telekit.desktop.tools.apiclient;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.desktop.ModalController;
import org.telekit.base.i18n.I18n;
import org.telekit.base.net.HttpConstants.ContentType;
import org.telekit.base.net.HttpConstants.Method;
import org.telekit.controls.util.BindUtils;
import org.telekit.desktop.tools.Action;
import org.telekit.desktop.tools.apiclient.Template.BatchSeparator;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static javafx.beans.property.IntegerProperty.integerProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.desktop.i18n.DesktopMessages.TOOLS_EDIT_TEMPLATE;
import static org.telekit.desktop.i18n.DesktopMessages.TOOLS_NEW_TEMPLATE;
import static org.telekit.desktop.tools.apiclient.Executor.BATCH_PLACEHOLDER_NAME;

@FxmlPath("/org/telekit/desktop/tools/apiclient/template.fxml")
public class TemplateController implements Component, ModalController {

    public static final String DEFAULT_BATCH_WRAPPER = "%(" + BATCH_PLACEHOLDER_NAME + ")";

    public @FXML VBox rootPane;
    public @FXML TabPane tabPane;
    public @FXML TextField tfName;
    public @FXML ComboBox<Method> cmbMethod;
    public @FXML TextField tfURI;
    public @FXML TextArea taHeaders;
    public @FXML TextArea taBody;
    public @FXML Spinner<Integer> spnWaitTimeout;
    public @FXML Spinner<Integer> spnBatchSize;
    public @FXML TextArea taBatchWrapper;
    public @FXML ComboBox<BatchSeparator> cmbBatchSeparator;
    public @FXML TextArea taDescription;
    public @FXML Button btnSubmit;

    private final Set<String> usedTemplateNames = new HashSet<>();
    private Action action;
    private Template template;

    private BiConsumer<Action, Template> onSubmitCallback;
    private Runnable onCancelCallback;

    @FXML
    public void initialize() {
        // full width tabs
        tabPane.tabMinWidthProperty().bind(tabPane.widthProperty().divide(tabPane.getTabs().size()).subtract(20));

        btnSubmit.disableProperty().bind(BindUtils.or(
                BindUtils.isBlank(tfName.textProperty()),
                BindUtils.isBlank(tfURI.textProperty()),
                BindUtils.contains(tfName.textProperty(), usedTemplateNames, StringUtils::trim))
        );

        spnBatchSize.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) { return; }
            String batchWrapperText = taBatchWrapper.getText();
            if (newValue > 1 && isBlank(batchWrapperText)) {
                taBatchWrapper.setText(DEFAULT_BATCH_WRAPPER);
            } else if (newValue <= 1) {
                taBatchWrapper.setText("");
            }
        });
        cmbBatchSeparator.setItems(FXCollections.observableArrayList(BatchSeparator.values()));

        BooleanBinding batchSizeLessThatOne =
                Bindings.lessThan(integerProperty(spnBatchSize.getValueFactory().valueProperty()), 2);
        taBatchWrapper.disableProperty().bind(batchSizeLessThatOne);
        cmbBatchSeparator.disableProperty().bind(batchSizeLessThatOne);

        cmbMethod.getItems().addAll(Method.values());
    }

    public void setData(Action action, Template sourceTemplate, Set<String> templateNames) {
        this.action = Objects.requireNonNull(action);

        usedTemplateNames.clear();
        if (templateNames != null) { usedTemplateNames.addAll(templateNames); }

        if (sourceTemplate == null) {
            template = new Template();
            template.setHeaders(ContentType.APPLICATION_JSON.toHeader(StandardCharsets.UTF_8));
            template.setBatchSeparator(BatchSeparator.COMMA);
        } else {
            template = new Template(sourceTemplate);
        }

        String titleKey = "";
        if (action == Action.NEW || action == Action.DUPLICATE) {
            template.setId(UUID.randomUUID());
            titleKey = TOOLS_NEW_TEMPLATE;
        }
        if (action == Action.EDIT) {
            titleKey = TOOLS_EDIT_TEMPLATE;
            // bypass name check
            usedTemplateNames.remove(template.getName());
        }

        ((Stage) rootPane.getScene().getWindow()).setTitle(I18n.t(titleKey));
        tfName.setText(template.getName());
        tfURI.setText(template.getUri());
        cmbMethod.getSelectionModel().select(template.getMethod());
        taHeaders.setText(template.getHeaders());
        taBody.setText(template.getBody());
        spnBatchSize.getValueFactory().setValue(template.getBatchSize());
        taBatchWrapper.setText(template.getBatchWrapper());
        cmbBatchSeparator.getSelectionModel().select(template.getBatchSeparator());
        spnWaitTimeout.getValueFactory().setValue(template.getWaitTimeout());
        taDescription.setText(template.getDescription());

        tabPane.getSelectionModel().selectFirst();
    }

    @FXML
    public void submit() {
        template.setName(trim(tfName.getText()));
        template.setMethod(cmbMethod.getSelectionModel().getSelectedItem());
        template.setUri(trim(tfURI.getText()));
        template.setHeaders(trim(taHeaders.getText()));
        template.setBody(trim(taBody.getText()));
        template.setDescription(trim(taDescription.getText()));
        template.setBatchSize(spnBatchSize.getValue());
        template.setBatchWrapper(taBatchWrapper.getText());
        template.setBatchSeparator(cmbBatchSeparator.getSelectionModel().getSelectedItem());
        template.setWaitTimeout(spnWaitTimeout.getValue());

        if (onSubmitCallback != null) { onSubmitCallback.accept(action, new Template(template)); }
    }

    @FXML
    public void cancel() {
        if (onCancelCallback != null) { onCancelCallback.run(); }
    }

    @Override
    public Region getRoot() { return rootPane; }

    @Override
    public void reset() {}

    public void setOnSubmit(BiConsumer<Action, Template> handler) { this.onSubmitCallback = handler; }

    @Override
    public Runnable getOnCloseRequest() { return onCancelCallback; }

    @Override
    public void setOnCloseRequest(Runnable handler) { this.onCancelCallback = handler; }
}
