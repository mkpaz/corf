module telekit.controls {

    requires java.logging;

    requires javafx.controls;
    requires javafx.fxml;

    requires org.fxmisc.richtext;
    requires flowless;
    requires undofx;

    exports org.telekit.controls;
    exports org.telekit.controls.i18n;
    exports org.telekit.controls.richtextfx;

    exports fontawesomefx;
    opens fontawesomefx;
    exports fontawesomefx.fa;
    opens fontawesomefx.fa;
}