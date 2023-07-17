package corf.desktop.tools.httpsender;

import atlantafx.base.theme.Styles;
import jakarta.inject.Inject;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import corf.base.common.Lazy;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.desktop.EventID;
import corf.desktop.layout.Recommends;

import static corf.base.desktop.ExtraStyles.BG_DEFAULT;

final class TemplateTab extends VBox {

    TemplateCard templateCard;
    ParamsCard paramsCard;

    private final HttpSenderView view;
    private final Lazy<TemplateManagerDialog> templateManagerDialog;

    @Inject
    public TemplateTab(HttpSenderView view) {
        super();

        this.view = view;

        this.templateManagerDialog = new Lazy<>(() -> {
            var dialog = new TemplateManagerDialog();
            dialog.setOnCloseRequest(() -> {
                view.hideOverlay();
                dialog.setSelection(null);
            });
            return dialog;
        });

        createView();
        init();
    }

    private void createView() {
        templateCard = new TemplateCard(view);

        paramsCard = new ParamsCard(view);
        VBox.setVgrow(paramsCard, Priority.ALWAYS);

        var sep = new Separator();
        sep.getStyleClass().add(Styles.SMALL);

        getStyleClass().add(BG_DEFAULT);
        setSpacing(Recommends.CONTENT_SPACING);
        getChildren().setAll(templateCard, sep, paramsCard);
        setId("file-builder");
    }

    public void init() {
        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.TEMPLATE_MANAGER_SHOW, HttpSenderTool.EVENT_SOURCE)) {
                TemplateManagerDialog dialog = templateManagerDialog.get();
                if (e.getPayload() instanceof Template template) {
                    dialog.setSelection(template);
                }
                view.showOverlay(dialog);
            }
        });
    }
}
