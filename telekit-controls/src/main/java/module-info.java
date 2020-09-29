module telekit.controls {

    requires java.logging;

    requires javafx.controls;
    requires javafx.fxml;

    requires flowless;
    requires org.fxmisc.richtext;

    exports fontawesomefx;
    opens fontawesomefx;

    exports fontawesomefx.fa;
    opens fontawesomefx.fa;
}