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

/**
 * @author Jens Deters
 */
public enum GlyphsStyle {

    DEFAULT("/styles/glyphs.css"),
    DARK("/styles/glyphs_dark.css"),
    LIGHT("/styles/glyphs_light.css"),
    BLUE("/styles/glyphs_blue.css"),
    RED("/styles/glyphs_red.css");

    private final String stylePath;

    private GlyphsStyle(String stylePath) {
        this.stylePath = stylePath;
    }

    public String getStylePath() {
        return stylePath;
    }

    @Override
    public String toString() {
        return stylePath;
    }
}
