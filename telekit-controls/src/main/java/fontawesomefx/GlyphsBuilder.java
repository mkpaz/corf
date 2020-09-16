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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jens Deters (mail@jensd.de)
 */
@SuppressWarnings("ALL")
public class GlyphsBuilder {

    private GlyphIcon glyphIcon;

    private GlyphsBuilder(Class<? extends GlyphIcon> clazz) {
        try {
            glyphIcon = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            Logger.getLogger(GlyphsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static GlyphsBuilder create(Class<? extends GlyphIcon> clazz) {
        return new GlyphsBuilder(clazz);
    }

    public GlyphsBuilder glyph(GlyphIcons glyph) {
        glyphIcon.setGlyphName(glyph.name());
        return this;
    }

    public GlyphsBuilder size(String size) {

        glyphIcon.setSize(size);
        return this;
    }

    public GlyphsBuilder style(String style) {
        glyphIcon.setGlyphStyle(style);
        return this;
    }

    public GlyphsBuilder styleClass(String styleClass) {
        glyphIcon.setStyleClass(styleClass);
        return this;
    }

    public GlyphIcon build() {
        return glyphIcon;
    }
}
