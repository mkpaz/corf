package corf.desktop.layout.preferences;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import corf.base.Env;
import corf.base.desktop.Dialogs;
import corf.base.desktop.controls.ModalDialog;
import corf.base.plugin.Metadata;
import corf.base.plugin.Plugin;
import corf.base.plugin.internal.PluginBox;
import corf.base.plugin.internal.PluginState;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.startup.Config;

import java.io.File;
import java.util.Objects;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

@Singleton
public class PluginDialogView extends ModalDialog
        implements View<PluginDialogView, PluginDialogViewModel>, Initializable {

    static final Image DEFAULT_PLUGIN_ICON = new Image(Objects.requireNonNull(
            Config.DESKTOP_MODULE.concat("assets/icons/plugin_64.png").getResourceAsStream()
    ));

    private static final int DIALOG_WIDTH = 800;

    HBox mainLayer;
    StackPane placeholderLayer;
    ListView<PluginBox> pluginList;
    PluginDetailsPane detailsPane;
    IntegerBinding pluginCountBinding;

    Button installBtn;
    Button closeBtn;

    private final PluginDialogViewModel model;

    @Inject
    @SuppressWarnings("NullAway.Init")
    public PluginDialogView(PluginDialogViewModel model) {
        super();

        this.model = model;

        setContent(createContent());
        setId("plugin-dialog");
    }

    private Content createContent() {
        mainLayer = createMainLayer();
        placeholderLayer = createPlaceholderLayer();

        var body = new StackPane(mainLayer, placeholderLayer);
        body.setMinHeight(500); // mandatory as it sets PluginDetailsPane scroll pane height
        body.setMaxHeight(500);
        body.setPrefWidth(DIALOG_WIDTH);

        // == FOOTER ==

        installBtn = new Button(t(DM.ACTION_INSTALL), new FontIcon(Material2MZ.PLUS));
        installBtn.getStyleClass().add(Styles.SUCCESS);

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(installBtn, new Spacer(), closeBtn);

        return Content.create(t(DM.PLUGINS), body, footer);
    }

    @Override
    public PluginDialogView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public PluginDialogViewModel getViewModel() {
        return model;
    }

    @Override
    public void init() {
        pluginCountBinding = Bindings.size(model.getPlugins());
        pluginCountBinding.addListener((obs, old, val) -> togglePlaceholder());

        // order matters, listeners must be added first to perform initial update
        model.selectedPluginProperty().addListener((obs, old, val) -> detailsPane.update(val));

        // ugly reference, because JavaFX selection model is package-private
        // order matters, selected template listener must be added first
        model.setSelectionModel(pluginList.getSelectionModel());
        pluginList.setItems(model.getPlugins());

        installBtn.setOnAction(e -> installPlugin());
        closeBtn.setOnAction(e -> close());

        togglePlaceholder();
    }

    private HBox createMainLayer() {
        pluginList = new ListView<>();
        pluginList.getStyleClass().add("plugin-list");
        pluginList.setCellFactory(lv -> new PluginListCell());
        pluginList.setMinWidth(300); // mandatory as it limits PluginDetailsPane scroll pane width
        pluginList.setMaxWidth(300);

        detailsPane = new PluginDetailsPane(model);
        detailsPane.setFillWidth(true);
        HBox.setHgrow(detailsPane, Priority.ALWAYS);

        var layer = new HBox(pluginList, detailsPane);
        layer.setSpacing(Recommends.CONTENT_SPACING);
        layer.setAlignment(Pos.CENTER_LEFT);
        layer.getStyleClass().add(ExtraStyles.BG_DEFAULT);

        return layer;
    }

    private StackPane createPlaceholderLayer() {
        var text = new Text("There is no plugins installed yet.");
        text.getStyleClass().addAll(TITLE_4);

        var layer = new StackPane(text);
        layer.setAlignment(Pos.CENTER);
        layer.getStyleClass().add(ExtraStyles.BG_DEFAULT);

        return layer;
    }

    private void installPlugin() {
        File zipFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_ZIP), "*.zip")
                .initialDirectory(Env.getLastVisitedDir())
                .build()
                .showOpenDialog(getWindow());
        if (zipFile == null) { return; }

        model.installPluginCommand().execute(zipFile.toPath());
    }

    private void togglePlaceholder() {
        if (model.getPlugins().isEmpty()) {
            placeholderLayer.toFront();
        } else {
            mainLayer.toFront();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class PluginListCell extends ListCell<PluginBox> {

        private static final PseudoClass DISABLED = PseudoClass.getPseudoClass("disabled");

        private final HBox root;
        private final ImageView image;
        private final Label nameLabel;
        private final Label versionLabel;

        public PluginListCell() {
            super();

            image = new ImageView();
            image.setFitHeight(24);
            image.setFitWidth(24);

            nameLabel = new Label();
            nameLabel.getStyleClass().add(TEXT_BOLD);
            nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

            versionLabel = new Label();
            versionLabel.getStyleClass().add(TEXT_MUTED);

            var nameVersionBox = new VBox(Recommends.SUB_ITEM_MARGIN, nameLabel, versionLabel);
            nameVersionBox.setAlignment(Pos.CENTER_LEFT);

            root = new HBox(10, image, nameVersionBox);
            root.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        public void updateItem(@Nullable PluginBox pluginBox, boolean empty) {
            super.updateItem(pluginBox, empty);

            if (pluginBox == null || empty) {
                image.setImage(null);
                nameLabel.setText(null);
                versionLabel.setText(null);
                setGraphic(null);
            } else {
                Plugin plugin = pluginBox.getPlugin();
                Metadata metadata = plugin.getMetadata();

                image.setImage(plugin.getIcon() != null ? plugin.getIcon() : DEFAULT_PLUGIN_ICON);
                nameLabel.setText(metadata.getName());
                versionLabel.setText(metadata.getVersion());
                setGraphic(root);

                pseudoClassStateChanged(DISABLED, pluginBox.getState() != PluginState.STARTED);
            }
        }
    }
}
