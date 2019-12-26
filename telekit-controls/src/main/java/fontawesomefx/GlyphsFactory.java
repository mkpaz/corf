/*
 * Copyright (c) 2013-2016 Jens Deters http://www.jensd.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fontawesomefx;

import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.lang.reflect.Field;

/**
 * @author Jens Deters
 */
public class GlyphsFactory {

    /**
     * @param clazz The IconView class containing the TTF_PATH field pointing to the TTF file
     */
    public GlyphsFactory(Class clazz) {
        loadFont(getPathToFontFromClass(clazz));
    }

    /**
     * @param pathToIconFont The TTF_PATH as String pointing to the TTF file
     */
    public GlyphsFactory(String pathToIconFont) {
        loadFont(pathToIconFont);
    }

    private String getPathToFontFromClass(Class clazz) {
        String path = "";
        try {
            Field ttfPath = clazz.getField("TTF_PATH");
            path = (String) ttfPath.get(null);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return path;
    }

    private final void loadFont(String pathToIconFont) {
        try {
            Font.loadFont(GlyphsFactory.class
                    .getResource(pathToIconFont).openStream(), 10.0);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Text createIcon(GlyphIcons icon) {
        return createIcon(icon, GlyphIcon.DEFAULT_FONT_SIZE);
    }

    public Text createIcon(GlyphIcons icon, String iconSize) {
        Text text = new Text(icon.unicode());
        text.getStyleClass().add("glyph-icon");
        text.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %s;", icon.fontFamily(), iconSize));
        return text;
    }

    public Label createIconLabel(GlyphIcons icon, String text, String iconSize, String fontSize, ContentDisplay contentDisplay) {
        Text iconLabel = createIcon(icon, iconSize);
        Label label = new Label(text);
        label.setStyle("-fx-font-size: " + fontSize);
        label.setGraphic(iconLabel);
        label.setContentDisplay(contentDisplay);
        return label;
    }

    public Button createIconButton(GlyphIcons icon) {
        return createIconButton(icon, "");
    }

    public Button createIconButton(GlyphIcons icon, String text) {
        Text label = createIcon(icon, GlyphIcon.DEFAULT_FONT_SIZE);
        Button button = new Button(text);
        button.setGraphic(label);
        return button;
    }

    public Button createIconButton(GlyphIcons icon, String text, String iconSize, String fontSize, ContentDisplay contentDisplay) {
        Text label = createIcon(icon, iconSize);
        Button button = new Button(text);
        button.setStyle("-fx-font-size: " + fontSize);
        button.setGraphic(label);
        button.setContentDisplay(contentDisplay);
        return button;
    }

    public ToggleButton createIconToggleButton(GlyphIcons icon) {
        return createIconToggleButton(icon, "");
    }

    public ToggleButton createIconToggleButton(GlyphIcons icon, String text) {
        return createIconToggleButton(icon, text, GlyphIcon.DEFAULT_FONT_SIZE);
    }

    public ToggleButton createIconToggleButton(GlyphIcons icon, String text, String iconSize) {
        Text label = createIcon(icon, iconSize);
        ToggleButton button = new ToggleButton(text);
        button.setGraphic(label);
        return button;
    }

    public ToggleButton createIconToggleButton(GlyphIcons icon, String text, String iconSize, ContentDisplay contentDisplay) {
        return createIconToggleButton(icon, text, iconSize, GlyphIcon.DEFAULT_FONT_SIZE, contentDisplay);
    }

    public ToggleButton createIconToggleButton(GlyphIcons icon, String text, String iconSize, String fontSize, ContentDisplay contentDisplay) {
        Text label = createIcon(icon, iconSize);
        ToggleButton button = new ToggleButton(text);
        button.setStyle("-fx-font-size: " + fontSize);
        button.setGraphic(label);
        button.setContentDisplay(contentDisplay);
        return button;
    }

    public void setIcon(Tab tab, GlyphIcons icon) {
        setIcon(tab, icon, GlyphIcon.DEFAULT_FONT_SIZE);
    }

    public void setIcon(Tab tab, GlyphIcons icon, String iconSize) {
        tab.setGraphic(createIcon(icon, iconSize));
    }

    public void setIcon(Labeled labeled, GlyphIcons icon) {
        setIcon(labeled, icon, GlyphIcon.DEFAULT_FONT_SIZE);
    }

    public void setIcon(Labeled labeled, GlyphIcons icon, ContentDisplay contentDisplay) {
        setIcon(labeled, icon, GlyphIcon.DEFAULT_FONT_SIZE, contentDisplay);
    }

    public void setIcon(Labeled labeled, GlyphIcons icon, String iconSize) {
        setIcon(labeled, icon, iconSize, ContentDisplay.LEFT);
    }

    public void setIcon(Labeled labeled, GlyphIcons icon, String iconSize, ContentDisplay contentDisplay) {
        if (labeled == null) {
            throw new IllegalArgumentException("The component must not be 'null'!");
        }
        labeled.setGraphic(createIcon(icon, iconSize));
        labeled.setContentDisplay(contentDisplay);
    }

    public void setIcon(MenuItem menuItem, GlyphIcons icon) {
        setIcon(menuItem, icon, GlyphIcon.DEFAULT_FONT_SIZE, GlyphIcon.DEFAULT_FONT_SIZE);
    }

    public void setIcon(MenuItem menuItem, GlyphIcons icon, String iconSize) {
        setIcon(menuItem, icon, GlyphIcon.DEFAULT_FONT_SIZE, iconSize);
    }

    public void setIcon(MenuItem menuItem, GlyphIcons icon, String fontSize, String iconSize) {
        if (menuItem == null) {
            throw new IllegalArgumentException("The menu item must not be 'null'!");
        }
        Text label = createIcon(icon, iconSize);
        menuItem.setStyle("-fx-font-size: " + fontSize);
        menuItem.setGraphic(label);
    }

    public void setIcon(TreeItem treeItem, GlyphIcons icon) {
        setIcon(treeItem, icon, GlyphIcon.DEFAULT_FONT_SIZE);
    }

    public void setIcon(TreeItem treeItem, GlyphIcons icon, String iconSize) {
        if (treeItem == null) {
            throw new IllegalArgumentException("The tree item must not be 'null'!");
        }
        Text label = createIcon(icon, iconSize);
        treeItem.setGraphic(label);
    }
}
