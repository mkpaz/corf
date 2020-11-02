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

    exports org.telekit.controls;
    exports org.telekit.controls.format;
    exports org.telekit.controls.i18n;
    exports org.telekit.controls.richtextfx;
    exports org.telekit.controls.util;

    exports fontawesomefx;
    opens fontawesomefx;
    exports fontawesomefx.fa;
    opens fontawesomefx.fa;
}