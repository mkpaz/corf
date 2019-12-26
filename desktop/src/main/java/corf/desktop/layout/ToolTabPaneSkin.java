package corf.desktop.layout;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.MouseEvent;

import java.util.Set;

public final class ToolTabPaneSkin extends TabPaneSkin {

    private final EventHandler<MouseEvent> addTabHandler;

    public ToolTabPaneSkin(ToolTabPane tabPane) {
        super(tabPane);

        addTabHandler = e -> {
            if (e.isPrimaryButtonDown()) { tabPane.addTab(); }
            // consuming PRESSED event also prevents dragging the button
            e.consume();
        };

        registerEventHandlers(tabPane);
    }

    void registerEventHandlers(ToolTabPane tabPane) {
        Set<Node> tabContainers = tabPane.lookupAll(".tab-container");
        tabContainers.forEach(c -> {
            Parent parent = c.getParent(); // TabHeaderSkin is package-private
            Tab tab = (Tab) parent.getProperties().get(Tab.class);
            if (tab instanceof ToolTabPane.AddTabButton) {
                parent.removeEventFilter(MouseEvent.MOUSE_PRESSED, addTabHandler);
                parent.addEventFilter(MouseEvent.MOUSE_PRESSED, addTabHandler);
            }
        });
    }
}
