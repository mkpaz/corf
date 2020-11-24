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

import javafx.scene.layout.StackPane;

import java.util.Collection;

/**
 * @author Jens Deters
 */
@SuppressWarnings("ALL")
public class GlyphsStack extends StackPane {

    public static GlyphsStack create() {
        return new GlyphsStack();
    }

    public GlyphsStack add(GlyphIcon icon) {
        getChildren().add(icon);
        return this;
    }

    /**
     * Add all {@code icons} to this {@link GlyphsStack}. If the icons are {@code null} or empty,
     * nothing is performed.
     *
     * @param icons The icons to add to this stack.
     * @return This instance of GlyphsStack.
     */
    public GlyphsStack addAll(GlyphIcon... icons) {
        if (icons != null && icons.length > 0) {
            this.getChildren().addAll(icons);
        }
        return this;
    }

    /**
     * Add all {@code icons} to this {@link GlyphsStack}. If the icons are {@code null} or empty,
     * nothing is performed.
     *
     * @param icons The icons to add to this stack.
     * @return This instance of GlyphsStack.
     */
    public GlyphsStack addAll(Collection<? extends GlyphIcon> icons) {
        if (icons != null && !icons.isEmpty()) {
            this.getChildren().addAll(icons);
        }
        return this;
    }

    /**
     * Add all {@code icons} to this {@link GlyphsStack}. If the icons are {@code null} or empty,
     * nothing is performed.
     *
     * @param index index at which to insert the first element from the specified collection.
     * @param icons The icons to add to this stack.
     * @return This instance of GlyphsStack.
     */
    public GlyphsStack addAll(int index, Collection<? extends GlyphIcon> icons) {
        if (icons != null && !icons.isEmpty()) {
            this.getChildren().addAll(index, icons);
        }
        return this;
    }

    public GlyphsStack remove(GlyphIcon icon) {
        getChildren().remove(icon);
        return this;
    }
}
