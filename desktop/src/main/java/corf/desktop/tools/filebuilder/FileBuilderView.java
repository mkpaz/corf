package corf.desktop.tools.filebuilder;

import atlantafx.base.theme.Styles;
import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import corf.base.common.Lazy;
import corf.base.desktop.Focusable;
import corf.base.desktop.Observables;
import corf.base.desktop.Overlay;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import jakarta.inject.Inject;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import static atlantafx.base.theme.Styles.ACCENT;
import static atlantafx.base.theme.Styles.LARGE;
import static corf.base.i18n.I18n.t;

public final class FileBuilderView extends VBox implements View<FileBuilderView, FileBuilderViewModel>,
    Initializable,
    Focusable {

    static final int LEFT_AREA_WIDTH = 400;
    static final int TOOL_MAX_WIDTH = 1440;

    Button generateBtn;
    TemplateCard templateCard;
    ParamsCard paramsCard;

    private final FileBuilderViewModel model;
    private final Overlay overlay;
    private final Lazy<TemplateManagerDialog> templateManagerDialog;
    private final Lazy<SaveDialog> saveDialog;

    @Inject
    public FileBuilderView(FileBuilderViewModel model, Overlay overlay) {
        super();

        this.model = model;
        this.overlay = overlay;

        this.templateManagerDialog = new Lazy<>(() -> {
            var dialog = new TemplateManagerDialog();
            dialog.setOnCloseRequest(() -> {
                overlay.hide();
                dialog.setSelection(null);
            });
            return dialog;
        });

        this.saveDialog = new Lazy<>(() -> {
            var dialog = new SaveDialog(model);
            dialog.setOnCloseRequest(overlay::hide);
            return dialog;
        });

        createView();
    }

    private void createView() {
        templateCard = new TemplateCard(this);

        var sep = new Separator();
        sep.getStyleClass().add(Styles.SMALL);

        paramsCard = new ParamsCard(this);
        VBox.setVgrow(paramsCard, Priority.ALWAYS);

        generateBtn = new Button(t(DM.ACTION_GENERATE), new FontIcon(Material2OutlinedMZ.SHUFFLE));
        generateBtn.getStyleClass().addAll(ACCENT, LARGE);

        var actionsBox = new HBox(generateBtn);
        actionsBox.setAlignment(Pos.CENTER);

        setMaxWidth(TOOL_MAX_WIDTH);
        setPadding(Recommends.TOOL_PADDING);
        setSpacing(Recommends.CONTENT_SPACING);
        getChildren().setAll(templateCard, sep, paramsCard, actionsBox);
        setId("file-builder");
    }

    @Override
    public void init() {
        generateBtn.disableProperty().bind(Observables.or(
            model.selectedTemplateProperty().isNull(),
            model.ongoingProperty(),
            Observables.isBlank(model.csvTextProperty())
        ));
        generateBtn.setOnAction(e -> showSaveDialog());

        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.TEMPLATE_MANAGER_SHOW, FileBuilderTool.EVENT_SOURCE)) {
                TemplateManagerDialog dialog = templateManagerDialog.get();
                if (e.getPayload() instanceof Template template) {
                    dialog.setSelection(template);
                }
                overlay.show(dialog, Pos.TOP_CENTER, Recommends.MODAL_WINDOW_MARGIN);
            }
        });
    }

    @Override
    public FileBuilderView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public FileBuilderViewModel getViewModel() {
        return model;
    }

    @Override
    public Node getPrimaryFocusNode() {
        return templateCard.templateChoice;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    private void showSaveDialog() {
        model.setTemplateParams(paramsCard.getEditedParams());
        var dialog = saveDialog.get();
        dialog.prepare();
        overlay.show(dialog, Pos.TOP_CENTER, Recommends.MODAL_WINDOW_MARGIN);
    }
}
