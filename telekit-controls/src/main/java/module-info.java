module telekit.controls {

    requires java.logging;

    requires javafx.controls;
    requires javafx.fxml;

    requires transitive org.controlsfx.controls;
    requires org.fxmisc.richtext;
    requires flowless;
    requires reactfx;
    requires undofx;
    requires wellbehavedfx;

    exports org.telekit.controls.domain;
    exports org.telekit.controls.format;
    exports org.telekit.controls.i18n;
    exports org.telekit.controls.util;

    exports org.telekit.controls.components;
    opens org.telekit.controls.components;
    exports org.telekit.controls.components.richtextfx;
    opens org.telekit.controls.components.richtextfx;

    exports fontawesomefx;
    opens fontawesomefx;
    exports fontawesomefx.fa;
    opens fontawesomefx.fa;
}