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
    exports org.telekit.controls.custom;
    exports org.telekit.controls.dialogs;
    exports org.telekit.controls.theme;
    exports org.telekit.controls.util;
    exports org.telekit.controls.widgets;
    exports org.telekit.controls.widgets.richtextfx;

    opens org.telekit.controls.demo to
            javafx.graphics, javafx.base, javafx.fxml, telekit.base;

    exports org.telekit.controls.i18n;
    exports org.telekit.controls.demo;

    // resources
    opens org.telekit.controls.i18n;
    opens org.telekit.controls.assets.fonts;
    opens org.telekit.controls.assets.fonts.FiraMono;
    opens org.telekit.controls.assets.fonts.Roboto;
    opens org.telekit.controls.assets.theme;
}