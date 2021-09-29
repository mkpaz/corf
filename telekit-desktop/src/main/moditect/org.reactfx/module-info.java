module reactfx {

    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;

    exports org.reactfx;
    exports org.reactfx.collection;
    exports org.reactfx.inhibeans;
    exports org.reactfx.inhibeans.binding;
    exports org.reactfx.inhibeans.collection;
    exports org.reactfx.inhibeans.property;
    exports org.reactfx.inhibeans.value;
    exports org.reactfx.util;
    exports org.reactfx.value;
}