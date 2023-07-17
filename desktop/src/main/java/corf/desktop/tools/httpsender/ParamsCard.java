package corf.desktop.tools.httpsender;

import static atlantafx.base.theme.Styles.ACCENT;
import static atlantafx.base.theme.Styles.BUTTON_CIRCLE;
import static atlantafx.base.theme.Styles.FLAT;
import static atlantafx.base.theme.Styles.TEXT_BOLD;
import static atlantafx.base.theme.Styles.TEXT_MUTED;
import static atlantafx.base.theme.Styles.TITLE_4;
import static corf.base.i18n.I18n.t;

import corf.base.common.Lazy;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.ui.CSVTextArea;
import corf.desktop.tools.common.ui.NamedParamsHelpDialog;
import corf.desktop.tools.common.ui.ParamList;
import corf.desktop.tools.common.ui.TitleHelpLabel;
import java.util.ArrayList;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

final class ParamsCard extends VBox {

    Button namedParamsHelpBtn;
    ParamList paramList;
    CSVTextArea csvText;

    private final HttpSenderView view;
    private final HttpSenderViewModel model;
    private final Lazy<NamedParamsHelpDialog> namedParamsHelpDialog;

    public ParamsCard(HttpSenderView view) {
        super();

        this.view = view;
        this.model = view.getViewModel();

        this.namedParamsHelpDialog = new Lazy<>(() -> {
            var dialog = new NamedParamsHelpDialog();
            dialog.setOnCloseRequest(() -> view.getOverlay().hide());
            return dialog;
        });

        setSpacing(Recommends.CARD_SPACING);
        getChildren().setAll(createTitle(), createBody());
        init();
    }

    private Label createTitle() {
        var label = new Label(t(DM.PARAMETERS));
        label.getStyleClass().addAll(TITLE_4);

        return label;
    }

    private GridPane createBody() {
        paramList = new ParamList(model.getCompletionRegistry(), view.getOverlay());
        paramList.setMinWidth(HttpSenderView.LEFT_AREA_WIDTH);
        paramList.setMaxWidth(HttpSenderView.LEFT_AREA_WIDTH);
        paramList.setEditTemplateHandler(() -> model.openTemplateManagerCommand().run());

        csvText = new CSVTextArea();
        GridPane.setHgrow(csvText, Priority.ALWAYS);
        GridPane.setVgrow(csvText, Priority.ALWAYS);

        var grid = new GridPane();
        grid.getRowConstraints().setAll(new RowConstraints(-1, -1, -1),
            new RowConstraints(-1, -1, Double.MAX_VALUE, Priority.ALWAYS, VPos.TOP, true),
            new RowConstraints(-1, -1, -1));
        grid.setVgap(Recommends.CAPTION_MARGIN);
        grid.setHgap(Recommends.FORM_HGAP);

        namedParamsHelpBtn = createHelpButton();
        grid.add(createSubHeader(t(DM.TPL_NAMED_PARAMS), namedParamsHelpBtn), 0, 0);
        grid.add(paramList, 0, 1);

        grid.add(createSubHeader(t(DM.TPL_ROW_PARAMS), null), 1, 0);
        grid.add(csvText, 1, 1);
        VBox.setVgrow(grid, Priority.ALWAYS);

        return grid;
    }

    private void init() {
        model.selectedTemplateProperty().addListener((obs, old, val) -> {
            if (val != null && CollectionUtils.isNotEmpty(val.getParams())) {
                var params = new ArrayList<>(val.getParams());
                params.sort(Param.COMPARATOR);
                paramList.setItems(params);
            } else {
                paramList.clearItems();
            }
        });

        csvText.textProperty().bindBidirectional(model.csvTextProperty());

        var template = model.getSelectedTemplate();
        paramList.setItems(template != null ? template.getParams() : null);

        namedParamsHelpBtn.setOnAction(e -> {
            var dialog = namedParamsHelpDialog.get();
            view.getOverlay().show(dialog, Pos.CENTER, Recommends.MODAL_WINDOW_MARGIN);
        });
    }

    private Node createSubHeader(String text, @Nullable Button helpBtn) {
        var label = new Label(text);
        label.getStyleClass().addAll(TEXT_BOLD, TEXT_MUTED);

        if (helpBtn == null) {
            return label;
        }

        var box = new HBox(TitleHelpLabel.GRAPHIC_TEXT_GAP, label, helpBtn);
        box.setAlignment(Pos.BASELINE_LEFT);
        return box;
    }

    private Button createHelpButton() {
        var button = new Button("", new FontIcon(Material2OutlinedAL.LIVE_HELP));
        button.getStyleClass().addAll(BUTTON_CIRCLE, FLAT, ACCENT);
        button.setPadding(new Insets(2));
        button.setCursor(Cursor.HAND);
        return button;
    }

    Set<Param> getEditedParams() {
        return paramList.getEditedParams();
    }
}
