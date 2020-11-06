package org.telekit.controls.richtextfx;

import javafx.css.PseudoClass;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.GenericStyledArea;

public final class RichTextFXHelper {

    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");

    public static void addFocusedStateListener(GenericStyledArea<?, ?, ?> styledArea,
                                               VirtualizedScrollPane<?> rootContainer) {
        styledArea.focusedProperty().addListener(
                (obs, oldVal, newVal) -> rootContainer.pseudoClassStateChanged(FOCUSED, newVal)
        );
    }
}
