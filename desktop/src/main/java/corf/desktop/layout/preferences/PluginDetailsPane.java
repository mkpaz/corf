package corf.desktop.layout.preferences;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import corf.base.desktop.controls.FXHelpers;
import corf.base.event.BrowseEvent;
import corf.base.event.Events;
import corf.base.plugin.Metadata;
import corf.base.plugin.Plugin;
import corf.base.plugin.internal.PluginBox;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.net.URI;

import static atlantafx.base.theme.Styles.TEXT_BOLD;
import static atlantafx.base.theme.Styles.TEXT_MUTED;
import static corf.base.i18n.I18n.t;

class PluginDetailsPane extends VBox {

    ImageView image;
    Label nameLabel;
    MenuButton actionMenuBtn;
    MenuItem toggleActionItem;
    MenuItem uninstallActionItem;
    MenuItem purgeActionItem;
    Label versionLabel;
    Label authorLabel;
    Label statusLabel;
    Hyperlink homepageLink;
    Text descriptionText;
    TextFlow descriptionTextFlow;
    ScrollPane scrollPane;

    private final PluginDialogViewModel model;

    public PluginDetailsPane(PluginDialogViewModel model) {
        super();

        this.model = model;

        createView();
        init();
    }

    private void createView() {
        // == HEADER ==

        image = new ImageView();
        image.setFitHeight(48);
        image.setFitWidth(48);

        var imageBox = new StackPane(image);
        imageBox.setPadding(new Insets(10));

        nameLabel = new Label();
        nameLabel.getStyleClass().add(TEXT_BOLD);
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        toggleActionItem = new MenuItem(); // name depends on whether plugin enabled or not
        uninstallActionItem = new MenuItem(t(DM.ACTION_UNINSTALL));
        purgeActionItem = new MenuItem(t(DM.ACTION_PURGE));

        actionMenuBtn = new MenuButton();
        actionMenuBtn.setGraphic(new FontIcon(Material2OutlinedMZ.SETTINGS));
        actionMenuBtn.getStyleClass().addAll(Styles.FLAT, Tweaks.NO_ARROW);
        actionMenuBtn.getItems().setAll(
                toggleActionItem,
                new SeparatorMenuItem(),
                uninstallActionItem,
                purgeActionItem
        );

        var nameBox = new HBox(nameLabel, new Spacer(), actionMenuBtn);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        versionLabel = new Label();
        versionLabel.getStyleClass().add(TEXT_MUTED);

        authorLabel = new Label();

        var versionAuthorBox = new HBox(10, authorLabel, versionLabel);
        versionAuthorBox.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label();
        statusLabel.getStyleClass().add(TEXT_MUTED);

        homepageLink = new Hyperlink(t(DM.PROJECT_PAGE));

        var headerRightBox = new VBox(10, nameBox, versionAuthorBox, statusLabel, homepageLink);
        HBox.setHgrow(headerRightBox, Priority.ALWAYS);

        var headerBox = new HBox(10, imageBox, headerRightBox);
        headerBox.setAlignment(Pos.TOP_LEFT);

        // == DESCRIPTION ==

        descriptionText = new Text();

        descriptionTextFlow = new TextFlow();
        descriptionTextFlow.getChildren().setAll(descriptionText);

        scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(descriptionTextFlow);
        scrollPane.setMaxHeight(400);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // == ROOT ==

        setSpacing(Recommends.CONTENT_SPACING);
        getChildren().setAll(headerBox, scrollPane);
    }

    private void init() {
        // scroll pane have to be able to calculate children height
        //descriptionTextFlow.maxWidthProperty().bind(scrollPane.widthProperty().subtract(20));

        actionMenuBtn.setOnShowing(e -> refreshPluginActionMenu());
        toggleActionItem.setOnAction(e -> togglePlugin());
        uninstallActionItem.setOnAction(e -> uninstallPlugin(false));
        purgeActionItem.setOnAction(e -> uninstallPlugin(true));

        homepageLink.setOnAction(e -> Events.fire(new BrowseEvent(
                URI.create((String) homepageLink.getUserData()))
        ));

        FXHelpers.setManaged(this, false);
    }

    public void update(@Nullable PluginBox pluginBox) {
        if (pluginBox != null) {
            Plugin plugin = pluginBox.getPlugin();
            Metadata metadata = plugin.getMetadata();

            // MANDATORY FIELDS

            image.setImage(plugin.getIcon() != null ? plugin.getIcon() : PluginDialogView.DEFAULT_PLUGIN_ICON);

            nameLabel.setText(metadata.getName());
            FXHelpers.setManaged(nameLabel, StringUtils.isNotBlank(metadata.getName()));

            versionLabel.setText(metadata.getVersion());
            FXHelpers.setManaged(versionLabel, StringUtils.isNotBlank(metadata.getVersion()));

            authorLabel.setText(metadata.getAuthor());
            FXHelpers.setManaged(authorLabel, StringUtils.isNotBlank(metadata.getAuthor()));

            statusLabel.setText(pluginBox.getState().toString());

            homepageLink.setUserData(metadata.getHomePage());
            FXHelpers.setManaged(homepageLink, StringUtils.isNotBlank(metadata.getHomePage()));

            descriptionText.setText(metadata.getDescription());
            FXHelpers.setManaged(descriptionTextFlow, StringUtils.isNotBlank(metadata.getDescription()));
        } else {
            nameLabel.setText(null);
            versionLabel.setText(null);
            authorLabel.setText(null);
            statusLabel.setText(null);
            homepageLink.setUserData(null);
            descriptionText.setText(null);
        }

        FXHelpers.setManaged(this, pluginBox != null);
    }

    private void refreshPluginActionMenu() {
        PluginBox plugin = model.selectedPluginProperty().get();
        if (plugin == null) { return; }

        switch (plugin.getState()) {
            case STARTED, FAILED -> {
                // can be disabled when in STARTED create FAILED state
                toggleActionItem.setText(t(DM.ACTION_DISABLE));
                toggleActionItem.setDisable(false);
            }
            case STOPPED, DISABLED -> {
                // can be re-enabled when in DISABLED or STOPPED state
                toggleActionItem.setText(t(DM.ACTION_ENABLE));
                toggleActionItem.setDisable(false);
            }
            default -> {
                toggleActionItem.setText(t(DM.ACTION_ENABLE));
                toggleActionItem.setDisable(true);
            }
        }
    }

    private void togglePlugin() {
        model.togglePluginCommand().run();
    }

    private void uninstallPlugin(boolean deleteResources) {
        model.uninstallPluginCommand().execute(deleteResources);
    }
}
