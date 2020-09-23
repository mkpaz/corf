package org.telekit.ui.tools.import_file_builder;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.telekit.base.EventBus;
import org.telekit.base.i18n.Messages;
import org.telekit.base.domain.Encoding;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.fx.Controller;
import org.telekit.base.fx.FXBindings;
import org.telekit.ui.tools.Action;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.ui.main.MessageKeys.TOOLS_EDIT_TEMPLATE;
import static org.telekit.ui.main.MessageKeys.TOOLS_NEW_TEMPLATE;

public class TemplateModalController extends Controller {

    public @FXML VBox rootPane;
    public @FXML TextField tfName;
    public @FXML TextArea taHeader;
    public @FXML TextArea taFooter;
    public @FXML ComboBox<String> cmbDelimiter;
    public @FXML TextArea taPattern;
    public @FXML ComboBox<Encoding> cmbEncoding;
    public @FXML ComboBox<LineSeparator> cmbLineSeparator;
    public @FXML Button btnApply;
    public @FXML TextArea taDescription;
    public @FXML TabPane taTabs;

    private Action action;
    private Template template;
    private Set<String> usedTemplateNames;

    @FXML
    public void initialize() {
        BooleanBinding isNameNotUnique = isNameNotUnique(tfName.textProperty());

        cmbDelimiter.setConverter(new DelimiterStringConverter());
        cmbEncoding.getItems().setAll(Encoding.values());
        cmbLineSeparator.getItems().setAll(LineSeparator.values());

        btnApply.disableProperty().bind(
                FXBindings.isBlank(tfName.textProperty())
                        .or(isNameNotUnique
                                    .or(FXBindings.isBlank(taPattern.textProperty()))
                        )
        );
    }

    public void setData(Action action, Template sourceTemplate, Set<String> usedTemplateNames) {
        this.action = action;
        this.template = sourceTemplate == null ? new Template() : new Template(sourceTemplate);
        this.usedTemplateNames = usedTemplateNames;

        String titleKey = "";
        if (action == Action.NEW || action == Action.DUPLICATE) titleKey = TOOLS_NEW_TEMPLATE;
        if (action == Action.EDIT) titleKey = TOOLS_EDIT_TEMPLATE;

        ((Stage) rootPane.getScene().getWindow()).setTitle(Messages.get(titleKey));
        tfName.setText(template.getName());
        taHeader.setText(template.getHeader());
        taFooter.setText(template.getFooter());
        cmbDelimiter.getSelectionModel().select(template.getDelimiter());
        taPattern.setText(template.getPattern());
        taDescription.setText(template.getDescription());
        cmbEncoding.getSelectionModel().select(template.getEncoding());
        cmbLineSeparator.getSelectionModel().select(template.getLineSeparator());

        taTabs.getSelectionModel().selectFirst();
    }

    @FXML
    public void apply() {
        template.setName(trim(tfName.getText()));
        template.setHeader(trim(taHeader.getText()));
        template.setFooter(trim(taFooter.getText()));
        template.setDelimiter(cmbDelimiter.getSelectionModel().getSelectedItem());
        template.setPattern(trim(taPattern.getText()));
        template.setDescription(trim(taDescription.getText()));
        template.setEncoding(cmbEncoding.getSelectionModel().getSelectedItem());
        template.setLineSeparator(cmbLineSeparator.getSelectionModel().getSelectedItem());

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
