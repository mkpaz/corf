package corf.desktop.layout;

import backbonefx.mvvm.View;
import corf.base.Injector;
import corf.base.desktop.Focusable;
import corf.base.plugin.Tool;
import corf.base.plugin.internal.PluginBox;
import corf.desktop.i18n.DM;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

import java.util.Objects;

import static corf.base.i18n.I18n.t;

public final class ToolTabPane extends TabPane {

    private static final int TAB_MIN_WIDTH = 80;
    private static final int TAB_ICON_SIZE = 16;
    private final AddTabButton addTabButton = new AddTabButton();
    private final ContextMenu contextMenu = new ContextMenu();

    @SuppressWarnings("NullAway.Init")
    public ToolTabPane() {
        super();

        createView();
        init();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ToolTabPaneSkin(this);
    }

    private void createView() {
        getTabs().addAll(addTabButton);
        addTab();

        var closeItem = new MenuItem(t(DM.ACTION_CLOSE));
        closeItem.setOnAction(e -> closeSelectedTab());

        var closeAllItem = new MenuItem(t(DM.ACTION_CLOSE_ALL));
        closeAllItem.setOnAction(e -> closeAllTabs());

        // There's no way to determine the tab that triggered the context menu.
        // If user triggered the context menu on inactivate (not selected) tab
        // and chose this option, everything but _selected_ tab will be closed
        // which may be confusing, but this is intended behaviour and the area
        // for further improvement.
        var closeOthersItem = new MenuItem(t(DM.ACTION_CLOSE_OTHERS));
        closeOthersItem.setOnAction(e -> closeAllTabsExceptSelected());

        contextMenu.getItems().addAll(closeItem, closeAllItem, closeOthersItem);

        setId("tool-tab-pane");
        getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

        // This needs further improvements. In particular, code to keep AddButton
        // at the end of the list is an ugly hack. Overall, TabPane is a complete
        // mess and custom implementation is more relevant in this case.
        setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
    }

    private void init() {
        widthProperty().addListener((obs, old, val) -> updateMaxTabWidth());
        getTabs().addListener((ListChangeListener<Tab>) change -> {
            updateMaxTabWidth();

            while (change.next()) {
                var list = change.getList();

                // there's no option to disable reordering for some buttons,
                // so we have to restore proper tab sorting after drag finished
                if (change.wasPermutated() && list.size() > 1 && list.get(list.size() - 1) != addTabButton) {
                    // FXCollections has no swap method (https://bugs.openjdk.org/browse/JDK-8090975)
                    getTabs().remove(addTabButton);
                    getTabs().add(addTabButton);

                    // restore event handler, because removed tabs "lose its skin"
                    if (getSkin() != null && getSkin() instanceof ToolTabPaneSkin tabPaneSkin) {
                        tabPaneSkin.registerEventHandlers(this);
                    }
                }
                // always keep at least one tab
                else if (list.size() == 1 && list.get(0) == addTabButton) {
                    addTab();
                }
            }
        });
    }

    /** Adds new blank tab. */
    public void addTab() {
        // ugly hack to cope with min tab width which isn't working as expected
        var tab = new Tab(" ".repeat(24), new StartPage());
        tab.setContextMenu(contextMenu);

        var imageView = new ImageView(MainWindowView.BLANK_TAB_ICON);
        imageView.setFitWidth(TAB_ICON_SIZE);
        imageView.setFitHeight(TAB_ICON_SIZE);
        imageView.getStyleClass().add("icon");

        tab.setGraphic(imageView);
        addTab(tab);
    }

    /** Sets content to the currently selected tab. */
    public void setCurrentTabContent(Tool<?> tool) {
        var tab = getSelectionModel().getSelectedItem();
        if (tab == null) { return; }

        Class<? extends View<?, ?>> viewClass = tool.getView();
        View<?, ?> view = Injector.getView(viewClass);

        Image icon = tool.getIcon() != null ? tool.getIcon() : MainWindowView.DEFAULT_TOOL_ICON;
        tab.setText(tool.getName());
        tab.setGraphic(createTabGraphic(icon));
        tab.setUserData(tool);

        var toolRoot = view.getRoot();
        if (!toolRoot.getStyleClass().contains("tool")) {
            toolRoot.getStyleClass().add("tool");
        }

        var wrapper = new StackPane(toolRoot);
        wrapper.getStyleClass().add("tool-wrapper");

        tab.setContent(wrapper);

        if (view instanceof Focusable focusable && focusable.getPrimaryFocusNode() != null) {
            focusable.begForFocus(3);
        }
    }

    public void closeSelectedTab() {
        getTabs().remove(getSelectionModel().getSelectedItem());
    }

    @SuppressWarnings("ShortCircuitBoolean")
    public void closeAllTabs() {
        var firstTab = getTabs().get(0);
        if (getTabs().size() > 2) {
            getTabs().removeIf(tab -> tab != addTabButton & tab != firstTab);
        }

        firstTab.setText(" ".repeat(24));
        firstTab.setContent(new StartPage());
        firstTab.setGraphic(createTabGraphic(MainWindowView.BLANK_TAB_ICON));
    }

    @SuppressWarnings("ShortCircuitBoolean")
    public void closePluginTabs(PluginBox plugin) {
        Objects.requireNonNull(plugin, "plugin");
        getTabs().removeIf(tab -> tab != addTabButton
                & (tab.getUserData() instanceof Tool<?> tool && plugin.providesExtensionImpl(tool.getClass())));

        if (getTabs().isEmpty()) { addTab(); }
    }

    @SuppressWarnings("ShortCircuitBoolean")
    public void closeAllTabsExceptSelected() {
        if (getTabs().size() == 2) { return; } // nothing to close
        getTabs().removeIf(tab -> tab != addTabButton & tab != getSelectionModel().getSelectedItem());
    }

    /** Adds a new tab before the button and selects it. */
    private void addTab(Tab tab) {
        getTabs().add(getTabs().size() - 1, tab);
        getSelectionModel().select(getTabs().size() - 2);
    }

    private ImageView createTabGraphic(Image image) {
        var imageView = new ImageView(image);
        imageView.setFitWidth(TAB_ICON_SIZE);
        imageView.setFitHeight(TAB_ICON_SIZE);
        imageView.getStyleClass().add("icon");
        return imageView;
    }

    // Min tab width can't less than (graphic width + close icon width + tab padding).
    // In other words, only tab label can be shrunk due to label text overrun property.
    // To keep all tabs visible (which is the goal) we have to change close policy to
    // the SELECTED_TAB first, and after it won't be enough - stop adding new tabs.
    // The problem is that we can't determine actual tab width without forking TabPaneSkin,
    // due to marvelous "let's make everything private" JavaFX design.
    private void updateMaxTabWidth() {
        double maxWidth = getWidth() / getTabs().size();
        TabClosingPolicy closePolicy = TabClosingPolicy.ALL_TABS;

        if (maxWidth < TAB_MIN_WIDTH) {
            closePolicy = TabClosingPolicy.SELECTED_TAB;
        }

        if (getTabClosingPolicy() != closePolicy) {
            setTabClosingPolicy(closePolicy);
        }
        setTabMaxWidth(maxWidth);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final class AddTabButton extends Tab {

        public AddTabButton() {
            super("");
            setGraphic(new FontIcon(Material2MZ.PLUS));
            setClosable(false);
            getStyleClass().add("add-tab-button");
        }
    }
}
