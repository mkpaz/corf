package org.telekit.ui.main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import org.telekit.base.collect.SortedTreeNode;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.internal.ExtensionBox;
import org.telekit.base.util.CollectionUtils;
import org.telekit.ui.domain.BuiltinTool;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class ToolsMenuHelper {

    private final SortedTreeNode<String, Tool> root = new SortedTreeNode<>("ROOT");

    public ToolsMenuHelper() {}

    public ToolsMenuHelper(List<Tool> tools) {
        populate(tools);
    }

    public void populate(List<Tool> tools) {
        if (CollectionUtils.isEmpty(tools)) return;
        tools.forEach(this::addItem);
    }

    public List<MenuItem> createTopMenu(final EventHandler<ActionEvent> eventHandler) {
        List<MenuItem> menuItems = new ArrayList<>();

        for (SortedTreeNode<String, Tool> node : root.getChildren()) {
            // no need for recursion, because deeply nested menus is a mess, one level is enough
            if (node.hasChildren()) {
                final Menu menu = new Menu(node.getKey());
                final List<MenuItem> nestedItems = new ArrayList<>();
                node.getChildren().forEach(
                        child -> nestedItems.add(createMenuItem(child.getKey(), child.getValue(), eventHandler))
                );
                menu.getItems().setAll(nestedItems);
                menuItems.add(menu);
                continue;
            }

            menuItems.add(createMenuItem(node.getKey(), node.getValue(), eventHandler));
        }
        return menuItems;
    }

    public List<Labeled> createWelcomeMenu(final EventHandler<ActionEvent> eventHandler) {
        List<Labeled> menuItems = new ArrayList<>();

        for (SortedTreeNode<String, Tool> node : root.getChildren()) {
            // no need for recursion, because deeply nested menus is a mess, one level is enough
            if (node.hasChildren()) {
                Label label = new Label(node.getKey());
                label.getStyleClass().add("caption");
                menuItems.add(label);
                node.getChildren().forEach(child -> {
                    Hyperlink link = createLink(child.getKey(), child.getValue(), eventHandler);
                    link.getStyleClass().add("nested");
                    menuItems.add(link);
                });

                continue;
            }

            menuItems.add(createLink(node.getKey(), node.getValue(), eventHandler));
        }
        return menuItems;
    }

    public void addItem(Tool tool) {
        SortedTreeNode<String, Tool> parent = root;
        if (isNotBlank(tool.getGroupName())) {
            String groupName = tool.getGroupName();
            parent = root.computeIfAbsent(groupName, k -> new SortedTreeNode<>(groupName));
        }
        parent.put(new SortedTreeNode<>(tool.getName(), tool));
    }

    protected MenuItem createMenuItem(String text, Object userData, EventHandler<ActionEvent> eventHandler) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setUserData(userData);
        menuItem.setOnAction(eventHandler);
        return menuItem;
    }

    protected Hyperlink createLink(String text, Object userData, EventHandler<ActionEvent> eventHandler) {
        Hyperlink hyperlink = new Hyperlink(text);
        hyperlink.setUserData(userData);
        hyperlink.setOnAction(eventHandler);
        return hyperlink;
    }

    public static ToolsMenuHelper createForBuiltinTools() {
        return new ToolsMenuHelper(Arrays.asList(BuiltinTool.values()));
    }

    public static ToolsMenuHelper createForExtensions(Collection<ExtensionBox> extensionBoxes) {
        Objects.requireNonNull(extensionBoxes);
        ToolsMenuHelper helper = new ToolsMenuHelper();
        extensionBoxes.stream()
                .map(tool -> (Tool) tool.getExtension())
                .forEach(helper::addItem);
        return helper;
    }
}
