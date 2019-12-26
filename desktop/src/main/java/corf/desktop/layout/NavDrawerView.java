package corf.desktop.layout;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Tweaks;
import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.desktop.Focusable;
import corf.base.desktop.controls.FXHelpers;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.plugin.Tool;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;

import static atlantafx.base.theme.Styles.TEXT_SUBTLE;
import static javafx.scene.layout.Priority.ALWAYS;
import static corf.base.i18n.I18n.t;

@Singleton
public final class NavDrawerView extends VBox
        implements View<NavDrawerView, NavDrawerViewModel>, Initializable, Focusable {

    static final double DRAWER_WIDTH = 400;
    static final int ICON_SIZE = 24;

    CustomTextField filterText;
    ListView<Tool<?>> navList;

    private final NavDrawerViewModel model;

    @Inject
    public NavDrawerView(NavDrawerViewModel model) {
        super();

        this.model = model;
        createView();
    }

    private void createView() {
        filterText = new CustomTextField();
        filterText.setLeft(new FontIcon(Material2OutlinedMZ.SEARCH));
        filterText.setPromptText(t(DM.SEARCH));
        HBox.setHgrow(filterText, ALWAYS);

        var searchBox = new HBox(filterText);
        searchBox.getStyleClass().add("search");

        navList = new ListView<>();
        navList.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        navList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        navList.setCellFactory(c -> new NavListCell());
        VBox.setVgrow(navList, ALWAYS);

        getChildren().setAll(searchBox, navList);
        setMinWidth(DRAWER_WIDTH);
        setMaxWidth(DRAWER_WIDTH);
        setId("navigation-drawer");
    }

    @Override
    public void init() {
        navList.setItems(model.navMenu());
        navList.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                navigate();
            } else {
                e.consume();
            }
        });
        navList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { navigate(); }

            if (e.getCode() == KeyCode.ESCAPE) {
                Events.fire(new ActionEvent<>(EventID.APP_HIDE_NAVIGATION));
            }

            // switch to filter and clear it
            if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) {
                filterText.setText(null);
                filterText.requestFocus();
            }

            // switch to the filter on typing
            if (e.getCode().isLetterKey() || e.getCode().isDigitKey()) {
                filterText.setText(e.getCode().getChar());
                filterText.requestFocus();
            }

            e.consume();
        });
        // add loop navigation for the list view
        navList.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            var items = navList.getItems();
            var selModel = navList.getSelectionModel();

            // DO NOT CONSUME other events
            if (e.getCode() == KeyCode.DOWN && items.size() > 1
                    && selModel.getSelectedItem() == items.get(items.size() - 1)) {
                selModel.selectFirst();
                e.consume();
            }
            if (e.getCode() == KeyCode.UP
                    && items.size() > 1
                    && selModel.getSelectedItem() == items.get(0)) {
                selModel.selectLast();
                e.consume();
            }
        });

        // preserve nav menu selection
        model.navMenu().addListener((ListChangeListener<Tool<?>>) c -> {
            if (!model.navMenu().isEmpty() && navList.getSelectionModel().isEmpty()) {
                navList.getSelectionModel().selectFirst();
            }
        });

        filterText.textProperty().bindBidirectional(model.filterProperty());

        filterText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                Events.fire(new ActionEvent<>(EventID.APP_HIDE_NAVIGATION));
                e.consume();
                return;
            }

            if (model.navMenu().isEmpty()) {
                e.consume();
                return;
            }

            // move focus from filter to nav menu
            if (e.getCode() == KeyCode.DOWN) {
                if (model.navMenu().size() > 1
                        && navList.getSelectionModel().getSelectedItem() == model.navMenu().get(0)) {
                    navList.getSelectionModel().selectNext();
                } else {
                    navList.getSelectionModel().selectFirst();
                }
                navList.requestFocus();
            }
            if (e.getCode() == KeyCode.UP) {
                if (model.navMenu().size() > 1
                        && navList.getSelectionModel().getSelectedItem() == model.navMenu().get(0)) {
                    navList.getSelectionModel().selectLast();
                } else {
                    navList.getSelectionModel().selectFirst();
                }
                navList.requestFocus();
            }

            // allow navigating to the first selected item when focus is in filter field
            if (e.getCode() == KeyCode.ENTER) { navigate(); }

            e.consume();
        });
    }

    @Override
    public NavDrawerView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public NavDrawerViewModel getViewModel() {
        return model;
    }

    @Override
    public Node getPrimaryFocusNode() {
        return navList;
    }

    private void navigate() {
        Tool<?> selectedItem = navList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            var event = new ActionEvent<Tool<?>>(EventID.TOOL_OPEN_IN_CURRENT_TAB, null, selectedItem);
            Events.fire(event);
        }
    }

    void prepareToOpen() {
        filterText.setText(null);
        navList.getSelectionModel().selectFirst();
        FXHelpers.begForFocus(filterText, 3);
    }

    ///////////////////////////////////////////////////////////////////////////

    static class NavListCell extends ListCell<Tool<?>> {

        final ImageView imageView;
        final HBox root;
        final Label titleLabel;
        final Label subTitleLabel;

        public NavListCell() {
            root = new HBox();
            root.getStyleClass().add("item");
            root.setAlignment(Pos.CENTER_LEFT);

            imageView = new ImageView();
            imageView.setFitHeight(ICON_SIZE);
            imageView.setFitWidth(ICON_SIZE);

            titleLabel = new Label();
            HBox.setHgrow(titleLabel, ALWAYS);
            titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            titleLabel.getStyleClass().add("title");

            subTitleLabel = new Label();
            HBox.setHgrow(subTitleLabel, ALWAYS);
            subTitleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            subTitleLabel.getStyleClass().add(TEXT_SUBTLE);
            subTitleLabel.getStyleClass().add("subtitle");

            var titleBox = new VBox(titleLabel, subTitleLabel);
            titleBox.setAlignment(Pos.CENTER_LEFT);

            root.getChildren().setAll(imageView, titleBox);
        }

        @Override
        protected void updateItem(Tool<?> tool, boolean empty) {
            super.updateItem(tool, empty);

            if (empty || tool == null) {
                titleLabel.setText(null);
                subTitleLabel.setText(null);
                imageView.setImage(null);
                setGraphic(null);
                return;
            }

            String group = tool.getGroup() != null ? tool.getGroup().getName() : null;
            Image icon = tool.getIcon() != null ? tool.getIcon() : MainWindowView.DEFAULT_TOOL_ICON;

            imageView.setImage(icon);
            titleLabel.setText(tool.getName());
            subTitleLabel.setText(group);
            FXHelpers.setManaged(subTitleLabel, group != null && !group.isBlank());
            setGraphic(root);
        }
    }
}
