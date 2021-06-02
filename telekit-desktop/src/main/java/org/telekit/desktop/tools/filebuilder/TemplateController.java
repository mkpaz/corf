package org.telekit.desktop.tools.filebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.domain.Encoding;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.i18n.Messages;
import org.telekit.controls.util.BooleanBindings;
import org.telekit.desktop.tools.Action;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.desktop.MessageKeys.TOOLS_EDIT_TEMPLATE;
import static org.telekit.desktop.MessageKeys.TOOLS_NEW_TEMPLATE;

@FxmlPath("/org/telekit/desktop/tools/filebuilder/_root.fxml")
public class TemplateController implements Component {

    public @FXML VBox rootPane;
    public @FXML TabPane tabPane;
    public @FXML TextField tfName;
    public @FXML TextArea taHeader;
    public @FXML TextArea taFooter;
    public @FXML ComboBox<String> cmbDelimiter;
    public @FXML TextArea taPattern;
    public @FXML ComboBox<Encoding> cmbEncoding;
    public @FXML ComboBox<LineSeparator> cmbLineSeparator;
    public @FXML Button btnSubmit;
    public @FXML TextArea taDescription;

    private final Set<String> usedTemplateNames = new HashSet<>();
    private Action action;
    private Template template;

    private BiConsumer<Action, Template> onSubmitCallback;
    private Runnable onCancelCallback;

    @FXML
    public void initialize() {
        cmbDelimiter.setConverter(new DelimiterStringConverter());
        cmbEncoding.getItems().setAll(Encoding.values());
        cmbLineSeparator.getItems().setAll(LineSeparator.values());
        btnSubmit.disableProperty().bind(BooleanBindings.or(
                BooleanBindings.isBlank(tfName.textProperty()),
                BooleanBindings.isBlank(taPattern.textProperty()),
                BooleanBindings.contains(tfName.textProperty(), usedTemplateNames, StringUtils::trim)
        ));
    }

    public void setData(Action action, Template sourceTemplate, Set<String> templateNames) {
        this.action = Objects.requireNonNull(action);

        usedTemplateNames.clear();
        if (templateNames != null) usedTemplateNames.addAll(templateNames);

        if (sourceTemplate == null) {
            template = new Template();
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

        ((Stage) rootPane.getScene().getWindow()).setTitle(Messages.get(titleKey));
        tfName.setText(template.getName());
        taHeader.setText(template.getHeader());
        taFooter.setText(template.getFooter());
        cmbDelimiter.getSelectionModel().select(template.getDelimiter());
        taPattern.setText(template.getPattern());
        taDescription.setText(template.getDescription());
        cmbEncoding.getSelectionModel().select(template.getEncoding());
        cmbLineSeparator.getSelectionModel().select(template.getLineSeparator());

        tabPane.getSelectionModel().selectFirst();
    }

    @FXML
    public void submit() {
        template.setName(trim(tfName.getText()));
        template.setHeader(trim(taHeader.getText()));
        template.setFooter(trim(taFooter.getText()));
        template.setDelimiter(cmbDelimiter.getSelectionModel().getSelectedItem());
        template.setPattern(trim(taPattern.getText()));
        template.setDescription(trim(taDescription.getText()));
        template.setEncoding(cmbEncoding.getSelectionModel().getSelectedItem());
        template.setLineSeparator(cmbLineSeparator.getSelectionModel().getSelectedItem());

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

    public void setOnCancel(Runnable handler) { this.onCancelCallback = handler; }
}
