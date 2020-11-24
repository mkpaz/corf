package org.telekit.ui.tools.apiclient;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.domain.HttpConstants.ContentType;
import org.telekit.base.domain.HttpConstants.Method;
import org.telekit.base.event.CancelEvent;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.Controller;
import org.telekit.controls.util.BooleanBindings;
import org.telekit.ui.tools.Action;
import org.telekit.ui.tools.SubmitActionEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static javafx.beans.property.IntegerProperty.integerProperty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.ui.MessageKeys.TOOLS_EDIT_TEMPLATE;
import static org.telekit.ui.MessageKeys.TOOLS_NEW_TEMPLATE;

public class TemplateModalController extends Controller {

    public @FXML VBox rootPane;
    public @FXML TabPane tabPane;
    public @FXML TextField tfName;
    public @FXML TextField tfURI;
    public @FXML ComboBox<Method> cmbMethod;
    public @FXML ComboBox<ContentType> cmbContentType;
    public @FXML TextArea taHeaders;
    public @FXML TextArea taBody;
    public @FXML TextArea taBatchWrapper;
    public @FXML Spinner<Integer> spnBatchSize;
    public @FXML Spinner<Integer> spnWaitTimeout;
    public @FXML TextArea taDescription;
    public @FXML Button btnSubmit;

    private final Set<String> usedTemplateNames = new HashSet<>();
    private Action action;
    private Template template;

    @FXML
    public void initialize() {
        btnSubmit.disableProperty().bind(BooleanBindings.or(
                BooleanBindings.isBlank(tfName.textProperty()),
                BooleanBindings.isBlank(tfURI.textProperty()),
                BooleanBindings.contains(tfName.textProperty(), usedTemplateNames, StringUtils::trim)
        ));

        spnBatchSize.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            ContentType contentType = cmbContentType.getSelectionModel().getSelectedItem();
            String batchWrapperText = taBatchWrapper.getText();
            if (newValue > 1 && isBlank(batchWrapperText)) {
                taBatchWrapper.setText(getDefaultBatchWrapper(contentType));
            } else if (newValue <= 1) {
                taBatchWrapper.setText("");
            }
        });

        taBatchWrapper.disableProperty().bind(
                Bindings.lessThan(integerProperty(spnBatchSize.getValueFactory().valueProperty()), 2)
        );

        cmbContentType.setConverter(new StringConverter<>() {

            @Override
            public String toString(ContentType contentType) {
                return contentType != null ? contentType.getMimeType() : "";
            }

            @Override
            public ContentType fromString(String mimeType) {
                return isNotEmpty(mimeType) ? ContentType.fromValue(mimeType) : null;
            }
        });

        cmbMethod.getItems().addAll(Method.values());
        cmbContentType.getItems().add(null); // because it's an optional param
        cmbContentType.getItems().addAll(ContentType.values());
    }

    @FXML
    public void onContentTypeChanged() {
        if (spnBatchSize.getValue() > 1) {
            ContentType contentType = cmbContentType.getSelectionModel().getSelectedItem();
            taBatchWrapper.setText(getDefaultBatchWrapper(contentType));
        }
    }

    public void setData(Action action, Template sourceTemplate, Set<String> templateNames) {
        this.action = Objects.requireNonNull(action);

        usedTemplateNames.clear();
        if (templateNames != null) usedTemplateNames.addAll(templateNames);

        if (sourceTemplate == null) {
            template = new Template();
            template.setContentType(ContentType.APPLICATION_JSON);
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
        }

        ((Stage) rootPane.getScene().getWindow()).setTitle(Messages.get(titleKey));
        tfName.setText(template.getName());
        tfURI.setText(template.getUri());
        cmbMethod.getSelectionModel().select(template.getMethod());
        cmbContentType.getSelectionModel().select(template.getContentType());
        taHeaders.setText(template.getHeaders());
        taBody.setText(template.getBody());
        spnBatchSize.getValueFactory().setValue(template.getBatchSize());
        taBatchWrapper.setText(template.getBatchWrapper());
        spnWaitTimeout.getValueFactory().setValue(template.getWaitTimeout());
        taDescription.setText(template.getDescription());

        tabPane.getSelectionModel().selectFirst();
    }

    @FXML
    public void submit() {
        ContentType contentType = cmbContentType.getSelectionModel().getSelectedItem();

        template.setName(trim(tfName.getText()));
        template.setUri(trim(tfURI.getText()));
        template.setMethod(cmbMethod.getSelectionModel().getSelectedItem());
        template.setContentType(contentType);
        template.setHeaders(trim(taHeaders.getText()));
        template.setBody(trim(taBody.getText()));
        template.setBatchSize(spnBatchSize.getValue());
        template.setBatchWrapper(taBatchWrapper.getText());
        template.setWaitTimeout(spnWaitTimeout.getValue());
        template.setDescription(trim(taDescription.getText()));

        eventBus.publish(new SubmitActionEvent<>(new Template(template), action));
    }

    @FXML
    public void cancel() {
        eventBus.publish(new CancelEvent());
    }

    private String getDefaultBatchWrapper(ContentType contentType) {
        String noWrapper = "%(batch)";
        if (contentType == null) return noWrapper;
        return switch (contentType) {
            case APPLICATION_JSON -> "[" + noWrapper + "]";
            case APPLICATION_SOAP_XML, TEXT_XML -> "<tagName>" + noWrapper + "</tagName>";
            default -> noWrapper;
        };
    }
}
