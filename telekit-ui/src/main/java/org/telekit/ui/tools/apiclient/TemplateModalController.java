package org.telekit.ui.tools.apiclient;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.telekit.base.EventBus;
import org.telekit.base.domain.HttpConstants.ContentType;
import org.telekit.base.domain.HttpConstants.Method;
import org.telekit.base.i18n.Messages;
import org.telekit.base.ui.Controller;
import org.telekit.controls.util.ExtraBindings;
import org.telekit.ui.tools.Action;

import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.*;
import static org.telekit.ui.MessageKeys.TOOLS_EDIT_TEMPLATE;
import static org.telekit.ui.MessageKeys.TOOLS_NEW_TEMPLATE;

public class TemplateModalController extends Controller {

    public @FXML VBox rootPane;
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
    public @FXML Button btnApply;
    public @FXML TabPane taTabs;

    private Action action;
    private Template template;
    private Set<String> usedTemplateNames;

    @FXML
    public void initialize() {
        BooleanBinding isNameNotUnique = isNameNotUnique(tfName.textProperty());

        btnApply.disableProperty().bind(
                ExtraBindings.isBlank(tfName.textProperty())
                        .or(isNameNotUnique
                                    .or(ExtraBindings.isBlank(tfURI.textProperty()))
                        )
        );

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
                Bindings.lessThan(IntegerProperty.integerProperty(spnBatchSize.getValueFactory().valueProperty()), 2)
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
        cmbContentType.getItems().add(null);
        cmbContentType.getItems().addAll(ContentType.values());
    }

    @FXML
    public void onContentTypeChanged() {
        if (spnBatchSize.getValue() > 1) {
            ContentType contentType = cmbContentType.getSelectionModel().getSelectedItem();
            taBatchWrapper.setText(getDefaultBatchWrapper(contentType));
        }
    }

    public void setData(Action action, Template sourceTemplate, Set<String> usedTemplateNames) {
        this.action = action;
        if (sourceTemplate == null) {
            this.template = new Template();
            this.template.setContentType(ContentType.APPLICATION_JSON);
        } else {
            this.template = new Template(sourceTemplate);
        }
        this.usedTemplateNames = usedTemplateNames;

        String titleKey = "";
        if (action == Action.NEW || action == Action.DUPLICATE) {
            this.template.setId(UUID.randomUUID());
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

        taTabs.getSelectionModel().selectFirst();
    }

    @FXML
    public void apply() {
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

        rootPane.getScene().getWindow().hide();
        EventBus.getInstance().publish(new TemplateUpdateEvent(this.action, new Template(this.template)));
    }

    @FXML
    public void cancel() {
        rootPane.getScene().getWindow().hide();
    }

    @Override
    public void reset() { /* not yet implemented */ }

    private BooleanBinding isNameNotUnique(StringProperty textProperty) {
        return Bindings.createBooleanBinding(
                () -> textProperty.get() != null &&
                        usedTemplateNames != null &&
                        action != Action.EDIT &&
                        usedTemplateNames.contains(trim(textProperty.get())),
                textProperty
        );
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

    ///////////////////////////////////////////////////////////////////////////

    public static class TemplateUpdateEvent {

        private final Action action;
        private final Template template;

        public TemplateUpdateEvent(Action action, Template template) {
            this.action = action;
            this.template = template;
        }

        public Action getAction() {
            return action;
        }

        public Template getTemplate() {
            return template;
        }
    }
}
