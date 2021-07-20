package org.telekit.desktop.service;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public final class IconRepository {

    public static final String FAVICON = "FAVICON";
    public static final Map<String, Image> REPO = new HashMap<>();

    public static Image get(String iconID) {
        return REPO.get(iconID);
    }

    public static void put(String iconID, Image icon) {
        REPO.put(iconID, icon);
    }
}
