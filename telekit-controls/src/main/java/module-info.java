module telekit.controls {

    requires telekit.base;

    // modularized dependencies
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.swing;

    // not modularized dependencies
    requires org.apache.commons.lang3;
    requires org.fxmisc.richtext;
    requires flowless;
    requires reactfx;
    requires undofx;
    requires wellbehavedfx;

    // exports
    exports org.telekit.controls.components;
    exports org.telekit.controls.components.dialogs;
    exports org.telekit.controls.components.richtextfx;
    exports org.telekit.controls.components.skins;
    exports org.telekit.controls.format;
    exports org.telekit.controls.i18n;
    exports org.telekit.controls.theme to telekit.ui;
    exports org.telekit.controls.util;
    exports org.telekit.controls.views;

    exports org.telekit.controls.glyphs;
    opens org.telekit.controls.glyphs;

    exports org.telekit.controls.glyphs.fontawesome;
    opens org.telekit.controls.glyphs.fontawesome;

    exports org.telekit.controls.overview;
    opens org.telekit.controls.overview;
}