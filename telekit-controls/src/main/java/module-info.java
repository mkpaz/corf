module telekit.controls {

    requires java.logging;

    requires javafx.controls;
    requires javafx.fxml;

    // dependencies with Automatic-Module-Name
    requires org.fxmisc.richtext;

    exports fontawesomefx;
    opens fontawesomefx;

    exports fontawesomefx.fa;
    opens fontawesomefx.fa;
}