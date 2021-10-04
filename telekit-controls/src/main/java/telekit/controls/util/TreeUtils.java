package telekit.controls.util;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class TreeUtils {

    public static <T> List<T> getAllItems(TreeItem<T> item) {
        ArrayList<T> accumulator = new ArrayList<>();
        getAllItems(item, accumulator);
        return accumulator;
    }

    private static <T> void getAllItems(TreeItem<T> item, List<T> accumulator) {
        T value = item.getValue();
        if (value != null) { accumulator.add(value); }

        if (item.getChildren().size() > 0) {
            for (TreeItem<T> subItem : item.getChildren()) {
                getAllItems(subItem, accumulator);
            }
        }
    }
}
