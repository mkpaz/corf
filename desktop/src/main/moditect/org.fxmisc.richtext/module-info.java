module org.fxmisc.richtext {

    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;

    requires transitive reactfx;
    requires transitive org.fxmisc.flowless;
    requires transitive org.fxmisc.undo;
    requires transitive wellbehavedfx;

    exports org.fxmisc.richtext;
    exports org.fxmisc.richtext.event;
    exports org.fxmisc.richtext.model;
    exports org.fxmisc.richtext.util;
}
