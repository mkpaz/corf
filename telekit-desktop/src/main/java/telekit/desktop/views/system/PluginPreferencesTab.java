package telekit.desktop.views.system;

import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import telekit.base.Env;
import telekit.base.domain.event.Notification;
import telekit.base.event.DefaultEventBus;
import telekit.base.plugin.Metadata;
import telekit.base.plugin.Plugin;
import telekit.base.plugin.internal.PluginBox;
import telekit.base.plugin.internal.PluginState;
import telekit.base.util.DesktopUtils;
import telekit.base.util.TextBuilder;
import telekit.controls.dialogs.Dialogs;
import telekit.controls.util.Containers;
import telekit.controls.util.Controls;
import telekit.controls.util.NodeUtils;
import telekit.desktop.i18n.DesktopMessages;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import static javafx.geometry.Pos.*;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy;
import static javafx.scene.layout.GridPane.REMAINING;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static telekit.base.i18n.I18n.t;
import static telekit.base.plugin.internal.PluginState.*;
import static telekit.base.util.FileSystemUtils.getParentPath;
import static telekit.controls.util.Containers.*;
import static telekit.desktop.i18n.DesktopMessages.*;

public class PluginPreferencesTab extends Tab {

    ListView<PluginBox> pluginList;
    Button installPluginBtn;

    Label nameLabel;
    Label versionLabel;
    HBox authorBox;
    Label authorLabel;
    Hyperlink homepageLink;
    Hyperlink docsLink;
    TextFlow descTextFlow;
    Text descriptionText;
    Text resourcesText;

    MenuButton actionMenuBtn;
    MenuItem toggleActionItem;
    MenuItem uninstallActionItem;

    VBox dummyBox;

    private final PreferencesView view;
    private final PreferencesViewModel model;
    private Path lastVisitedDirectory;

    public PluginPreferencesTab(PreferencesView view, PreferencesViewModel model) {
        this.view = view;
        this.model = model;

        createView();
    }

    private void createView() {
        installPluginBtn = new Button(t(DesktopMessages.ACTION_INSTALL));
        installPluginBtn.setGraphic(Controls.fontIcon(Material2AL.GET_APP));
        installPluginBtn.setOnAction(e -> installPlugin());

        HBox installBox = new HBox();
        installBox.setSpacing(10);
        installBox.setAlignment(CENTER_LEFT);
        installBox.getChildren().addAll(
                installPluginBtn,
                horizontalSpacer()
        );

        pluginList = Controls.create(ListView::new, "plugin-list");
        pluginList.setCellFactory(lv -> new PluginListCell());
        pluginList.setItems(model.getPlugins());

        model.selectedPluginProperty().bind(pluginList.getSelectionModel().selectedItemProperty());
        model.selectedPluginProperty().addListener((obs, old, value) -> updatePluginDetails(value));

        StackPane pluginDetailsPane = createPluginDetailsGrid();

        // GRID

        GridPane grid = gridPane(10, 5, new Insets(10), "grid");

        grid.add(pluginList, 0, 0);
        grid.add(installBox, 0, 1, REMAINING, 1);

        grid.add(pluginDetailsPane, 1, 0);

        grid.getColumnConstraints().setAll(
                Containers.columnConstraints(Priority.NEVER),
                Containers.columnConstraints(Priority.ALWAYS)
        );
        grid.getColumnConstraints().get(0).setPercentWidth(40);

        setText(t(DesktopMessages.PLUGINS));
        setContent(grid);
    }

    private StackPane createPluginDetailsGrid() {
        // ACTION MENU

        toggleActionItem = new MenuItem();
        toggleActionItem.setOnAction(e -> togglePlugin());

        uninstallActionItem = new MenuItem(t(DesktopMessages.ACTION_UNINSTALL));
        uninstallActionItem.setOnAction(e -> uninstallPlugin());

        actionMenuBtn = Controls.menuIconButton(Material2MZ.SETTINGS, "flat-dropdown");
        GridPane.setValignment(actionMenuBtn, VPos.TOP);
        GridPane.setHalignment(actionMenuBtn, HPos.RIGHT);
        actionMenuBtn.visibleProperty().bind(model.selectedPluginProperty().isNotNull());
        actionMenuBtn.setOnShowing(e -> refreshPluginActionMenu());
        actionMenuBtn.getItems().setAll(toggleActionItem, uninstallActionItem);

        // DETAILS

        nameLabel = Controls.create(Label::new, "name");
        nameLabel.setWrapText(true);

        versionLabel = Controls.create(Label::new, "version");
        versionLabel.setPadding(new Insets(0, 10, 0, 0));

        authorLabel = Controls.create(Label::new, "author");
        authorBox = hbox(2, CENTER_LEFT, Insets.EMPTY);
        authorBox.getChildren().setAll(
                Controls.fontIcon(Material2AL.COPYRIGHT),
                authorLabel
        );

        HBox versionAuthorBox = hbox(5, CENTER_LEFT, Insets.EMPTY);
        versionAuthorBox.getChildren().setAll(versionLabel, authorBox);

        homepageLink = new Hyperlink(t(SYSTEM_PROJECT_PAGE));
        homepageLink.setOnAction(e -> DesktopUtils.browseQuietly(URI.create((String) homepageLink.getUserData())));

        docsLink = new Hyperlink(t(DesktopMessages.DOCUMENTATION));
        docsLink.setOnAction(e -> openDocs());

        HBox homeDocsBox = hbox(10, CENTER_LEFT, Insets.EMPTY);
        homeDocsBox.getChildren().setAll(homepageLink, docsLink);

        descriptionText = Controls.create(Text::new);
        descTextFlow = Controls.create(TextFlow::new, "description");
        descTextFlow.getChildren().setAll(descriptionText);

        resourcesText = Controls.create(Text::new, "resources", "monospace");

        VBox descResourcesBox = vbox(10, TOP_LEFT, Insets.EMPTY);
        descResourcesBox.getChildren().setAll(descTextFlow, resourcesText);

        ScrollPane descResourcesScroll = new ScrollPane();
        descResourcesScroll.getStyleClass().add("edge-to-edge");
        setScrollConstraints(descResourcesScroll,
                ScrollBarPolicy.AS_NEEDED, true,
                ScrollBarPolicy.AS_NEEDED, true
        );
        descResourcesScroll.setContent(descResourcesBox);

        // scroll pane have to be able to calculate children height
        descTextFlow.maxWidthProperty().bind(descResourcesScroll.widthProperty().subtract(20));

        // GRID

        GridPane grid = gridPane(10, 5, Insets.EMPTY, "grid");

        grid.add(nameLabel, 0, 0);
        grid.add(actionMenuBtn, 1, 0);

        grid.add(versionAuthorBox, 0, 1, REMAINING, 1);
        grid.add(homeDocsBox, 0, 2, REMAINING, 1);
        grid.add(descResourcesScroll, 0, 3, REMAINING, 1);

        grid.getColumnConstraints().setAll(HGROW_ALWAYS, HGROW_NEVER, HGROW_NEVER);
        grid.getRowConstraints().setAll(VGROW_NEVER, VGROW_NEVER, VGROW_NEVER, VGROW_ALWAYS);

        // ROOT

        dummyBox = vbox(0, CENTER, Insets.EMPTY, "dummy");
        dummyBox.getChildren().setAll(new Label(t(NO_DATA)));

        StackPane stackPane = Containers.create(StackPane::new, "plugin-info");
        stackPane.getChildren().setAll(grid, dummyBox);

        dummyBox.toFront();

        return stackPane;
    }

    private void refreshPluginActionMenu() {
        PluginBox plugin = model.selectedPluginProperty().get();
        if (plugin == null) { return; }

        PluginState state = plugin.getState();
        if (state == STARTED || state == FAILED) {
            // can be disabled when in STARTED of FAILED state
            toggleActionItem.setText(t(ACTION_DISABLE));
            toggleActionItem.setVisible(true);
        } else if (state == STOPPED || state == DISABLED) {
            // can be re-enable when in DISABLED or STOPPED state
            toggleActionItem.setText(t(ACTION_ENABLE));
            toggleActionItem.setVisible(true);
        } else {
            // toggle option is unavailable in LOADED, INSTALLED, UNINSTALLED
            toggleActionItem.setVisible(false);
        }
    }

    private void updatePluginDetails(PluginBox pluginBox) {
        if (pluginBox == null) {
            dummyBox.toFront();
            return;
        } else {
            dummyBox.toBack();
        }

        Plugin plugin = pluginBox.getPlugin();
        Metadata metadata = plugin.getMetadata();

        // MANDATORY FIELDS

        nameLabel.setText(metadata.getName());
        versionLabel.setText(metadata.getVersion());

        // OPTIONAL FIELDS

        authorLabel.setText(metadata.getAuthor());
        NodeUtils.toggleVisibility(authorBox, isNotBlank(metadata.getAuthor()));

        homepageLink.setUserData(metadata.getHomePage());
        NodeUtils.toggleVisibility(homepageLink, isNotBlank(metadata.getHomePage()));

        NodeUtils.toggleVisibility(docsLink, plugin.providesDocs());

        descriptionText.setText(metadata.getDescription());
        NodeUtils.toggleVisibility(descTextFlow, isNotBlank(metadata.getDescription()));

        // PLUGINS RESOURCES

        // don't display resources info for plugins in installation state
        // it's an intermediate state, some resources may reside in temp dirs or may be already deleted
        if (pluginBox.getState() != INSTALLED && pluginBox.getState() != UNINSTALLED) {

            TextBuilder tb = new TextBuilder();
            Path pluginDataDir = Env.getPluginDataDir(pluginBox.getPluginClass());
            Path pluginJarPath = pluginBox.getJarPath();

            if (pluginJarPath != null) {
                tb.appendLine(pluginDataDir.relativize(pluginJarPath).toString());
            }

            for (Path path : pluginBox.getConfigs()) {
                tb.appendLine(pluginDataDir.relativize(path).toString());
            }

            // no need to maintain visibility, there's always at least plugin JAR
            resourcesText.setText(tb.toString());
        }
    }

    private void togglePlugin() {
        PluginBox plugin = model.selectedPluginProperty().get();
        int index = pluginList.getSelectionModel().getSelectedIndex();
        if (plugin == null) { return; }

        model.togglePluginCommand().execute();
        pluginList.getSelectionModel().select(index);
    }

    private void installPlugin() {
        File zipFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_ZIP), "*.zip")
                .initialDirectory(lastVisitedDirectory)
                .build()
                .showOpenDialog(view.getWindow());
        if (zipFile == null) { return; }

        lastVisitedDirectory = getParentPath(zipFile);
        model.installPluginCommand().execute(zipFile.toPath());

        DefaultEventBus.getInstance().publish(Notification.info(t(PLUGIN_MANAGER_MSG_INSTALL_SUCCESS)));
    }

    private void uninstallPlugin() {
        PluginBox plugin = model.selectedPluginProperty().get();
        if (plugin == null) { return; }

        boolean deleteResources = false;
        if (plugin.hasConfigs()) {
            Alert dialog = Dialogs.confirm()
                    .title(t(CONFIRMATION))
                    .content(t(PLUGIN_MANAGER_MSG_UNINSTALL_CONFIRM))
                    .setButtonTypes(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                    .owner(view.getWindow())
                    .build();
            Optional<ButtonType> confirmation = dialog.showAndWait();

            if (confirmation.isEmpty() || confirmation.get().equals(ButtonType.CANCEL)) {
                return;
            }

            deleteResources = confirmation.get().equals(ButtonType.YES);
        }

        model.uninstallPluginCommand().execute(deleteResources);

        DefaultEventBus.getInstance().publish(Notification.info(t(PLUGIN_MANAGER_MSG_UNINSTALL_SUCCESS)));
    }

    private void openDocs() {
        PluginBox plugin = model.selectedPluginProperty().get();
        if (plugin != null && plugin.getPlugin().providesDocs()) {
            plugin.getPlugin().openDocs(Locale.getDefault());
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    static class PluginListCell extends ListCell<PluginBox> {

        private static final PseudoClass DISABLED = PseudoClass.getPseudoClass("disabled");

        @Override
        public void updateItem(PluginBox pluginBox, boolean empty) {
            super.updateItem(pluginBox, empty);

            if (pluginBox == null) {
                setText(null);
            } else {
                Plugin plugin = pluginBox.getPlugin();
                Metadata metadata = plugin.getMetadata();
                setText(metadata.getName());

                pseudoClassStateChanged(DISABLED, pluginBox.getState() != STARTED);
            }
        }
    }
}
