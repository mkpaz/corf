package corf.desktop.tools.httpsender;

import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Nullable;
import corf.base.Injector;
import corf.base.desktop.controls.ModalDialog;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;

import static atlantafx.base.theme.Styles.TITLE_4;
import static corf.base.i18n.I18n.t;

final class TemplateManagerDialog extends ModalDialog {

    TemplateManagerView view = createTemplateManager();

    public TemplateManagerDialog() {
        super();

        setContent(createContent());
    }

    private Content createContent() {
        var titleLabel = new Label(t(DM.TPL_TEMPLATE_MANAGER));
        titleLabel.getStyleClass().add(TITLE_4);

        view.setCloseHandler(e -> {
            if (view.isDirty()) {
                Events.fire(new ActionEvent<>(EventID.TEMPLATE_MANAGER_RELOAD, HttpSenderTool.EVENT_SOURCE));
            }
            view.reset();
            this.close();
        });

        var body = new VBox(view);
        VBox.setVgrow(view, Priority.ALWAYS);

        return new Content(titleLabel, body, null);
    }

    public void setSelection(@Nullable Template template) {
        view.getViewModel().getSelectionModel().select(template);
    }

    private TemplateManagerView createTemplateManager() {
        return Injector.getInstance().getBean(TemplateManagerView.class);
    }
}
