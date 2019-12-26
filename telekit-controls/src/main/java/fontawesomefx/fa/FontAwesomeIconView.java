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
package fontawesomefx.fa;

import fontawesomefx.GlyphIcon;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jens Deters
 */
public class FontAwesomeIconView extends GlyphIcon<FontAwesomeIcon> {

    public final static String TTF_PATH = "/assets/glyphs/fontawesome/fontawesome-webfont.ttf";

    static {
        try {
            Font.loadFont(FontAwesomeIconView.class.getResource(TTF_PATH).openStream(), 10.0d);
        } catch (IOException ex) {
            Logger.getLogger(FontAwesomeIconView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FontAwesomeIconView(FontAwesomeIcon icon, String iconSize) {
        super(FontAwesomeIcon.class);
        setIcon(icon);
        setStyle(String.format("-fx-font-family: %s; -fx-font-size: %s;", icon.fontFamily(), iconSize));
    }

    public FontAwesomeIconView(FontAwesomeIcon icon) {
        this(icon, "1em");
    }

    public FontAwesomeIconView() {
        this(FontAwesomeIcon.ANCHOR);
    }

    @Override
    public FontAwesomeIcon getDefaultGlyph() {
        return FontAwesomeIcon.ANCHOR;
    }
}
