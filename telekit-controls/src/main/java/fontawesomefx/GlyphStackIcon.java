/*
 * Copyright (c) 2016 Diego Cirujano Cuesta (diego.cirujano-cuesta@zeiss.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fontawesomefx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.*;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Diego Cirujano Cuesta (diego.cirujano-cuesta@zeiss.com)
 */
@SuppressWarnings("ALL")
public abstract class GlyphStackIcon<T extends Enum<T> & GlyphStackIcons<W>, V extends GlyphIcon, W extends Enum<W> & GlyphIcons> extends StackPane {

    public static final double DEFAULT_ICON_SIZE = 32.0d;
    public static final String GLYPH_ICON_STACK = "glyph-icon-stack";

    private StringProperty glyphStyle;
    private ObjectProperty<String> glyphName;
    private DoubleProperty glyphSize;

    public GlyphStackIcon() {
        this(null, DEFAULT_ICON_SIZE);
    }

    public GlyphStackIcon(T iconStack) {
        this(iconStack, DEFAULT_ICON_SIZE);
    }

    public GlyphStackIcon(T iconStack, double size) {
        T iconStackFixed = iconStack;
        if (iconStackFixed == null) {
            iconStackFixed = getDefaultGlyph();
        }
        getStyleClass().addAll("root", GLYPH_ICON_STACK, iconStackFixed.name().toLowerCase());
        setMinSize(size, size);
        setPrefSize(size, size);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        initBindings();
        initValues(iconStackFixed, size);
    }

    private void initBindings() {
        glyphSizeProperty().addListener(observable -> updateSize());
        glyphStyleProperty().addListener(observable -> updateStyle());
        glyphNameProperty().addListener(observable -> updateIcon());
    }

    private void initValues(T icon, double size) {
        for (int i = 0; i < icon.getGlyphs().length; i++) {
            W glyphIcons = icon.getGlyphs()[i];
            V glyphIcon = getGlyph(glyphIcons, size);
            glyphIcon.getStyleClass().add(GLYPH_ICON_STACK + "-" + i);
            getChildren().add(glyphIcon);
        }
    }

    private void updateSize() {
        getChildren().stream()
                .filter(node -> node instanceof GlyphIcon)
                .forEach(node -> ((GlyphIcon) node).setGlyphSize(getGlyphSize()));
        setMinSize(getGlyphSize(), getGlyphSize());
    }

    private void updateStyle() {
        setStyle(getGlyphStyle());
    }

    private void updateIcon() {
        getChildren().stream()
                .filter(node -> node instanceof GlyphIcon)
                .forEach(node -> ((GlyphIcon) node).updateIcon());
    }

    public abstract T getDefaultGlyph();

    protected abstract V getGlyph(W glyphIcons, double size);

    /**
     * Background Name property
     *
     * @return Background Name property
     */
    public final ObjectProperty<String> glyphNameProperty() {
        if (glyphName == null) {
            glyphName = new SimpleStyleableObjectProperty<>(StyleableProperties.GLYPH_NAME, this, "glyphName");
        }
        return glyphName;
    }

    public final String getGlyphName() {
        return glyphNameProperty().getValue();
    }

    public final void setGlyphName(String glyphName) {
        glyphNameProperty().setValue(glyphName);
    }

    public final void setGlyph(T glyph) {
        setGlyphName(glyph.name());
    }

    /**
     * Size property
     *
     * @return The size property
     */
    public final DoubleProperty glyphSizeProperty() {
        if (glyphSize == null) {
            glyphSize = new SimpleStyleableDoubleProperty(StyleableProperties.GLYPH_SIZE, this, "glyphSize");
            glyphSize.setValue(DEFAULT_ICON_SIZE);
        }
        return glyphSize;
    }

    public final Double getGlyphSize() {
        return glyphSizeProperty().getValue();
    }

    public final void setGlyphSize(Double size) {
        Number sizeFixed = (size == null) ? DEFAULT_ICON_SIZE : size;
        glyphSizeProperty().setValue(sizeFixed);
    }

    /**
     * StyleClass property
     *
     * @return StyleClass property
     */

    public StringProperty glyphStyleProperty() {
        if (glyphStyle == null) {
            glyphStyle = new SimpleStringProperty("");
        }
        return glyphStyle;
    }

    public String getGlyphStyle() {
        return glyphStyleProperty().getValue();
    }

    public void setGlyphStyle(String style) {
        glyphStyleProperty().setValue(style);
    }

    public GlyphStackIcon setStyleClass(String styleClass) {
        getStyleClass().add(styleClass);
        return this;
    }

    /**
     * Css Style properties
     */
    private static class StyleableProperties {

        StyleableProperties() {
        }

        private static final CssMetaData<GlyphStackIcon, String> GLYPH_NAME
                = new CssMetaData<GlyphStackIcon, String>("-glyph-name", StyleConverter.getStringConverter(), "BLANK") {

            @Override
            public boolean isSettable(GlyphStackIcon styleable) {
                return styleable.glyphName == null || !styleable.glyphName.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(GlyphStackIcon styleable) {
                return (StyleableProperty) styleable.glyphNameProperty();
            }

            @Override
            public String getInitialValue(GlyphStackIcon styleable) {
                return "BLANK";
            }
        };

        private static final CssMetaData<GlyphStackIcon, Number> GLYPH_SIZE
                = new CssMetaData<GlyphStackIcon, Number>("-glyph-size", StyleConverter.getSizeConverter(), DEFAULT_ICON_SIZE) {
            @Override
            public boolean isSettable(GlyphStackIcon styleable) {
                return styleable.glyphSize == null || !styleable.glyphSize.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(GlyphStackIcon styleable) {
                return (StyleableProperty) styleable.glyphSizeProperty();
            }

            @Override
            public Number getInitialValue(GlyphStackIcon styleable) {
                return DEFAULT_ICON_SIZE;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(StackPane.getClassCssMetaData());
            Collections.addAll(styleables, GLYPH_NAME, GLYPH_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
