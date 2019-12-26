module wellbehavedfx {

    requires transitive javafx.base;
    requires transitive javafx.graphics;

    exports org.fxmisc.wellbehaved.event;
    exports org.fxmisc.wellbehaved.event.internal;
    exports org.fxmisc.wellbehaved.event.template;
}