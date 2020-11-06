package org.telekit.base.ui;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public final class IconCache {

    public static final String ICON_APP = "ICON_APP";

    public static final Map<String, Image> CACHE = new HashMap<>();

    public static Image get(String iconID) {
        return CACHE.get(iconID);
    }

    public static void put(String iconID, Image icon) {
        CACHE.put(iconID, icon);
    }
}
