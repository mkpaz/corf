import corf.base.plugin.Plugin;

module corf.example {

    provides Plugin with corf.example.ExamplePlugin;

    requires corf.base;
    requires org.jetbrains.annotations;
    requires org.apache.commons.lang3;

    exports corf.example;
    opens   corf.example;
    exports corf.example.i18n;
    opens   corf.example.i18n;
    exports corf.example.tools;
    opens   corf.example.tools;

    // assets
    opens corf.example.assets;
}
