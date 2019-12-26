package corf.desktop.tools.httpsender;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import corf.base.common.Lazy;
import corf.base.desktop.controls.FXHelpers;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.common.ui.DescriptionText;
import corf.desktop.tools.common.ui.TitleHelpLabel;

import static atlantafx.base.theme.Styles.TEXT_SUBTLE;
import static corf.base.i18n.I18n.t;

final class TemplateCard extends VBox {

    private static final int MAX_DESCRIPTION_SIZE = 300;

    TitleHelpLabel titleLabel;
    ComboBox<Template> templateChoice;
    Button manageBtn;
    DescriptionText descriptionText;
    HBox descriptionPlaceholder;
    Hyperlink addDescriptionLink;

    private final HttpSenderView view;
    private final HttpSenderViewModel model;
    private final Lazy<TemplateHelpDialog> helpDialog;

    @SuppressWarnings("NullAway.Init")
    public TemplateCard(HttpSenderView view) {
        super();

        this.view = view;
        this.model = view.getViewModel();

        this.helpDialog = new Lazy<>(() -> {
            var dialog = new TemplateHelpDialog();
            dialog.setOnCloseRequest(() -> view.getOverlay().hide());
            return dialog;
        });

        setSpacing(Recommends.CARD_SPACING);
        getChildren().setAll(
                createTitle(),
                createBody()
        );
        init();
    }

    private HBox createTitle() {
        var icon = new FontIcon(Material2OutlinedAL.ARTICLE);

        titleLabel = new TitleHelpLabel(t(DM.TPL_TEMPLATE));

        var title = new HBox(Recommends.CARD_SPACING, icon, titleLabel);
        title.setAlignment(Pos.CENTER_LEFT);

        return title;
    }

    private VBox createBody() {
        templateChoice = new ComboBox<>();
        templateChoice.setButtonCell(new TemplateListCell());
        templateChoice.setCellFactory(property -> new TemplateListCell());
        templateChoice.setPrefWidth(HttpSenderView.LEFT_AREA_WIDTH);

        manageBtn = new Button(t(DM.TPL_MANAGE_TEMPLATES), new FontIcon(Material2OutlinedAL.EDIT));

        descriptionText = new DescriptionText(MAX_DESCRIPTION_SIZE);

        var noDescriptionMessage = new Text(t(DM.TPL_TEMPLATE_HAS_NO_DESCRIPTION));
        noDescriptionMessage.getStyleClass().add(TEXT_SUBTLE);

        addDescriptionLink = new Hyperlink(t(DM.TPL_EDIT_TEMPLATE));

        descriptionPlaceholder = new HBox(10, noDescriptionMessage, addDescriptionLink);
        descriptionPlaceholder.setAlignment(Pos.CENTER_LEFT);
        descriptionPlaceholder.setPadding(new Insets(0, 0, 0, 2));

        var templateBox = new HBox(Recommends.FORM_INLINE_SPACING);
        templateBox.setAlignment(Pos.CENTER_LEFT);
        templateBox.getChildren().addAll(templateChoice, manageBtn);

        return new VBox(10, templateBox, descriptionText, descriptionPlaceholder);
    }

    private void init() {
        titleLabel.setOnHelpButtonClicked(e -> {
            var dialog = helpDialog.get();
            view.getOverlay().show(dialog, Pos.CENTER, Recommends.MODAL_WINDOW_MARGIN);
        });

        model.selectedTemplateProperty().addListener((obs, old, val) -> toggleDescription(val));
        toggleDescription(model.selectedTemplateProperty().get());

        templateChoice.setItems(model.getTemplates());
        templateChoice.valueProperty().bindBidirectional(model.selectedTemplateProperty());

        addDescriptionLink.setOnAction(e -> model.openTemplateManagerCommand().run());
        manageBtn.setOnAction(e -> model.openTemplateManagerCommand().run());
    }

    private void toggleDescription(Template template) {
        var description = template != null ? template.getDescription() : null;
        var hasDescription = StringUtils.isNotBlank(description);
        descriptionText.setText(hasDescription ? description : null);
        FXHelpers.setManaged(descriptionText, hasDescription);
        FXHelpers.setManaged(descriptionPlaceholder, !hasDescription);
    }

    ///////////////////////////////////////////////////////////////////////////

    static class TemplateListCell extends ListCell<Template> {

        @Override
        protected void updateItem(Template template, boolean empty) {
            super.updateItem(template, empty);
            setText(template != null && !empty ? template.getName() : null);
        }
    }
}
