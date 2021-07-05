package org.telekit.desktop.views.layout;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TreeItem;
import org.telekit.base.desktop.mvvm.Command;
import org.telekit.base.desktop.mvvm.CommandBase;
import org.telekit.base.desktop.mvvm.ViewModel;
import org.telekit.base.desktop.routing.Route;
import org.telekit.base.desktop.routing.Router;
import org.telekit.base.di.Initializable;
import org.telekit.base.event.Listener;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.ToolGroup;
import org.telekit.base.plugin.internal.ExtensionBox;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.plugin.internal.PluginState;
import org.telekit.base.plugin.internal.PluginStateChangedEvent;
import org.telekit.base.util.CommonUtils;
import org.telekit.desktop.startup.config.Config;
import org.telekit.desktop.views.NavLink;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static org.telekit.base.plugin.internal.PluginState.STARTED;
import static org.telekit.base.plugin.internal.PluginState.STOPPED;
import static org.telekit.base.util.CommonUtils.map;
import static org.telekit.desktop.startup.config.Config.DEFAULT_ROUTE;
import static org.telekit.desktop.views.NavLink.Arg.APP_TITLE;
import static org.telekit.desktop.views.NavLink.Arg.PLUGIN_CLASS;
import static org.telekit.desktop.views.layout.NavDrawerView.HOME_NAV_LINK;

@Singleton
public class NavDrawerViewModel implements Initializable, ViewModel {

    static final Comparator<TreeItem<NavLink>> NAV_TREE_COMPARATOR =
            Comparator.comparing(item -> Objects.requireNonNull(item.getValue()).title());

    private final Router router;
    private final PluginManager pluginManager;

    @Inject
    public NavDrawerViewModel(Router router, PluginManager pluginManager) {
        this.router = router;
        this.pluginManager = pluginManager;
    }

    @Override
    public void initialize() {
        // register routes for initially loaded (and started) plugins
        pluginManager.getExtensionsOfType(Tool.class).stream()
                .map(box -> (Tool<?>) box.getExtension())
                .forEach(tool -> router.registerRoute(tool.id(), tool.getComponent()));

        treeRoot.getChildren().setAll(buildNavigationTree());

        // monitor plugins state to reflect changes on navigation tree
        pluginManager.addEventListener(this::onPluginStateChanged);

        // update selected nav item value if router.navigate() has been called from another model
        router.currentRouteProperty().addListener((obs, old, value) -> verifySelection(value));
    }

    private List<TreeItem<NavLink>> buildNavigationTree() {
        List<Tool<?>> tools = new ArrayList<>(Config.getBuiltinTools());
        Map<String, String> tool2PluginMapping = new HashMap<>();

        for (ExtensionBox extensionBox : pluginManager.getExtensionsOfType(Tool.class)) {
            Tool<?> tool = (Tool<?>) extensionBox.getExtension();
            tools.add(tool);
            tool2PluginMapping.put(tool.id(), extensionBox.getPluginClass().getCanonicalName());
        }

        Map<String, TreeItem<NavLink>> lookup = new HashMap<>();
        Set<TreeItem<NavLink>> groupItems = new HashSet<>();

        for (Tool<?> tool : tools) {
            String toolID = tool.id();

            Map<String, Object> routeArgs = new HashMap<>();
            routeArgs.put(APP_TITLE, tool.getName());
            if (tool2PluginMapping.containsKey(toolID)) {
                routeArgs.put(PLUGIN_CLASS, tool2PluginMapping.get(toolID));
            }

            NavLink link = new NavLink(tool.getName(), new Route(toolID, routeArgs));
            TreeItem<NavLink> treeItem = new TreeItem<>(link);

            if (tool.getGroup() == null) {
                lookup.put(toolID, treeItem);
            } else {
                ToolGroup group = tool.getGroup();
                TreeItem<NavLink> parent = lookup.computeIfAbsent(group.getName(), groupName ->
                        new TreeItem<>(new NavLink(groupName, null))
                );
                parent.setExpanded(group.isExpanded());
                parent.getChildren().add(treeItem);
                groupItems.add(parent);
            }
        }

        // sort groups
        groupItems.forEach(item -> item.getChildren().sort(NAV_TREE_COMPARATOR));

        // collect all tools
        List<TreeItem<NavLink>> treeItems = lookup.values().stream().sorted(NAV_TREE_COMPARATOR).collect(Collectors.toList());

        // prepend home page
        TreeItem<NavLink> homeItem = new TreeItem<>(HOME_NAV_LINK);
        treeItems.add(0, homeItem);

        return treeItems;
    }

    private Optional<NavLink> getSelectedLink() {
        return Optional.ofNullable(CommonUtils.map(selectionModel.get().getSelectedItem(), TreeItem::getValue));
    }

    // if selected nav item doesn't match current route, fix it
    private void verifySelection(Route route) {
        if (route == null || selectionModel.get() == null || getSelectedLink()
                .map(link -> Objects.equals(link.route(), route))
                .orElse(false)) {
            return;
        }

        TreeItem<NavLink> item = findTreeItemByRoute(treeRoot, route);
        if (item != null) { selectionModel.get().select(item); }
    }

    private TreeItem<NavLink> findTreeItemByRoute(TreeItem<NavLink> item, Route route) {
        NavLink link = item.getValue();
        if (link != null && Objects.equals(link.route(), route)) {
            return item;
        } else {
            for (TreeItem<NavLink> child : item.getChildren()) {
                TreeItem<NavLink> e = findTreeItemByRoute(child, route);
                if (e != null) { return e; }
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    //@formatter:off
    private final TreeItem<NavLink> treeRoot = new TreeItem<>();
    public TreeItem<NavLink> treeRoot() { return treeRoot; }

    // selection model implementation is package-private, so we can only obtain it from the view
    private final ObjectProperty<SelectionModel<TreeItem<NavLink>>> selectionModel = new SimpleObjectProperty<>(this, "selectionModel");
    public ObjectProperty<SelectionModel<TreeItem<NavLink>>> selectionModelProperty() { return selectionModel; }
    //@formatter:on

    ///////////////////////////////////////////////////////////////////////////
    // Commands                                                              //
    ///////////////////////////////////////////////////////////////////////////

    private final Command navigateCommand = new CommandBase() {

        @Override
        protected void doExecute() {
            getSelectedLink().ifPresent(link -> {
                if (!Objects.equals(link.route(), router.getCurrentRoute())) {
                    router.navigate(link.route());
                }
            });
        }
    };

    public Command navigateCommand() { return navigateCommand; }

    ///////////////////////////////////////////////////////////////////////////
    // Event Bus                                                             //
    ///////////////////////////////////////////////////////////////////////////

    @Listener
    private void onPluginStateChanged(PluginStateChangedEvent event) {
        PluginState pluginState = event.getPluginState();

        // rebuild navigation tree on plugin start and stop events
        if (pluginState == STARTED || pluginState == STOPPED) {
            treeRoot.getChildren().setAll(buildNavigationTree());
        }

        // register routes for newly started plugins
        if (pluginState == STARTED) {
            pluginManager.getExtensionsOfType(Tool.class).stream()
                    .filter(box -> Objects.equals(box.getPluginClass(), event.getPluginClass()))
                    .map(box -> (Tool<?>) box.getExtension())
                    .forEach(tool -> router.registerRoute(tool.id(), tool.getComponent()));
        }

        if (pluginState == STOPPED) {
            // navigate to the start page if tool provided by the stopped plugin is active
            if (Objects.equals(
                    event.getPluginClass().getCanonicalName(),
                    router.getCurrentRoute().getArg(PLUGIN_CLASS, String.class))
            ) {
                router.navigate(DEFAULT_ROUTE);
            }

            // NOTE: Theoretically, here we should also unregister routes for the tools
            // provided by the stopped plugin. To do so, we have to traverse the
            // whole tree and find items which has corresponding Arg.PLUGIN_CLASS
            // value. Practically, it's such a small overhead. Should not really matter.
        }
    }
}
