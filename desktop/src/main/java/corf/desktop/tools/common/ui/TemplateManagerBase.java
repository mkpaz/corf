package corf.desktop.tools.common.ui;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.desktop.controls.Message;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

public abstract class TemplateManagerBase<T> extends BorderPane {

    protected static final int LEFT_BOX_WIDTH = 400;

    protected final VBox leftBox = new VBox();
    protected final ListView<T> templateList = new ListView<>();
    protected final StackPane content = new StackPane();
    protected final ToolBar toolBar = new ToolBar();
    protected final VBox placeholder = new VBox();
    protected final Hyperlink addLinkBtn;

    protected final CustomTextField filterText;
    protected final Button addBtn;
    protected final Button removeBtn;
    protected final Button duplicateBtn;
    protected final Button exportBtn;
    protected final Button importBtn;

    protected final Button applyBtn;
    protected final Button closeBtn;

    public TemplateManagerBase() {
        super();

        // == LEFT ==

        VBox.setVgrow(templateList, Priority.ALWAYS);

        filterText = new CustomTextField();
        filterText.setPrefWidth(160);
        filterText.setLeft(new FontIcon(Material2OutlinedMZ.SEARCH));
        filterText.setPromptText(t(DM.SEARCH));
        filterText.getStyleClass().add(SMALL);

        addBtn = createActionButton(Material2OutlinedAL.ADD, t(DM.ACTION_ADD));
        addBtn.setOnAction(e -> addTemplate());
        addBtn.getStyleClass().add(SMALL);

        removeBtn = createActionButton(Material2OutlinedMZ.REMOVE, t(DM.ACTION_REMOVE));
        removeBtn.setOnAction(e -> removeTemplate());
        removeBtn.getStyleClass().add(SMALL);

        duplicateBtn = createActionButton(Material2OutlinedAL.CONTENT_COPY, t(DM.ACTION_DUPLICATE));
        duplicateBtn.setOnAction(e -> duplicateTemplate());
        duplicateBtn.getStyleClass().add(SMALL);

        exportBtn = createActionButton(Material2OutlinedAL.GET_APP, t(DM.ACTION_EXPORT));
        exportBtn.setOnAction(e -> exportTemplate());
        exportBtn.getStyleClass().add(SMALL);

        importBtn = createActionButton(Material2OutlinedAL.FILE_UPLOAD, t(DM.ACTION_IMPORT));
        importBtn.setOnAction(e -> importTemplate());
        importBtn.getStyleClass().add(SMALL);

        toolBar.getStyleClass().addAll("actions", Styles.TOP);
        toolBar.getItems().setAll(
                addBtn, removeBtn, duplicateBtn, exportBtn, importBtn, new Spacer(), filterText
        );

        leftBox.getChildren().setAll(toolBar, templateList);
        leftBox.setMinWidth(LEFT_BOX_WIDTH);

        // == RIGHT ==

        addLinkBtn = new Hyperlink(t(DM.TPL_ADD_NEW_TEMPLATE));
        addLinkBtn.setOnAction(e -> addTemplate());

        var addLinkBtnText = new Text(t(DM.TPL_OR_SELECT_EXISTING_TEMPLATE_TO_EDIT));
        addLinkBtnText.getStyleClass().addAll(TEXT, TEXT_MUTED);

        placeholder.getChildren().setAll(addLinkBtn, addLinkBtnText);
        placeholder.getStyleClass().add(ExtraStyles.BG_DEFAULT);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setFillWidth(true);
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        content.getChildren().add(placeholder);
        content.setAlignment(Pos.TOP_LEFT);
        BorderPane.setMargin(content, new Insets(0, 0, 0, Recommends.CONTENT_SPACING));

        // == FOOTER ==

        var requiredMark = new Text("*");
        requiredMark.getStyleClass().addAll(Styles.TEXT, DANGER);

        var requiredLabel = new Label("- " + t(DM.REQUIRED_FIELD));
        requiredLabel.setGraphic(requiredMark);

        applyBtn = new Button(t(DM.ACTION_APPLY));
        applyBtn.getStyleClass().addAll(ACCENT);
        applyBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);
        applyBtn.setOnAction(e -> updateTemplate());

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(Recommends.FORM_INLINE_SPACING, requiredLabel, new Spacer(), applyBtn, closeBtn);
        footer.setPadding(new Insets(30, 0, 0, 0));
        footer.setAlignment(Pos.CENTER_LEFT);

        // == ROOT ==

        setLeft(leftBox);
        setCenter(content);
        setBottom(footer);
    }

    protected Button createActionButton(Ikon icon, String tooltipText) {
        var btn = new Button("", new FontIcon(icon));
        btn.getStyleClass().addAll(BUTTON_ICON);
        btn.setTooltip(new Tooltip(tooltipText));
        return btn;
    }

    protected void addTemplate() { }

    protected void removeTemplate() { }

    protected void duplicateTemplate() { }

    protected void exportTemplate() { }

    protected void importTemplate() { }

    protected void updateTemplate() { }

    protected void setCloseHandler(EventHandler<ActionEvent> eventHandler) {
        closeBtn.setOnAction(eventHandler);
    }

    protected void showMessage(Message message) {
        BorderPane.setMargin(message, new Insets(0, 0, Recommends.CONTENT_SPACING, 0));
        setTop(message);
    }

    protected void hideMessage() {
        setTop(null);
    }
}
