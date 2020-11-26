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
package org.telekit.controls.glyphs;

import javafx.scene.text.Font;

import java.io.IOException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FontAwesomeIcon extends GlyphIcon<FontAwesome> {

    public static final String TTF_PATH = "/assets/glyphs/fontawesome4-webfont.ttf";
    public static final Comparator<FontAwesome> NAME_COMPARATOR = (o1, o2) -> {
        if (o1 != null && o2 != null) {
            return o1.name().compareTo(o2.name());
        }
        return 0;
    };

    static {
        try {
            Font.loadFont(FontAwesomeIcon.class.getResource(TTF_PATH).openStream(), 10.0d);
        } catch (IOException e) {
            Logger.getLogger(FontAwesomeIcon.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public FontAwesomeIcon(FontAwesome icon, String iconSize) {
        super(FontAwesome.class);
        setIcon(icon);
        setStyle(String.format("-fx-font-family: %s; -fx-font-size: %s;", icon.fontFamily(), iconSize));
    }

    public FontAwesomeIcon(FontAwesome icon) {
        this(icon, "1em");
    }

    public FontAwesomeIcon() {
        this(FontAwesome.ANCHOR);
    }

    @Override
    public FontAwesome getDefaultGlyph() {
        return FontAwesome.ANCHOR;
    }
}
