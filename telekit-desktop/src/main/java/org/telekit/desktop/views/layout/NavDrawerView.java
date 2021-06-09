package org.telekit.desktop.views.layout;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.desktop.i18n.DesktopMessages;
import org.telekit.desktop.views.NavLink;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.telekit.base.i18n.I18n.t;
import static org.telekit.desktop.startup.config.Config.DEFAULT_ROUTE;

@Singleton
public class NavDrawerView extends VBox implements Initializable, View<NavDrawerViewModel> {

    static final PseudoClass VSCROLL = PseudoClass.getPseudoClass("vscroll");
    static final PseudoClass LEAF = PseudoClass.getPseudoClass("leaf");
    static final NavLink HOME_NAV_LINK = new NavLink(t(DesktopMessages.SYSTEM_HOME), DEFAULT_ROUTE);
    static final double DRAWER_WIDTH = 300;

    TreeView<NavLink> navigationTree;

    private final NavDrawerViewModel model;

    @Inject
    public NavDrawerView(NavDrawerViewModel model) {
        this.model = model;

        createView();
    }

    private void createView() {
        navigationTree = new TreeView<>() {
            @Override
            protected Skin<?> createDefaultSkin() {
                return new CustomTreeViewSkin<>(this);
            }
        };
        navigationTree.getStyleClass().add("navigation-tree");
        navigationTree.setShowRoot(false);
        navigationTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        navigationTree.setCellFactory(new NavLinkCellFactory(model));
        VBox.setVgrow(navigationTree, Priority.ALWAYS);

        getChildren().add(navigationTree);
        Containers.setFixedWidth(this, DRAWER_WIDTH);
        setId("navigation-drawer");
    }

    @Override
    public void initialize() {
        model.selectionModelProperty().bind(navigationTree.selectionModelProperty());
        navigationTree.setRoot(model.treeRoot());
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public NavDrawerViewModel getViewModel() { return model; }

    ///////////////////////////////////////////////////////////////////////////

    static class NavLinkCellFactory implements Callback<TreeView<NavLink>, TreeCell<NavLink>> {

        private final NavDrawerViewModel model;

        public NavLinkCellFactory(NavDrawerViewModel model) {
            this.model = model;
        }

        @Override
        public TreeCell<NavLink> call(TreeView<NavLink> listView) {
            NavLinkCell cell = new NavLinkCell();
            cell.setOnMouseClicked(e -> model.navigateCommand().execute());
            return cell;
        }
    }

    static class NavLinkCell extends TreeCell<NavLink> {

        final HBox root;
        final Label titleLabel;

        public NavLinkCell() {
            root = Containers.create(HBox::new, "nav-link");
            root.setAlignment(Pos.CENTER_LEFT);

            titleLabel = Controls.create(Label::new, "title");
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

            root.getChildren().setAll(titleLabel);
        }

        protected void updateItem(NavLink link, boolean empty) {
            super.updateItem(link, empty);

            if (empty) {
                titleLabel.setText(null);
                setGraphic(null);
                return;
            }

            if (link != null) {
                titleLabel.setText(link.title());
            }

            pseudoClassStateChanged(LEAF, getTreeItem().isLeaf());

            setGraphic(root);
        }
    }

    @SuppressWarnings("rawtypes")
    static class CustomVirtualFlow<I extends IndexedCell> extends VirtualFlow<I> {

        public CustomVirtualFlow() {
            super();
            getVbar().visibleProperty().addListener((obs, old, value) -> pseudoClassStateChanged(VSCROLL, value));
        }
    }

    static class CustomTreeViewSkin<T> extends TreeViewSkin<T> {

        public CustomTreeViewSkin(TreeView<T> control) {
            super(control);
        }

        @Override
        protected VirtualFlow<TreeCell<T>> createVirtualFlow() {
            return new CustomVirtualFlow<>();
        }
    }
}
