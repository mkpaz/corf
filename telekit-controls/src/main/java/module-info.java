module telekit.controls {

    requires telekit.base;

    // modularized dependencies
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.swing;
    requires transitive org.kordamp.ikonli.core;
    requires transitive org.kordamp.ikonli.javafx;
    requires transitive org.kordamp.ikonli.fontawesome5;
    requires transitive org.kordamp.ikonli.material2;
    requires transitive eu.hansolo.medusa;

    // not modularized dependencies
    requires org.apache.commons.lang3;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;

    // exports
    exports telekit.controls.custom;
    exports telekit.controls.dialogs;
    exports telekit.controls.theme;
    exports telekit.controls.util;
    exports telekit.controls.widgets;
    exports telekit.controls.widgets.richtextfx;

    opens telekit.controls.demo to
            javafx.graphics, javafx.base, javafx.fxml, telekit.base;

    exports telekit.controls.i18n;
    exports telekit.controls.demo;

    // resources
    opens telekit.controls.i18n;
    opens telekit.controls.assets.fonts;
    opens telekit.controls.assets.fonts.FiraMono;
    opens telekit.controls.assets.fonts.Roboto;
    opens telekit.controls.assets.theme;
}